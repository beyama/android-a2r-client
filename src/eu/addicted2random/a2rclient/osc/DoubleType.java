package eu.addicted2random.a2rclient.osc;

import java.math.BigDecimal;

import eu.addicted2random.a2rclient.Range;

public class DoubleType extends NumberType {

  public DoubleType(Range<?> range) {
    super(Double.class, range);
  }

  public DoubleType() {
    this(null);
  }

  @Override
  public char getTypeCode(Object object) {
    if(object == null) return 'N';
    return 'd';
  }

  @Override
  public String getTypeName() {
    return "double";
  }

  @Override
  public Type setRange(Range<?> range) {
    if(!range.clazz.equals(clazz))
      throw new IllegalArgumentException("Range must be a Double range");
    return new DoubleType(range);
  }
  
  @Override
  public Type setRange(BigDecimal start, BigDecimal end, BigDecimal step) {
    return setRange(new Range<Double>(start, end, step, Double.class));
  }
  
  @Override
  public Object cast(Object object) {
    if(object == null) 
      return null;
    if(object instanceof Double)
      return object;
    if(object instanceof Number)
      return ((Number)object).doubleValue();
    throw new ClassCastException(String.format("Can't cast '%s' to Double", object.getClass().getName()));
  }

}
