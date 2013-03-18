package eu.addicted2random.a2rclient.grid;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a {@link Layout} section and holds the UI
 * {@link Element}s of a section.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class Section implements Serializable {
  private static final long serialVersionUID = -8835900992990759223L;

  private String id;

  private final String name;

  private final String title;

  @JsonProperty
  private final List<Element<?>> elements = new LinkedList<Element<?>>();

  @JsonBackReference("layout")
  private Layout layout;
  
  @JsonCreator
  public Section(@JsonProperty(value = "name", required = true) String name, @JsonProperty("title") String title) {
    super();
    this.name = name;
    this.title = title;
  }

  /**
   * Get section ID.
   * 
   * @return
   */
  public String getId() {
    return id;
  }

  /**
   * Set section ID.
   * 
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get section name.
   * 
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * Get section title.
   * 
   * @return
   */
  public String getTitle() {
    return title;
  }

  /**
   * Get elements.
   * 
   * @return
   */
  public List<Element<?>> getElements() {
    return elements;
  }

  /**
   * Get layout.
   * 
   * @return
   */
  public Layout getLayout() {
    return layout;
  }

  /**
   * Set layout.
   * 
   * @param layout
   */
  public void setLayout(Layout layout) {
    this.layout = layout;
  }

  /**
   * Dispose this section and all elements in it.
   */
  public void dispose() {
    for (Element<?> e : getElements())
      e.dispose();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Section other = (Section) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

}
