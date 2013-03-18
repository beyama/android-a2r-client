package eu.addicted2random.a2rclient.osc;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import eu.addicted2random.a2rclient.utils.Range;

public class PackSupport implements Pack {

  final protected Type[] types;
  final protected Object[] values;
  final protected ReentrantLock lock;

  protected boolean dirty = false;
  protected Object actor = null;
  protected List<PackListener> listeners = null;

  /**
   * Constructor to construct a new {@link PackSupport} instance for given types
   * and values.
   * 
   * @param types
   *          Array of types.
   * @param values
   *          Array of values.
   * @param lock
   *          Lock to synchronize access
   */
  public PackSupport(Type[] types, Object[] values, ReentrantLock lock) {
    super();
    this.lock = lock;
    if (types.length != values.length)
      throw new IllegalArgumentException("types and values must have the same length");
    this.types = types;
    this.values = values;
  }

  /**
   * Constructor to construct a new {@link PackSupport} instance for a given
   * types and an empty list of values.
   * 
   * @param types
   * @param lock
   *          Lock to synchronize access
   */
  public PackSupport(Type[] types, ReentrantLock lock) {
    this(types, new Object[types.length], lock);
  }

  /**
   * Constructor to construct a new {@link PackSupport} instance for given type
   * and value.
   * 
   * @param type
   * @param value
   * @param lock
   *          Lock to synchronize access
   */
  public PackSupport(Type type, Object value, ReentrantLock lock) {
    this(new Type[] { type }, new Object[] { value }, lock);
  }

  /**
   * Constructor to construct a new {@link PackSupport} instance for a given
   * type with a specified length.
   * 
   * @param type
   * @param length
   * @param lock
   *          Lock to synchronize access
   */
  public PackSupport(Type type, int length, ReentrantLock lock) {
    this(new Type[length], new Object[length], lock);

    for (int i = 0; i < length; i++)
      types[i] = type;
  }

  /**
   * Constructor to construct a new {@link PackSupport} instance for a given
   * type with a length of one.
   * 
   * @param type
   * @param length
   * @param lock
   *          Lock to synchronize access
   */
  public PackSupport(Type type, ReentrantLock lock) {
    this(new Type[] { type }, new Object[1], lock);
  }

  @Override
  public Type[] getSignature() {
    return types;
  }

  @Override
  public Object[] getValues() {
    return values;
  }

  @Override
  public Type getTypeAt(int index) {
    return types[index];
  }

  @Override
  public Object get(int index) {
    return values[index];
  }

  @Override
  public Range getRangeAt(int index) {
    Type type = getTypeAt(index);
    return type.getRange();
  }

  @Override
  public void set(int index, Object value) {
    if (!lock.isLocked())
      throw new IllegalStateException("Pack must be locked before changing it's values");

    Object oldValue = values[index];

    Type type = getTypeAt(index);
    Range range = type.getRange();

    if (type.canCast(value))
      value = type.cast(value);
    else
      return;

    if (range != null)
      value = type.cast(range.round(value));

    if (value.equals(oldValue))
      return;

    values[index] = value;

    dirty = true;
    onValueChanged(index, oldValue, value);
  }

  @Override
  public int length() {
    return values.length;
  }

  @Override
  public void lock(Object actor) {
    lock.lock();
    this.actor = actor;
  }

  @Override
  public void unlock() {
    if (!lock.isLocked())
      return;

    Object actor = this.actor;

    this.actor = null;

    if (dirty) {
      dirty = false;
      lock.unlock();
      onPacked(actor);
    } else {
      lock.unlock();
    }
  }

  protected void onValueChanged(int index, Object oldValue, Object newValue) {
    if (listeners == null)
      return;

    for (PackListener listener : listeners) {
      if (listener != actor)
        listener.onValueChanged(this, actor, index, oldValue, newValue);
    }
  }

  protected void onPacked(Object actor) {
    if (listeners == null)
      return;

    for (PackListener listener : listeners) {
      if (listener != actor)
        listener.onPacked(this);
    }
  }

  @Override
  public boolean isLocked() {
    return lock.isLocked();
  }

  @Override
  public boolean isDirty() {
    return dirty;
  }

  @Override
  public void addPackListener(PackListener listener) {
    lock.lock();
    if (listeners == null)
      listeners = new LinkedList<Pack.PackListener>();

    if (!listeners.contains(listener))
      listeners.add(listener);
    lock.unlock();
  }

  @Override
  public boolean removePackListener(PackListener listener) {
    if (listeners == null)
      return false;

    lock.lock();
    boolean ret = listeners.remove(listener);
    lock.unlock();
    return ret;
  }

}
