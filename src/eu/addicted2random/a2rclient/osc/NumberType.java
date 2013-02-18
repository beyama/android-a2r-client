package eu.addicted2random.a2rclient.osc;

import eu.addicted2random.a2rclient.utils.Range;

public abstract class NumberType implements Type {
  
  final protected Class<? extends Number> clazz;
  final protected Range range;
  
  public NumberType(Class<? extends Number> clazz) {
    this(clazz, null);
  }
  
  public NumberType(Class<? extends Number> clazz, Range range) {
    super();
    this.clazz = clazz;
    this.range = range;
  }

  @Override
  public Class<?> getNativeClass() {
    return clazz;
  }
  
  @Override
  public Range getRange() {
    return range;
  }

  @Override
  public boolean isInstance(Object object) {
    return clazz.isInstance(object);
  }

  @Override
  public boolean canCast(Object object) {
    if(object instanceof Number)
      return true;
    return false;
  }

}
