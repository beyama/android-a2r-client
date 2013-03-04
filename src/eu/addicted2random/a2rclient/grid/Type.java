package eu.addicted2random.a2rclient.grid;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Type model to describe the signature of a {@link Route}.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class Type {

  @JsonProperty(required = true)
  private String type;

  @JsonProperty
  private Double minimum;

  @JsonProperty
  private Double maximum;

  @JsonProperty
  private Double step;

  @JsonProperty("default")
  private Object defaultValue;

  public Type() {
    super();
  }

  /**
   * Get the type name.
   * 
   * @return
   */
  public String getType() {
    return type;
  }

  /**
   * Set type name.
   * 
   * @param type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Get minimum value.
   * 
   * @return
   */
  public Double getMinimum() {
    return minimum;
  }

  /**
   * Set minimum value.
   * 
   * @param minimum
   */
  public void setMinimum(Double minimum) {
    this.minimum = minimum;
  }

  /**
   * Get maximum value.
   * 
   * @return
   */
  public Double getMaximum() {
    return maximum;
  }

  /**
   * Set maximum value.
   * 
   * @param maximum
   */
  public void setMaximum(Double maximum) {
    this.maximum = maximum;
  }

  /**
   * Get step size.
   * 
   * @return
   */
  public Double getStep() {
    return step;
  }

  /**
   * Set step size.
   * 
   * @param step
   */
  public void setStep(Double step) {
    this.step = step;
  }

  /**
   * Get default value.
   * 
   * @return
   */
  public Object getDefaultValue() {
    return defaultValue;
  }

  /**
   * Set default value.
   * 
   * @param defaultValue
   */
  public void setDefaultValue(Object defaultValue) {
    this.defaultValue = defaultValue;
  }

}
