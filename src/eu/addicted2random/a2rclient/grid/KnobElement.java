package eu.addicted2random.a2rclient.grid;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.graphics.Color;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.addicted2random.a2rclient.osc.Pack;
import eu.addicted2random.a2rclient.osc.PackSupport;
import eu.addicted2random.a2rclient.osc.Type;
import eu.addicted2random.a2rclient.osc.Types;
import eu.addicted2random.a2rclient.utils.Range;
import eu.addicted2random.a2rclient.widgets.Knob;

/**
 * Implementation of an {@link Element} that creates a {@link Knob} in the grid.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class KnobElement extends Element<Knob> {
  private static final long serialVersionUID = -196128424898301544L;

  private class KnobChangeListener implements Knob.OnKnobChangeListener {

    @Override
    public void onKnobChanged(Knob knob, BigDecimal value) {
      Pack pack = KnobElement.this.getPack();
      pack.lock(knob);
      try {
        pack.set(0, value);
      } finally {
        pack.unlock();
      }
    }
  }

  @JsonProperty
  private Double minimum = null;

  @JsonProperty
  private Double maximum = null;

  @JsonProperty
  private Double step = null;

  private Integer sweepColor = null;
  private Integer outlineColor = null;

  @JsonProperty
  private boolean showSteps = false;

  private Type valueType = Types.INTEGER_TYPE;

  @JsonCreator
  public KnobElement(@JsonProperty("type") String type, @JsonProperty("x") int x, @JsonProperty("y") int y,
      @JsonProperty("cols") int cols, @JsonProperty("rows") int rows) {
    super(type, x, y, cols, rows);
  }

  /**
   * Get minimum value of Knob range.
   * 
   * @return
   */
  public Double getMinimum() {
    return minimum;
  }

  /**
   * Set minimum value of Knob range.
   * 
   * @param minimum
   */
  public void setMinimum(Double minimum) {
    this.minimum = minimum;
  }

  /**
   * Get maximum value of Knob range.
   * 
   * @return
   */
  public Double getMaximum() {
    return maximum;
  }

  /**
   * Set maximum value of Knob range.
   * 
   * @return
   */
  public void setMaximum(Double maximum) {
    this.maximum = maximum;
  }

  /**
   * Get step of Knob range.
   * 
   * @return
   */
  public Double getStep() {
    return step;
  }

  /**
   * Set step of Knob range.
   * 
   * @return
   */
  public void setStep(Double step) {
    this.step = step;
  }

  /**
   * Get sweep color.
   * 
   * @return
   */
  public int getSweepColor() {
    return sweepColor;
  }

  /**
   * Set sweep color.
   * 
   * @param sweepColor
   */
  @JsonProperty
  public void setSweepColor(String sweepColor) {
    this.sweepColor = Color.parseColor(sweepColor);
  }

  /**
   * Get outline color.
   * 
   * @return
   */
  public int getOutlineColor() {
    return outlineColor;
  }

  /**
   * Set outline color.
   * 
   * @param outlineColor
   */
  @JsonProperty
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
   * 
   * @param valueType
   */
  @JsonProperty
  public void setValueType(String valueType) {
    Type type = Types.getTypeByName(valueType);

    if (type != null)
      this.valueType = type;
  }

  @Override
  protected void setupView() {
    super.setupView();

    Knob knob = getView();

    if (sweepColor != null)
      knob.setSweepColor(sweepColor);
    if (outlineColor != null)
      knob.setOutlineColor(outlineColor);
    if (isShowSteps())
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
  protected Pack createPack(ReentrantLock lock) {
    Range range = null;

    // float or integer type?
    if (this.minimum != null && this.maximum != null)
      range = new Range(this.minimum, this.maximum, this.step);
    else
      range = new Range(1, 127);

    Type type = valueType.setRange(range);
    Object value = type.cast(range.start);
    return new PackSupport(type, value, lock);
  }

}
