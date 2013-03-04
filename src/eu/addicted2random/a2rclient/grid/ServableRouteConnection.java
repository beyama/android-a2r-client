package eu.addicted2random.a2rclient.grid;

import android.annotation.SuppressLint;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents an {@link Servable} to {@link Route} connection.
 * 
 * This class is used to connect {@link Element}s and {@link Sensor}s to a
 * {@link Route}.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
@SuppressLint("UseSparseArrays")
public class ServableRouteConnection {
  private final Map<Integer, Integer> fromTo;
  private final Map<Integer, Integer> toFrom;
  private Servable servable;
  private Route route;

  public ServableRouteConnection(Map<Integer, Integer> fromTo) {
    this.fromTo = fromTo;
    this.toFrom = new HashMap<Integer, Integer>(fromTo.size());

    for (Entry<Integer, Integer> entry : this.fromTo.entrySet())
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
   * Get servable.
   * 
   * @return
   */
  public Servable getServable() {
    return servable;
  }

  /**
   * Set servable.
   * 
   * @param serveable
   */
  public void setServable(Servable servable) {
    this.servable = servable;
  }

  /**
   * Get route.
   * 
   * @return
   */
  public Route getRoute() {
    return route;
  }

  /**
   * Set route.
   * 
   * @param route
   */
  public void setRoute(Route route) {
    this.route = route;
  }

}
