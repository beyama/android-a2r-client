package eu.addicted2random.a2rclient.grid;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONException;

import android.content.Context;
import android.hardware.SensorManager;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.addicted2random.a2rclient.fragments.GridFragment;
import eu.addicted2random.a2rclient.jam.Jam;
import eu.addicted2random.a2rclient.osc.DataNode;
import eu.addicted2random.a2rclient.osc.Hub;
import eu.addicted2random.a2rclient.osc.PackConnection;

/**
 * This class represents a Layout, usually loaded from a JSON file and rendered
 * by the {@link GridFragment}.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class Layout implements Serializable {
  private static final long serialVersionUID = 5291560734856103190L;

  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.NONE);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  static private void postParse(Context context, Layout layout) { 
    // routes
    Iterator<Route> routeIterator = layout.getRoutes().iterator();

    while (routeIterator.hasNext()) {
      Route route = routeIterator.next();
      
      if (route.onCreatePack() == null) // invalid route
        routeIterator.remove();
    }

    // sections
    List<Section> sections = layout.getSections();

    for (int i = 0; i < sections.size(); i++) {
      Section section = sections.get(i);

      section.setId(layout.getId() + "." + String.valueOf(i));

      // elements
      List<Element<?>> elements = section.getElements();

      for (int j = 0; j < elements.size(); j++) {
        Element<?> element = elements.get(j);

        element.setId(section.getId() + "." + String.valueOf(j));
        
        /**
         * Jackson can't handle back references in abstract types
         * so we set the back reference manually.
         */
        element.setSection(section);

        // connect element out-mapping with route
        if (!element.getOuts().isEmpty()) {
          for (Out out : element.getOuts()) {
            Route route = layout.getRoute(out.getAddress());

            if (route != null) {
              ServableRouteConnection connection = new ServableRouteConnection(out.getMap());
              route.addServableRouteConnection(connection);
              element.addServableRouteConnection(connection);
            }
          }
        }

      }
    }

    // sensors
    if (!layout.getSensors().isEmpty()) {
      SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

      for (Sensor sensor : layout.getSensors()) {
        sensor.setSensorManager(sensorManager);

        if (sensor.getSensor() != null) {
          if (!sensor.getOuts().isEmpty()) {
            for (Out out : sensor.getOuts()) {
              Route route = layout.getRoute(out.getAddress());

              if (route != null) {
                ServableRouteConnection connection = new ServableRouteConnection(out.getMap());
                route.addServableRouteConnection(connection);
                sensor.addServableRouteConnection(connection);
              }
            }
          }

        }
      }

    }
  }

  /**
   * Create a {@link Layout} instance form JSON input stream.
   * 
   * @param context
   * @param in
   * @return
   * @throws IOException
   * @throws JSONException
   * @throws InvalidLayoutException
   */
  static public Layout fromJSON(Context context, InputStream in) throws IOException {
    Layout layout = mapper.readValue(in, Layout.class);
    postParse(context, layout);
    return layout;
  }

  /**
   * Create a {@link Layout} instance from JSON.
   * 
   * @param context
   * @param json
   * @return
   * @throws JSONException
   * @throws InvalidLayoutException
   * @throws IOException
   * @throws JsonMappingException
   * @throws JsonParseException
   */
  static public Layout fromJSON(Context context, String json) throws JsonParseException, JsonMappingException,
      IOException {
    Layout layout = mapper.readValue(json, Layout.class);
    postParse(context, layout);
    return layout;
  }

  static public Layout fromJSON(Context context, TreeNode node) throws JsonProcessingException {
    Layout layout = mapper.treeToValue(node, Layout.class);
    postParse(context, layout);
    return layout;
  }

  /* Layout name */
  @JsonProperty
  private final String name;

  /* Layout title */
  @JsonProperty
  private final String title;

  @JsonProperty
  @JsonManagedReference("layout")
  private final List<Section> sections = new LinkedList<Section>();

  @JsonProperty
  @JsonManagedReference("layout")
  private final Set<Route> routes = new HashSet<Route>();

  @JsonProperty
  @JsonManagedReference("layout")
  private final List<Sensor> sensors = new LinkedList<Sensor>();

  private Jam jam;

  private Hub hub;

  private final ReentrantLock lock = new ReentrantLock();

  private final IdMap idMap = new IdMap();

  @JsonCreator
  public Layout(@JsonProperty(value = "name", required = true) String name, @JsonProperty("title") String title) {
    super();
    this.name = name;
    this.title = title;
  }

  /**
   * Get layout id
   * 
   * @return
   */
  public String getId() {
    return name;
  }

  /**
   * Get layout name
   * 
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * Get layout title
   * 
   * @return
   */
  public String getTitle() {
    return title;
  }

  /**
   * Get sections list
   * 
   * @return
   */
  public List<Section> getSections() {
    return sections;
  }

  public Section getSection(String id) {
    for (Section s : getSections()) {
      if (s.getId().equals(id))
        return s;
    }
    return null;
  }

  /**
   * Add a {@link Section} to the sections list
   * 
   * @param section
   */
  public void addSection(Section section) {
    sections.add(section);
  }

  /**
   * Get route by address.
   * 
   * @param address
   * @return
   */
  public Route getRoute(String address) {
    for (Route route : routes) {
      if (route.getAddress().equals(address))
        return route;
    }
    return null;
  }

  /**
   * Get routes collection.
   * 
   * @return
   */
  public Set<Route> getRoutes() {
    return routes;
  }

  /**
   * Add a {@link Route} to the routes list.
   * 
   * @param route
   */
  public void addRoute(Route route) {
    routes.add(route);
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
   * Get jam.
   * 
   * @return
   */
  public Jam getJam() {
    return jam;
  }

  /**
   * Set jam.
   * 
   * @param jam
   */
  public void setJam(Jam jam) {
    this.jam = jam;
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
    if (hub != null)
      hub.dispose();
    for (Section s : getSections())
      s.dispose();
    for (Sensor s : getSensors())
      s.dispose();
  }

  /**
   * Connect layout to the OSC hub.
   */
  private void connect() {
    for (Route route : getRoutes()) {
      // connect elements and sensors with this route
      if (route.getConnections() != null) {
        for (ServableRouteConnection connection : route.getConnections()) {
          new PackConnection(connection.getServable().getPack(), route.getPack(), connection.getFromTo(),
              connection.getToFrom());
        }
      }
      // create a data node for this route
      new DataNode(hub, route.getAddress(), route.getPack());
    }
  }

  public synchronized Hub getHub() {
    if (hub != null)
      return hub;

    hub = new Hub();
    connect();

    return hub;
  }

  /**
   * Get global lock.
   * 
   * @return
   */
  public ReentrantLock getLock() {
    return lock;
  }

  /**
   * Returns the layout title.
   */
  @Override
  public String toString() {
    return getTitle();
  }

}
