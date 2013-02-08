package eu.addicted2random.a2rclient.models.layout;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents an {@link Element} to {@link Route} connection.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public class ElementRouteConnection {
  private final Map<Integer, Integer> fromTo;
  private final Map<Integer, Integer> toFrom;
  private Element<?> element;
  private Route route;
  
  public ElementRouteConnection(Map<Integer, Integer> fromTo) {
    this.fromTo = fromTo;
    this.toFrom = new HashMap<Integer, Integer>(fromTo.size());
    
    for(Entry<Integer, Integer> entry : this.fromTo.entrySet())
      this.toFrom.put(entry.getValue(), entry.getKey());
  }

  /**
   * Get mapping from {@link Element} pack index to {@link Route} pack index.
   * 
   * @return
   */
  public Map<Integer, Integer> getFromTo() {
    return fromTo;
  }

  /**
   * Get mapping from {@link Route} pack index to {@link Element} pack index.
   * 
   * @return
   */
  public Map<Integer, Integer> getToFrom() {
    return toFrom;
  }

  /**
   * Get element.
   * @return
   */
  public Element<?> getElement() {
    return element;
  }

  /**
   * Set element.
   * @param element
   */
  public void setElement(Element<?> element) {
    this.element = element;
  }

  /**
   * Get route.
   * @return
   */
  public Route getRoute() {
    return route;
  }

  /**
   * Set route.
   * @param route
   */
  public void setRoute(Route route) {
    this.route = route;
  }
  
}
