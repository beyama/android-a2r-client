package eu.addicted2random.a2rclient.models.layout;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import android.content.Context;

import eu.addicted2random.a2rclient.grid.IdMap;
import eu.addicted2random.a2rclient.osc.PackConnection;
import eu.addicted2random.a2rclient.services.osc.DataNode;
import eu.addicted2random.a2rclient.services.osc.Hub;


public class Layout implements Serializable {
  private static final long serialVersionUID = 5291560734856103190L;

  /**
   * Create a {@link Layout} instance form JSON input stream.
   * @param in
   * @return
   * @throws IOException
   * @throws JSONException
   * @throws InvalidLayoutException
   */
  static public Layout fromJSON(Context context, InputStream in) throws IOException, JSONException, InvalidLayoutException {
    return new JSONLayoutParser(context, in).parse();
  }
  
  /**
   * Create a {@link Layout} instance from JSON.
   * 
   * @param JSON string
   * @return
   * @throws JSONException
   * @throws InvalidLayoutException
   */
  static public Layout fromJSON(Context context, String json) throws JSONException, InvalidLayoutException {
    return new JSONLayoutParser(context, json).parse();
  }

  /* Layout name */
  private final String name;
  
  /* Layout title */
  private final String title;
  
  private final List<Section> sections = new LinkedList<Section>();
  
  private final Map<String, Route> routes = new HashMap<String, Route>();
  
  private final List<Sensor> sensors = new LinkedList<Sensor>();
  
  private final IdMap idMap = new IdMap();

  public Layout(String name, String title) {
    super();
    this.name = name;
    this.title = title;
  }
  
  /**
   * Get layout id
   * @return
   */
  public String getId() {
    return name;
  }

  /**
   * Get layout name
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * Get layout title
   * @return
   */
  public String getTitle() {
    return title;
  }

  /**
   * Get sections list
   * @return
   */
  public List<Section> getSections() {
    return sections;
  }
  
  public Section getSection(String id) {
    for(Section s : getSections()) {
      if(s.getId().equals(id))
        return s;
    }
    return null;
  }
  
  /**
   * Add a {@link Section} to the sections list
   * @param section
   */
  public void addSection(Section section) {
    sections.add(section);
  }

  /**
   * Get routes collection.
   * @return
   */
  public Collection<Route> getRoutes() {
    return routes.values();
  }
  
  /**
   * Get route by address.
   * @return
   */
  public Route getRoute(String address) {
    return routes.get(address);
  }
  
  /**
   * Add a {@link Route} to the routes list.
   * @param route
   */
  public void addRoute(Route route) {
    routes.put(route.getAddress(), route);
  }
  
  /**
   * Get sensors.
   * 
   * @return
   */
  public List<Sensor> getSensors() {
    return sensors;
  }
  
  public void addSensor(Sensor sensor) {
    sensors.add(sensor);
  }
  
  /**
   * Get view id for element id.
   * 
   * @param elementId
   * @return
   */
  public int getViewId(String elementId) {
    return idMap.getId(elementId);
  }

  /**
   * Dispose this layout.
   */
  public void dispose() {
    for(Section s : getSections())
      s.dispose();
    for(Sensor s : getSensors())
      s.dispose();
  }
  
  /**
   * Connect layout to the hub.
   * 
   * @param hub
   */
  public void connect(Hub hub) {
    for(Route route : getRoutes()) {
      DataNode node = new DataNode(hub, route.getAddress(), route.getPack());
      if(route.getConnections() != null) {
        for(ServableRouteConnection connection : route.getConnections()) {
          new PackConnection(connection.getServable().getPack(), node.getPack(), connection.getFromTo(), connection.getToFrom());
        }
      }
    }
  }

}
