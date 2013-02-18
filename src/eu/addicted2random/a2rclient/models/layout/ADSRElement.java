package eu.addicted2random.a2rclient.models.layout;

import java.math.BigDecimal;

import android.content.Context;
import android.graphics.Color;
import eu.addicted2random.a2rclient.Range;
import eu.addicted2random.a2rclient.osc.Pack;
import eu.addicted2random.a2rclient.osc.PackSupport;
import eu.addicted2random.a2rclient.osc.Type;
import eu.addicted2random.a2rclient.osc.Types;
import eu.addicted2random.a2rclient.widgets.ADSR;
import eu.addicted2random.a2rclient.widgets.ADSR.Parameter;

public class ADSRElement extends Element<ADSR> {
  private static final long serialVersionUID = -885815685358024664L;
  
  private class ADSRChangeListener implements ADSR.OnADSRChangeListener {

    @Override
    public void onADSRParameterChanged(ADSR adsr, Parameter param, BigDecimal value) {
      Pack pack = ADSRElement.this.getPack();
      pack.lock(adsr);
      
      switch(param) {
      case ATTACK:
        pack.set(0, value);
        break;
      case DECAY:
        pack.set(1, value);
        break;
      case SUSTAIN:
        pack.set(2, value);
        break;
      case RELEASE:
        pack.set(3, value);
        break;
      }
      pack.unlock();
    }
    
  }
  
  private String minimum;
  
  private String maximum;
  
  private String step;
  
  private Integer attackColor;
  
  private Integer decayColor;
  
  private Integer sustainColor;
  
  private Integer releaseColor;
  
  private Type valueType = Types.INTEGER_TYPE;
  
  public ADSRElement(String type, int x, int y, int cols, int rows) {
    super(type, x, y, cols, rows);
  }

  /**
   * Get minimum value.
   * 
   * @return
   */
  public String getMinimum() {
    return minimum;
  }

  /**
   * Set minimum value.
   * 
   * @param minimum
   */
  @Option
  public void setMinimum(String minimum) {
    this.minimum = minimum;
  }

  /**
   * Get maximum value.
   * 
   * @return
   */
  public String getMaximum() {
    return maximum;
  }

  /**
   * Set maximum value.
   * 
   * @param maximum
   */
  @Option
  public void setMaximum(String maximum) {
    this.maximum = maximum;
  }

  /**
   * Get step.
   * 
   * @return
   */
  public String getStep() {
    return step;
  }

  /**
   * Set step.
   * 
   * @param step
   */
  @Option
  public void setStep(String step) {
    this.step = step;
  }

  /**
   * Get attack color.
   * 
   * @return
   */
  public Integer getAttackColor() {
    return attackColor;
  }

  /**
   * Set attack color.
   * 
   * @param attackColor
   */
  @Option
  public void setAttackColor(String attackColor) {
    this.attackColor = Color.parseColor(attackColor);
  }

  /**
   * Get decay color.
   * 
   * @return
   */
  public Integer getDecayColor() {
    return decayColor;
  }

  /**
   * Set decay color.
   * 
   * @param decayColor
   */
  @Option
  public void setDecayColor(String decayColor) {
    this.decayColor = Color.parseColor(decayColor);
  }

  /**
   * Get sustain color.
   * 
   * @return
   */
  public Integer getSustainColor() {
    return sustainColor;
  }

  /**
   * Set sustain color.
   * 
   * @param sustainColor
   */
  @Option
  public void setSustainColor(String sustainColor) {
    this.sustainColor = Color.parseColor(sustainColor);
  }

  /**
   * Get release color.
   * 
   * @return
   */
  public Integer getReleaseColor() {
    return releaseColor;
  }

  /**
   * Set release color.
   * 
   * @param releaseColor
   */
  @Option
  public void setReleaseColor(String releaseColor) {
    this.releaseColor = Color.parseColor(releaseColor);
  }

  /**
   * Get OSC value type.
   * 
   * @return
   */
  public Type getValueType() {
    return valueType;
  }

  /**
   * Set OSC value type.
   * @param valueType
   */
  @Option
  public void setValueType(String valueType) {
    Type type = Types.getTypeByName(valueType);
    
    if(type != null)
      this.valueType = type;
  }

  @Override
  protected ADSR createInstance(Context context) {
    ADSR adsr = new ADSR(context);
    adsr.setRange(getPack().getRangeAt(0));
    adsr.setOnADSRChangeListener(new ADSRChangeListener());
    return adsr;
  }

  @Override
  protected void onSync() {
    ADSR adsr = getView();
    Pack pack = getPack();
    
    adsr.setAttack(Range.valueOf(pack.get(0)));
    adsr.setDecay(Range.valueOf(pack.get(1)));
    adsr.setSustain(Range.valueOf(pack.get(2)));
    adsr.setRelease(Range.valueOf(pack.get(3)));
  }

  @Override
  protected void setupView() {
    super.setupView();
    
    ADSR adsr = getView();
    
    if(attackColor != null)
      adsr.setAttackColor(attackColor);
    if(decayColor != null)
      adsr.setDecayColor(decayColor);
    if(sustainColor != null)
      adsr.setSustainColor(sustainColor);
    if(releaseColor != null)
      adsr.setReleaseColor(releaseColor);
  }

  @Override
  protected Pack createPack() {
    Range range;
    
    if(minimum != null && maximum != null)
      range = new Range(minimum, maximum, step);
    else
      range = new Range(0, 127);
    
    Type t = valueType.setRange(range);
    Object v = t.cast(range.start);
    
    return new PackSupport(new Type[] { t, t, t, t }, new Object[] { v, v, v, v });
  }

}
