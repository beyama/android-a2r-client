package eu.addicted2random.a2rclient.osc;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import eu.addicted2random.a2rclient.Range;

public class PackSupport implements Pack {
  
  final protected Type[] types;
  final protected Object[] values;
  final protected ReentrantLock lock = new ReentrantLock();
  
  protected boolean dirty = false;
  protected Object actor = null;
  protected List<PackListener> listeners = null;
  
  /**
   * Constructor to construct a new {@link PackSupport} instance
   * for given types and values.
   * 
   * @param types Array of types.
   * @param values Array of values.
   */
  public PackSupport(Type[] types, Object[] values) {
    super();
    if(types.length != values.length)
      throw new IllegalArgumentException("types and values must have the same length");
    this.types = types;
    this.values = values;
  }
  
  /**
   * Constructor to construct a new {@link PackSupport} instance
   * for a given types and an empty list of values.
   * 
   * @param types
   */
  public PackSupport(Type[] types) {
    this(types, new Object[types.length]);
  }
  
  /**
   * Constructor to construct a new {@link PackSupport} instance
   * for given type and value.
   * 
   * @param type
   * @param value
   */
  public PackSupport(Type type, Object value) {
    this(new Type[] { type }, new Object[] { value });
  }
  
  /**
   * Constructor to construct a new {@link PackSupport} instance
   * for a given type with a specified length.
   * 
   * @param type
   * @param length
   */
  public PackSupport(Type type, int length) {
    this(new Type[length], new Object[length]);
    
    for(int i = 0; i < length; i++)
      types[i] = type;
  }
  
  /**
   * Constructor to construct a new {@link PackSupport} instance
   * for a given type with a length of one.
   * 
   * @param type
   * @param length
   */
  public PackSupport(Type type) {
    this(new Type[] { type }, new Object[1]);
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
  public Range<?> getRangeAt(int index) {
    Type type = getTypeAt(index);
    return type.getRange();
  }

  @Override
  public void set(int index, Object value) {
    if(!lock.isLocked())
      throw new IllegalStateException("Pack must be locked before changing it's values");
    
    Object oldValue = values[index];
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
    if(!lock.isLocked()) return;
    
    if(dirty) onPacked();
    
    actor = null;
    dirty = false;
    
    lock.unlock();
  }
  
  protected void onValueChanged(int index, Object oldValue, Object newValue) {
    if (listeners == null)
      return;

    for (PackListener listener : listeners) {
      if (listener != actor)
        listener.onValueChanged(this, actor, index, oldValue, newValue);
    }
  }
  
  protected void onPacked() {
    if(listeners == null) return;
    
    for(PackListener listener : listeners) {
      if(listener != actor)
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
    if(listeners == null) listeners = new LinkedList<Pack.PackListener>();
    
    if(!listeners.contains(listener))
      listeners.add(listener);
    lock.unlock();
  }

  @Override
  public boolean removePackListener(PackListener listener) {
    if(listeners == null) return false;
    
    lock.lock();
    boolean ret = listeners.remove(listener);
    lock.unlock();
    return ret;
  }

}
