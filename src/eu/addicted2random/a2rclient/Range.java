package eu.addicted2random.a2rclient;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Represents a range.
 * 
 * @author Alexander Jentz, beyama.de
 *
 * @param <T>
 */
public class Range<T extends Number> {
  public static BigDecimal MINUS_ONE = BigDecimal.valueOf(-1);
  
  public final BigDecimal start;
  public final BigDecimal end;
  public final BigDecimal step;
  public final BigDecimal scaleFactor;
  public final BigDecimal stepSize;
  public final BigDecimal distance;
  public final BigDecimal stepCount;
  public final Class<T> clazz;
  
  /**
   * Convert a number value to {@link BigDecimal}.
   * 
   * @param number
   * @return
   */
  public static BigDecimal valueOf(Number number) {
    if(number instanceof BigDecimal) {
      return (BigDecimal)number;
    } else if(number instanceof Integer) {
      return BigDecimal.valueOf((Integer)number);
    } else if(number instanceof Long) {
      return BigDecimal.valueOf((Long)number);
    } else if(number instanceof Float) {
      return new BigDecimal(String.valueOf((Float)number));
    } else if(number instanceof Double) {
      return new BigDecimal(String.valueOf((Double)number));
    } else {
      throw new IllegalArgumentException("Type unsupported");
    }
  }
  
  public Range(BigDecimal start, BigDecimal end, BigDecimal step, Class<T> clazz) {
    int scale = step.scale();
    
    this.start = start.setScale(scale);
    this.end = end.setScale(scale);
    this.step = step.setScale(scale);
    this.clazz = clazz;
    
    if(this.start.compareTo(this.end) != -1)
      throw new IllegalArgumentException("start must be lower than end");
    
    this.scaleFactor = this.end.subtract(this.start);
    
    if(this.start.compareTo(BigDecimal.ZERO) == -1 && this.end.compareTo(BigDecimal.ZERO) == 1)
      this.distance = this.start.multiply(MINUS_ONE).add(this.end);
    else
      this.distance = this.scaleFactor;
    
    this.stepCount = this.distance.divide(this.step);
    
    if(this.stepCount.compareTo(BigDecimal.ONE) < 0)
      throw new IllegalArgumentException("Distance divided by step can't be lower than 1");
    
    this.stepSize = distance.divide(stepCount);
  }
  
  @SuppressWarnings("unchecked")
  public Range(T start, T end, T step) {
    this(valueOf(start), valueOf(end), valueOf(step), (Class<T>)start.getClass());
  }
  
  /**
   * Scale a {@link BigDecimal} {@code value} form {@code from} Range to a {@link BigDecimal} of this range.
   * 
   * @param from
   * @param value
   * @return Scaled value
   */
  public BigDecimal scale(Range<?> from, BigDecimal value) {
    return scaleFactor.multiply(value.subtract(from.start)).divide(from.scaleFactor, RoundingMode.DOWN).add(start);
  }
  
  /**
   * Scale an {@link Integer} {@code value} from from-Range to a value of this range.
   * 
   * @param from Range to scale from
   * @param value Value to scale to this range
   * @return
   */
  public T scale(Range<Integer> from, Integer value) {
    return this.cast(this.scale(from, valueOf(value)));
  }
  
  /**
   * Scale an {@link Long} {@code value} from from-Range to a value of this range.
   * 
   * @param from Range to scale from
   * @param value Value to scale to this range
   * @return
   */
  public T scale(Range<Long> from, Long value) {
    return this.cast(this.scale(from, valueOf(value)));
  }
  
  /**
   * Scale an {@link Float} {@code value} from from-Range to a value of this range.
   * 
   * @param from Range to scale from
   * @param value Value to scale to this range
   * @return
   */
  public T scale(Range<Float> from, Float value) {
    return this.cast(this.scale(from, valueOf(value)));
  }
  
  /**
   * Scale an {@link Double} {@code value} from from-Range to a value of this range.
   * 
   * @param from Range to scale from
   * @param value Value to scale to this range
   * @return
   */
  public T scale(Range<Double> from, Double value) {
    return this.cast(this.scale(from, valueOf(value)));
  }
  
  /**
   * Round (down) value to next multiple of stepSize.
   * 
   * Return this.start if value is lower than this.start
   * and this.end if value is greater than this.end.
   * 
   * @param value
   * @return
   */
  public BigDecimal round(BigDecimal value) {
    int comparedToStart = value.compareTo(this.start);
    int comparedToEnd = value.compareTo(this.end);
    
    if(comparedToStart == 0 || comparedToEnd == 0)
      return value;
    
    if(comparedToStart == -1)
      return this.start;
    
    if(comparedToEnd == 1)
      return this.end;
    
    BigDecimal steps = value.divide(stepSize, 0, RoundingMode.DOWN);
    
    if(this.stepCount.compareTo(steps) >= 0)
      return steps.multiply(stepSize);
    else
      return this.end;
  }
  
  /**
   * Round (down) value to next multiple of stepSize.
   * 
   * Return this.start if value is lower than this.start
   * and this.end if value is greater than this.end.
   * 
   * @param value
   * @return
   */
  public T round(Number value) {
    return cast(round(valueOf(value)));
  }
  
  /**
   * Cast a {@link BigDecimal} value to type T.
   * 
   * @param value
   * @return
   */
  @SuppressWarnings("unchecked")
  public T cast(BigDecimal value) {
    if(clazz.equals(BigDecimal.class))
      return (T)value;
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
  
  /**
   * Cast a {@link Number} to type T.
   * @param value
   * @return
   */
  public T cast(Number value) {
    return this.cast(valueOf(value));
  }

}
