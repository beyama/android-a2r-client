package eu.addicted2random.a2rclient.jam;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Jam {

  @JsonProperty
  private String id;

  @JsonProperty
  private String title;

  @JsonProperty
  private String description;

  @JsonProperty
  private String stream;

  @JsonCreator
  public Jam(@JsonProperty(value = "id", required = true) String id,
      @JsonProperty(value = "title", required = true) String title, @JsonProperty("description") String description) {
    super();
    this.id = id;
    this.title = title;
    this.description = description;
  }

  /**
   * Get jam id.
   * 
   * @return
   */
  public String getId() {
    return id;
  }

  /**
   * Set jam id.
   * 
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get jam title.
   * 
   * @return
   */
  public String getTitle() {
    return title;
  }

  /**
   * Set jam title.
   * 
   * @param title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Get jam description.
   * 
   * @return
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set jam description.
   * 
   * @param description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Get stream address.
   * 
   * @return
   */
  public String getStream() {
    return stream;
  }

  /**
   * Set stream address.
   * 
   * @param stream
   */
  public void setStream(String stream) {
    this.stream = stream;
  }

}
