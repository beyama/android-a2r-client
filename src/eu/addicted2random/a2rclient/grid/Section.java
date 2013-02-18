package eu.addicted2random.a2rclient.grid;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Section implements Serializable {
  private static final long serialVersionUID = -8835900992990759223L;

  private String id;
  private final String name;
  private final String title;
  
  private final List<Element<?>> elements = new LinkedList<Element<?>>();

  public Section(String name, String title) {
    super();
    this.name = name;
    this.title = title;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public String getTitle() {
    return title;
  }

  public List<Element<?>> getElements() {
    return elements;
  }
  
  public void addElement(Element<?> element) {
    this.elements.add(element);
  }
  
  /**
   * Dispose this section.
   */
  public void dispose() {
    for(Element<?> e : getElements())
      e.dispose();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

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
