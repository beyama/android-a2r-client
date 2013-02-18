package eu.addicted2random.a2rclient.grid;

import java.math.BigDecimal;

import android.content.Context;
import android.graphics.Color;
import eu.addicted2random.a2rclient.osc.Pack;
import eu.addicted2random.a2rclient.osc.PackSupport;
import eu.addicted2random.a2rclient.osc.Type;
import eu.addicted2random.a2rclient.osc.Types;
import eu.addicted2random.a2rclient.utils.Range;
import eu.addicted2random.a2rclient.widgets.Knob;

public class KnobElement extends Element<Knob> {
  private static final long serialVersionUID = -196128424898301544L;
  
  private class KnobChangeListener implements Knob.OnKnobChangeListener {

    @Override
    public void onKnobChanged(Knob knob, BigDecimal value) {
      Pack pack = KnobElement.this.getPack();
      pack.lock(knob);
      pack.set(0, value);
      pack.unlock();
    }
  }

  private Double minimum = null;
  private Double maximum = null;
  private Double step = null;
  private Integer sweepColor = null;
  private Integer outlineColor = null;
  private boolean showSteps = false;
  private Type valueType = Types.INTEGER_TYPE;
  
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
   * Get step of Knob range.
   * @return
   */
  public Double getStep() {
    return step;
  }

  /**
   * Set step of Knob range.
   * @return
   */
  @Option
  public void setStep(Double step) {
    this.step = step;
  }
  
  /**
   * Set step size of Knob range.
   * @return
   */
  @Option
  public void setStep(Integer step) {
    this.step = step.doubleValue();
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

  public boolean isShowSteps() {
    return showSteps;
  }

  /**
   * Set show steps.
   * 
   * @param showSteps
   */
  @Option
  public void setShowSteps(boolean showSteps) {
    this.showSteps = showSteps;
  }

  /**
   * Get value type.
   * 
   * @return
   */
  public Type getValueType() {
    return valueType;
  }

  /**
   * Set value type.
   * @param valueType
   */
  @Option
  public void setValueType(String valueType) {
    Type type = Types.getTypeByName(valueType);
    
    if(type != null)
      this.valueType = type;
  }

  @Override
  protected void setupView() {
    super.setupView();
    
    Knob knob = getView();
    
    if(sweepColor != null)
      knob.setSweepColor(sweepColor);
    if(outlineColor != null)
      knob.setOutlineColor(outlineColor);
    if(isShowSteps())
      knob.setShowSteps(true);
  }

  @Override
  protected Knob createInstance(Context context) {
    Pack pack = getPack();

    Knob knob = new Knob(context);
    knob.setRange(pack.getRangeAt(0));
    knob.setOnKnobChangeListener(new KnobChangeListener());
    return knob;
  }

  @Override
  public void onSync() {
    getView().setValue(Range.valueOf(getPack().get(0)));
  }
  
  @Override
  protected void onResetView() {
    getView().setOnKnobChangeListener(null);
  }

  @Override
  protected Pack createPack() {
    Range range = null;
    
    // float or integer type?
    if(this.minimum != null && this.maximum != null)
      range = new Range(this.minimum, this.maximum, this.step);
    else
      range = new Range(1, 127);

    Type type = valueType.setRange(range);
    Object value = type.cast(range.start);
    return new PackSupport(type, value);
  }
  
  
  
}
