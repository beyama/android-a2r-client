package eu.addicted2random.a2rclient.osc;

import eu.addicted2random.a2rclient.Range;

public class BooleanType implements Type {

  @Override
  public Class<?> getNativeClass() {
    return Boolean.class;
  }

  @Override
  public char getTypeCode(Object object) {
    if(object == null) return 'N';
    boolean bool = (Boolean)object;
    
    return bool ? 'T' : 'F';
  }

  @Override
  public String getTypeName() {
    return "boolean";
  }

  @Override
  public Type setRange(Range range) {
    throw new UnsupportedOperationException("Can't set range for boolean type");
  }

  @Override
  public Range getRange() {
    return null;
  }

  @Override
  public boolean isInstance(Object object) {
    return object instanceof Boolean;
  }

  @Override
  public boolean canCast(Object object) {
    return true;
  }

  @Override
  public Object cast(Object object) {
    if(object == null) return false;
    
    if(object instanceof Boolean) return (Boolean)object;
    
    if(object instanceof String) return ((String)object).length() > 0;
    
    if(object instanceof Number) return ((Number)object).intValue() > 0;
    
    return false;
  }

}
