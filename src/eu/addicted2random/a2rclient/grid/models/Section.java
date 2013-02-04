package eu.addicted2random.a2rclient.grid.models;

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

}
