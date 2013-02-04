package eu.addicted2random.a2rclient;

import java.math.BigDecimal;


public abstract class Range<T extends Number> {
  final BigDecimal start;
  final BigDecimal end;
  final BigDecimal step;
  final BigDecimal scaleFactor;
  final Class<T> clazz;
  
  public static BigDecimal valueOf(Number number) {
    if(number instanceof Integer || number instanceof Long) {
      return BigDecimal.valueOf((Long)number);
    } else if(number instanceof Float || number instanceof Double) {
      return new BigDecimal(String.valueOf((Long)number));
    } else {
      throw new IllegalArgumentException("Type unsupported");
    }
  }
  
  @SuppressWarnings("unchecked")
  public Range(T start, T end, T step) {
    this.start = valueOf(start);
    this.end = valueOf(end);
    this.step = valueOf(step);
    
    if(this.start.compareTo(this.end) != -1)
      throw new IllegalArgumentException("start must be lower than end");
    
    this.scaleFactor = this.end.subtract(this.start);
    
    if(start instanceof Integer)
      this.clazz = (Class<T>)Integer.class;
    else if(start instanceof Long)
      this.clazz = (Class<T>)Long.class;
    else if(start instanceof Float)
      this.clazz = (Class<T>)Float.class;
    else if(start instanceof Double)
      this.clazz = (Class<T>)Double.class;
    else
      throw new IllegalArgumentException("Type unsupported");
  }
  
  public BigDecimal scale(Range<?> from, BigDecimal value) {
    return scaleFactor.multiply(value.subtract(from.start)).divide(from.scaleFactor).add(start);
  }
  
  public T scale(Range<Integer> from, Integer value) {
    return this.cast(this.scale(from, valueOf(value)));
  }
  
  public T scale(Range<Long> from, Long value) {
    return this.cast(this.scale(from, valueOf(value)));
  }
  
  public T scale(Range<Float> from, Float value) {
    return this.cast(this.scale(from, valueOf(value)));
  }
  
  public T scale(Range<Double> from, Double value) {
    return this.cast(this.scale(from, valueOf(value)));
  }
  
  @SuppressWarnings("unchecked")
  public T cast(BigDecimal value) {
    if(clazz.equals(Integer.class))
      return (T)(Integer)value.intValue();
    if(clazz.equals(Long.class))
      return (T)(Long)value.longValue();
    if(clazz.equals(Float.class))
      return (T)(Float)value.floatValue();
    if(clazz.equals(Double.class))
      return (T)(Double)value.doubleValue();
    return null;
  }

}
