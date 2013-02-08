package eu.addicted2random.a2rclient.models.layout;

import java.math.BigDecimal;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import eu.addicted2random.a2rclient.Range;
import eu.addicted2random.a2rclient.osc.IntegerType;
import eu.addicted2random.a2rclient.osc.Pack;
import eu.addicted2random.a2rclient.osc.PackSupport;
import eu.addicted2random.a2rclient.osc.Type;
import eu.addicted2random.a2rclient.osc.Types;
import eu.addicted2random.a2rclient.widgets.FloatKnob;
import eu.addicted2random.a2rclient.widgets.IntegerKnob;
import eu.addicted2random.a2rclient.widgets.Knob;

public class KnobElement extends Element<Knob<?>> {
  private static final long serialVersionUID = -196128424898301544L;
  
  private class KnobChangeListener<T extends Number> implements Knob.OnKnobChangeListener<T> {

    @Override
    public void onKnobChanged(Knob<T> knob, T value) {
      Pack pack = KnobElement.this.getPack();
      pack.lock(knob);
      pack.set(0, value);
      pack.unlock();
    }
    
  }

  private Double minimum = null;
  private Double maximum = null;
  private Double stepSize = null;
  private int sweepColor = -1;
  private int outlineColor = -1;
  
  static boolean hasFraction(Double value) {
    return (value - Math.round(value) != value);
  }
  
  public KnobElement(String type, int x, int y, int cols, int rows) {
    super(type, x, y, cols, rows);
  }

  /**
   * Get minimum value of Knob range.
   * @return
   */
  public Double getMinimum() {
    return minimum;
  }

  /**
   * Set minimum value of Knob range.
   * @param minimum
   */
  @Option
  public void setMinimum(Double minimum) {
    this.minimum = minimum;
  }
  
  /**
   * Set minimum value of Knob range.
   * @param minimum
   */
  @Option
  public void setMinimum(Integer minimum) {
    this.minimum = minimum.doubleValue();
  }

  /**
   * Get maximum value of Knob range.
   * @return
   */
  public Double getMaximum() {
    return maximum;
  }

  /**
   * Set maximum value of Knob range.
   * @return
   */
  @Option
  public void setMaximum(Double maximum) {
    this.maximum = maximum;
  }
  
  /**
   * Set maximum value of Knob range.
   * @return
   */
  @Option
  public void setMaximum(Integer maximum) {
    this.maximum = maximum.doubleValue();
  }

  /**
   * Get step size of Knob range.
   * @return
   */
  public Double getStepSize() {
    return stepSize;
  }

  /**
   * Set step size of Knob range.
   * @return
   */
  @Option
  public void setStepSize(Double stepSize) {
    this.stepSize = stepSize;
  }
  
  /**
   * Set step size of Knob range.
   * @return
   */
  @Option
  public void setStepSize(Integer stepSize) {
    this.stepSize = stepSize.doubleValue();
  }
  
  /**
   * Get sweep color.
   * @return
   */
  public int getSweepColor() {
    return sweepColor;
  }

  /**
   * Set sweep color.
   * @param sweepColor
   */
  @Option
  public void setSweepColor(String sweepColor) {
    this.sweepColor = Color.parseColor(sweepColor);
  }

  /**
   * Get outline color.
   * @return
   */
  public int getOutlineColor() {
    return outlineColor;
  }

  /**
   * Set outline color.
   * @param outlineColor
   */
  @Option
  public void setOutlineColor(String outlineColor) {
    this.outlineColor = Color.parseColor(outlineColor);
  }

  @Override
  protected void setupView(View view) {
    super.setupView(view);
    
    Knob<?> knob = (Knob<?>)view;
    
    if(sweepColor != -1)
      knob.setSweepColor(sweepColor);
    if(outlineColor != -1)
      knob.setOutlineColor(outlineColor);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Knob<?> createInstance(Context context) {
    
    Pack pack = getPack();
    Type type = pack.getTypeAt(0);
    
    if(type instanceof IntegerType) {
      IntegerKnob knob = new IntegerKnob(context);
      if(type.getRange() != null)
        knob.setRange((Range<Integer>)type.getRange());
      knob.setOnKnobChangeListener(new KnobChangeListener<Integer>());
      return knob;
    } else {
      FloatKnob knob = new FloatKnob(context);
      if(type.getRange() != null)
        knob.setRange((Range<Float>)type.getRange());
      knob.setOnKnobChangeListener(new KnobChangeListener<Float>());
      return knob;
    }
  }

  @Override
  public void onSync() {
    Knob<?> knob = getView();
    Object value = getPack().get(0);
    
    if(knob instanceof IntegerKnob) {
      Log.v("KnobElement", "setValue " + String.valueOf(value));
      ((IntegerKnob)knob).setValue((Integer)value, true);
    } else {
      ((FloatKnob)knob).setValue((Float)value, true);
    }
  }
  
  @Override
  protected void onResetView(View view) {
    Knob<?> knob = (Knob<?>)view;
    knob.setOnKnobChangeListener(null);
  }

  @Override
  protected Pack createPack() {
    Type type;
    
    BigDecimal minimum;
    BigDecimal maximum;
    BigDecimal step;
    
    if(this.minimum != null && this.maximum != null && this.stepSize != null) {
      
      if(hasFraction(this.minimum) || hasFraction(this.maximum) || hasFraction(this.stepSize)) {
        type = Types.FLOAT_TYPE;
        
        minimum = new BigDecimal(this.minimum.floatValue());
        maximum = new BigDecimal(this.maximum.floatValue());
        step = new BigDecimal(this.stepSize.floatValue());
        
        type = type.setRange(minimum, maximum, step);
      } else {
        type = Types.INTEGER_TYPE;
        
        minimum = new BigDecimal(this.minimum.intValue());
        maximum = new BigDecimal(this.maximum.intValue());
        step = new BigDecimal(this.stepSize.intValue());

        type = type.setRange(minimum, maximum, step);
      }
    } else {
      type = Types.INTEGER_TYPE;
    }
    return new PackSupport(type);
  }
  
  
  
}
