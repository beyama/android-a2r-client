package eu.addicted2random.a2rclient.grid.models;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import eu.addicted2random.a2rclient.widgets.FloatKnob;
import eu.addicted2random.a2rclient.widgets.IntegerKnob;
import eu.addicted2random.a2rclient.widgets.Knob;

public class KnobElement extends Element<Knob<?>> {
  private static final long serialVersionUID = -196128424898301544L;

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
  public void setupView(View view) {
    super.setupView(view);
    
    Knob<?> knob = (Knob<?>)view;
    
    if(sweepColor != -1)
      knob.setSweepColor(sweepColor);
    if(outlineColor != -1)
      knob.setOutlineColor(outlineColor);
  }

  @Override
  public Knob<?> createInstance(Context context) {
    if(minimum != null && maximum != null && stepSize != null) {
      
      if(hasFraction(minimum) || hasFraction(maximum) || hasFraction(stepSize)) {
        FloatKnob knob = new FloatKnob(context);
        knob.setRange(minimum.floatValue(), maximum.floatValue(), stepSize.floatValue());
        return knob;
      } else {
        IntegerKnob knob = new IntegerKnob(context);
        knob.setRange(minimum.intValue(), maximum.intValue(), stepSize.intValue());
        return knob;
      }
    }
    return new IntegerKnob(context);
  }
}
