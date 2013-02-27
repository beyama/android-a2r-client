package eu.addicted2random.a2rclient.grid;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Type model to describe a signature for a {@link Route}.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public class Type {

  @JsonProperty(required=true)
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Double getMinimum() {
    return minimum;
  }

  public void setMinimum(Double minimum) {
    this.minimum = minimum;
  }

  public Double getMaximum() {
    return maximum;
  }

  public void setMaximum(Double maximum) {
    this.maximum = maximum;
  }

  public Double getStep() {
    return step;
  }

  public void setStep(Double step) {
    this.step = step;
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(Object defaultValue) {
    this.defaultValue = defaultValue;
  }
  
}
