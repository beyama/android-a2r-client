package eu.addicted2random.a2rclient.jam;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.addicted2random.a2rclient.grid.Layout;

public class Jam {

  @JsonProperty
  private String id;

  @JsonProperty
  private String title;

  @JsonProperty
  private String description;

  private List<Layout> layouts;

  private Layout currentSelectedLayout;

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
   * Get list of available layouts.
   * 
   * @return
   */
  public List<Layout> getLayouts() {
    return layouts;
  }

  /**
   * Set list of available layouts.
   * 
   * @param layouts
   */
  public void setLayouts(List<Layout> layouts) {
    this.layouts = layouts;
  }

  /**
   * Get current selected layout.
   * 
   * @return
   */
  public Layout getCurrentSelectedLayout() {
    return currentSelectedLayout;
  }

  /**
   * Set current selected layout.
   * 
   * @param currentSelectedLayout
   */
  public void setCurrentSelectedLayout(Layout currentSelectedLayout) {
    this.currentSelectedLayout = currentSelectedLayout;
  }

}
