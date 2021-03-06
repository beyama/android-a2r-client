package eu.addicted2random.a2rclient.osc;

import eu.addicted2random.a2rclient.utils.Range;

public class FloatType extends NumberType {
  
  public FloatType(Range range) {
    super(Float.class, range);
  }

  public FloatType() {
    this(null);
  }

  @Override
  public char getTypeCode(Object object) {
    if(object == null) return 'N';
    return 'f';
  }

  @Override
  public String getTypeName() {
    return "float";
  }

  @Override
  public Type setRange(Range range) {
    return new FloatType(range);
  }
  
  @Override
  public Object cast(Object object) {
    if(object == null) 
      return null;
    if(object instanceof Float)
      return object;
    if(object instanceof Number)
      return ((Number)object).floatValue();
    throw new ClassCastException(String.format("Can't cast '%s' to Float", object.getClass().getName()));
  }
  
}
