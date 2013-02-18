package eu.addicted2random.a2rclient.osc;

import eu.addicted2random.a2rclient.utils.Range;

public interface Pack {
  public interface PackListener {
    public void onValueChanged(Pack source, Object actor, int index, Object oldValue, Object newValue);
    public void onPacked(Pack source);
  }
  
  /**
   * Get type signature of this pack.
   * 
   * @return
   */
  public Type[] getSignature();

  /**
   * Get values of this pack.
   * @return
   */
  public Object[] getValues();
  
  /**
   * Get type at index.
   * 
   * @param index
   * @return
   */
  public Type getTypeAt(int index);
  
  /**
   * Get value at index.
   * 
   * @param index
   * @return
   */
  public Object get(int index);
  
  /**
   * Get range at index.
   * 
   * @param index
   * @return
   */
  public Range getRangeAt(int index);

  /**
   * Set value at specified index.
   * @param index
   * @param value
   */
  public void set(int index, Object value);
  
  /**
   * Get the length of the pack values/types array.
   * 
   * @return
   */
  public int length();
  
  /**
   * Lock this pack exclusively for the calling thread.
   * This is used to update multiple values before calling any
   * pack listener.
   * 
   * No {@link PackListener#onPacked(Pack)} will be called until
   * the Pack will be unlocked.
   * 
   * @param actor The object which wishes to apply changes to the pack.
   * 
   */
  public void lock(Object actor);
  
  /**
   * Unlock this pack see {@link Pack#lock()}.
   */
  public void unlock();
  
  /**
   * Returns true if this pack is locked otherwise false.
   * @return
   */
  public boolean isLocked();
  
  /**
   * Returns true if this pack will emit {@link PackListener#onPacked(Pack)}
   * on next {@link #endPack()} call.
   * @return
   */
  public boolean isDirty();
  
  /**
   * Add a pack listener.
   * 
   * @param listener to add.
   */
  public void addPackListener(PackListener listener);
  
  /**
   * Remove a pack listener.
   * 
   * @param listener to remove.
   * @return
   */
  public boolean removePackListener(PackListener listener);
  
}
