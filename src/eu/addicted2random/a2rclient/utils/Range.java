package eu.addicted2random.a2rclient.utils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Represents a range.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public class Range implements Serializable {
  private static final long serialVersionUID = 1293233932199251830L;
  
  public static BigDecimal MINUS_ONE = BigDecimal.valueOf(-1);
  public static MathContext MC = new MathContext(1, RoundingMode.DOWN);
  
  public final BigDecimal start;
  public final BigDecimal end;
  public final BigDecimal step;
  public final BigDecimal scaleFactor;
  public final BigDecimal distance;
  
  /**
   * Convert a number value to {@link BigDecimal}.
   * 
   * @param number
   * @return
   */
  public static BigDecimal valueOf(Object number) {
    if(number == null) return null;
    
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
    } else if(number instanceof String) {
      return new BigDecimal((String)number);
    } else {
      throw new IllegalArgumentException(String.format("Type unsupported '%s'", number == null ? "null" : number.getClass().getName()));
    }
  }
  
  public Range(BigDecimal start, BigDecimal end, BigDecimal step) {
    this.start = start;
    this.end = end;
    
    if(this.start.compareTo(BigDecimal.ZERO) == -1 && this.end.compareTo(BigDecimal.ZERO) == 1)
      this.distance = this.start.multiply(MINUS_ONE).add(this.end);
    else
      this.distance = this.end.subtract(this.start);
  
    this.scaleFactor = this.end.subtract(this.start);
    
    if(step == null) {
      if(start.scale() == 0 && end.scale() == 0)
        this.step = BigDecimal.ONE;
      else
        this.step = null;
    } else {
      if(step.compareTo(BigDecimal.ZERO) <= 0)
        throw new IllegalArgumentException("Step can't be zero or less than zero (" + step.toPlainString() + ").");
      if(step.subtract(step.abs(MC)).equals(step))
        this.step = step.setScale(0);
      else
        this.step = step;
    }
  }
  
  public Range(BigDecimal start, BigDecimal end) {
    this(start, end, null);
  }
  
  public Range(Object start, Object end) {
    this(valueOf(start), valueOf(end));
  }
  
  public Range(Object start, Object end, Object step) {
    this(valueOf(start), valueOf(end), step != null ? valueOf(step) : null);
  }
  
  /**
   * Scale a {@link BigDecimal} {@code value} form {@code from} Range to a {@link BigDecimal} of this range.
   * 
   * @param from
   * @param value
   * @return Scaled value
   */
  public BigDecimal scale(Range from, BigDecimal value) {
    return scaleFactor.multiply(value.subtract(from.start)).divide(from.scaleFactor, RoundingMode.DOWN).add(start);
  }
  
  /**
   * Scale a {@link BigDecimal} {@code value} form {@code from} Range to a {@link BigDecimal} of this range.
   * 
   * @param from
   * @param value
   * @return Scaled value
   */
  public BigDecimal scale(Range from, Object value) {
    return this.scale(from, valueOf(value));
  }
  
  /**
   * Check that the value is between start and end.
   * 
   * if step is not null, round the value to a full step.
   * 
   * @param value
   * @param roundingMode
   * @return
   */
  public BigDecimal round(BigDecimal value, RoundingMode roundingMode) {
    // make value a multiple of step
    if(this.step != null) {
      BigDecimal steps = value.divide(this.step, 0, roundingMode);
      value = this.step.multiply(steps);
    }
    
    BigDecimal min = this.start.min(this.end);
    BigDecimal max = this.start.max(this.end);
    
    int comparedToMin = value.compareTo(min);
    int comparedToMax = value.compareTo(max);
    
    if(comparedToMin == -1)
      value = min;
    else if(comparedToMax == 1)
      value = max;
    
    if(this.step != null && value.scale() != this.step.scale())
      value = value.setScale(this.step.scale());
    
    return value;
  }
  
  /**
   * Check that the value is between start and end.
   * 
   * if step is not null, round the value (down) to a full step.
   * 
   * @param value
   * @return
   */
  public BigDecimal round(BigDecimal value) {
    return round(value, RoundingMode.HALF_UP);
  }
  
  /**
   * Check that the value is between start and end.
   * 
   * if step is not null, round the value to a full step.
   * 
   * @param value
   * @param roundingMode
   * @return
   */
  public BigDecimal round(Object value, RoundingMode roundingMode) {
    return round(valueOf(value), roundingMode);
  }
  
  /**
   * Check that the value is between start and end.
   * 
   * if step is not null, round the value (down) to a full step.
   * 
   * @param value
   * @return
   */
  public BigDecimal round(Object value) {
    return round(value, RoundingMode.DOWN);
  }

  public Integer steps() {
    if(step == null) return null;
    return distance.divide(step, 0, RoundingMode.DOWN).intValue();
  }
  
}
