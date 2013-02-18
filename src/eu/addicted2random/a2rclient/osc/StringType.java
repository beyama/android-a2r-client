package eu.addicted2random.a2rclient.osc;

import eu.addicted2random.a2rclient.utils.Range;

public class StringType implements Type {

  @Override
  public Class<?> getNativeClass() {
    return String.class;
  }

  @Override
  public char getTypeCode(Object object) {
    if(object == null) return 'N';
    return 's';
  }

  @Override
  public String getTypeName() {
    return "string";
  }

  @Override
  public Type setRange(Range range) {
    throw new UnsupportedOperationException("StringType doesn't support ranges");
  }
  
  @Override
  public Range getRange() {
    return null;
  }

  @Override
  public boolean isInstance(Object object) {
    return object instanceof String;
  }

  @Override
  public boolean canCast(Object object) {
    return true;
  }

  @Override
  public Object cast(Object object) {
    if(object instanceof String) return object;
    return object.toString();
  }

}
