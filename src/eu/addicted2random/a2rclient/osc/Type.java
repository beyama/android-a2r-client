package eu.addicted2random.a2rclient.osc;

import eu.addicted2random.a2rclient.utils.Range;

public interface Type {  
  /**
   * Get Java class for OSC type.
   * @return
   */
  public Class<?> getNativeClass();
  
  /**
   * Get OSC type code for object.
   * @param object
   * @return
   */
  public char getTypeCode(Object object);
  
  /**
   * Get OSC type name for object.
   * @param object
   * @return
   */
  public String getTypeName();
  
  /**
   * Set range for number types. It returns a new
   * instance of {@link Type} with the range as range.
   * 
   * Throws an {@link UnsupportedOperationException} if this
   * isn't a number type.
   * 
   * @param range
   * @return
   */
  public Type setRange(Range range);
  
  /**
   * Returns range object or null.
   * @return
   */
  public Range getRange();
  
  /**
   * Returns true if object is an instance of the
   * native type otherwise false.
   * 
   * @param object
   * @return
   */
  public boolean isInstance(Object object);
  
  /**
   * Can object casted to this type?
   * 
   * @param object
   * @return
   */
  public boolean canCast(Object object);
  
  /**
   * Cast object to this type.
   * 
   * Throws a {@link ClassCastException} if this
   * isn't possible.
   * 
   * @param object
   * @return
   */
  public Object cast(Object object);
}
