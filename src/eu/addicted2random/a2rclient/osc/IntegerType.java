package eu.addicted2random.a2rclient.osc;

import eu.addicted2random.a2rclient.utils.Range;

public class IntegerType extends NumberType {

  public IntegerType(Range range) {
    super(Integer.class, range);
  }

  public IntegerType() {
    this(null);
  }

  @Override
  public char getTypeCode(Object object) {
    if(object == null) return 'N';
    return 'i';
  }

  @Override
  public String getTypeName() {
    return "integer";
  }

  @Override
  public Type setRange(Range range) {
    return new IntegerType(range);
  }
  
  @Override
  public Object cast(Object object) {
    if(object == null) 
      return null;
    if(object instanceof Integer)
      return object;
    if(object instanceof Number)
      return ((Number)object).intValue();
    throw new ClassCastException(String.format("Can't cast '%s' to Integer", object.getClass().getName()));
  }

}
