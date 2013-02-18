package eu.addicted2random.a2rclient.grid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;
import eu.addicted2random.a2rclient.osc.Pack;
import eu.addicted2random.a2rclient.osc.PackSupport;
import eu.addicted2random.a2rclient.osc.Type;
import eu.addicted2random.a2rclient.osc.Types;
import eu.addicted2random.a2rclient.utils.Range;

public class JSONLayoutParser {
  private static final Class<?>[] ARGUMENTS = { String.class, int.class, int.class, int.class, int.class };
  
  private final JSONObject json;
  private final Context context;
  private Layout layout = null;
  
  /**
   * Create a {@link JSONLayoutParser} instance form {@link JSONObject}.
   * @param in
   * @return
   */
  public JSONLayoutParser(Context context, JSONObject json) {
    this.context = context;
    this.json = json;
  }
  
  /**
   * Create a {@link JSONLayoutParser} instance form JSON string.
   * @param in
   * @return
   * @throws JSONException
   */
  public JSONLayoutParser(Context context, String json) throws JSONException {
    this(context, (JSONObject)new JSONTokener(json).nextValue());
  }
  
  /**
   * Create a {@link JSONLayoutParser} instance form JSON input stream.
   * @param in
   * @return
   * @throws IOException
   * @throws JSONException
   * @throws InvalidLayoutException
   */
  public JSONLayoutParser(Context context, InputStream in) throws IOException, JSONException, InvalidLayoutException {
    this.context = context;
    
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

    StringBuffer buffer = new StringBuffer();
    char[] buf = new char[1024];

    int numRead = 0;
    
    while((numRead = reader.read(buf)) != -1)
      buffer.append(buf, 0, numRead);
    
    String json = new String(buffer.toString());
    this.json = (JSONObject)new JSONTokener(json).nextValue();
  }
  
  /**
   * Create a {@link Route} entity from JSON.
   * 
   * @param address
   * @param signature
   * @return
   * @throws JSONException
   */
  private Route routeFromJSON(String address, JSONArray signature) throws JSONException {
    Type[] types = new Type[signature.length()];
    Object[] values = new Object[signature.length()];
    
    for(int i = 0; i < signature.length(); i++) {
      JSONObject o = signature.getJSONObject(i);
      
      String typeName = o.getString("type");
      Type type = Types.getTypeByName(typeName);
      
      String minimum = o.optString("minimum", null);
      String maximum = o.optString("maximum", null);
      String steps = o.optString("steps", null);
      
      if(minimum != null && maximum != null) {
        type = type.setRange(new Range(minimum, maximum, steps));
      }
      types[i] = type;
      
      if(o.has("default")) {
        Object def = o.get("default");
        if(type.canCast(def))
          values[i] = type.cast(def);
      }
    }
    
    Pack pack = new PackSupport(types, values);
    return new Route(address, pack);
  }
  
  /**
   * Create a {@link ServableRouteConnection} from JSON.
   * 
   * @param o
   * @return
   * @throws JSONException
   */
  private ServableRouteConnection servableRouteConnectionFromJSON(JSONObject o) throws JSONException {
    String address = o.optString("address", null);
    JSONObject map = o.optJSONObject("map");
    
    if(address != null && map != null) {
      Route route = layout.getRoute(address);
      
      if(route != null)  {
        Map<Integer, Integer> fromTo = new HashMap<Integer, Integer>(map.length());
        
        @SuppressWarnings("unchecked")
        Iterator<String> indices = map.keys();
        while(indices.hasNext()) {
          String name = indices.next();
          Integer from = Integer.valueOf(name);
          fromTo.put(from, map.getInt(name));
        }
        
        ServableRouteConnection connection = new ServableRouteConnection(fromTo);
        route.addServableRouteConnection(connection);
        return connection;
      }
    }
    return null;
  }
  
  /**
   * Create an {@link Element} entity from JSON.
   * 
   * @param section
   * @param e
   * @param i
   * @return
   * @throws JSONException
   */
  private Element<?> elementFromJSON(Section section, JSONObject e) throws JSONException {
    String type = e.getString("type");
    int x = e.getInt("x");
    int y = e.getInt("y");
    int cols = e.getInt("cols");
    int rows = e.getInt("rows");
    

    Element<?> element = null;
    
    String id = section.getId() + "." + String.valueOf(section.getElements().size());
    
    try {
      /* Element class name for type name */
      String className = String.format("%s.%sElement", Layout.class.getPackage().getName(), type);
      
      @SuppressWarnings("unchecked")
      Class<Element<?>> clz = (Class<Element<?>>) Class.forName(className);
      Constructor<Element<?>> constructor = clz.getConstructor(ARGUMENTS);
      element = constructor.newInstance(type, x, y, cols, rows);
      element.setId(id);
      
      @SuppressWarnings("unchecked")
      Iterator<String> iterator = (Iterator<String>)e.keys();
      while(iterator.hasNext()) {
        String property = iterator.next();
        
        if(!property.equals("id") && !property.equals("Id") && !property.equals("x") && !property.equals("y") && !property.equals("cols") && !property.equals("rows") && !property.equals("type")) {
          Object value = e.get(property);
          Element.trySet(element, property, value);
        }
        
      }
      
      if(element.getAddress() != null)
        layout.addRoute(new Route(element));
        
      if(e.has("outs")) {
        JSONArray outs = e.optJSONArray("outs");
        
        if(outs != null) {
          for(int i = 0; i < outs.length(); i++) {
            JSONObject out = outs.getJSONObject(i);
            
            ServableRouteConnection connection = servableRouteConnectionFromJSON(out);
                      
            if(connection != null)
              element.addServableRouteConnection(connection);
          }
        }
      }
    } catch (Throwable e1) {
      e1.printStackTrace();
      element = new SpaceElement(type, x, y, cols, rows);
      element.setId(id);
    }
    return element;
  }
  
  /**
   * Create a {@link Sensor} instance from JSON.
   * 
   * @param manager
   * @param type
   * @param s
   * @return
   * @throws JSONException
   */
  private Sensor sensorFromJSON(SensorManager manager, String type, JSONObject s) throws JSONException {
    Sensor sensor = new Sensor(type, manager);
    
    String address = s.optString("address", null);
    if(address != null) {
      sensor.setAddress(address);
      layout.addRoute(new Route(sensor));
    }
    
    if(s.has("outs")) {
      JSONArray outs = s.optJSONArray("outs");
      
      if(outs != null) {
        for(int i = 0; i < outs.length(); i++) {
          JSONObject out = outs.getJSONObject(i);
          
          ServableRouteConnection connection = servableRouteConnectionFromJSON(out);
          
          if(connection != null)
            sensor.addServableRouteConnection(connection);
        }
      }
    }
      
    return sensor;
  }
  
  /**
   * Create a {@link Layout} from JSON representation.
   * 
   * @return
   * @throws JSONException
   * @throws InvalidLayoutException
   */
  public Layout parse() throws JSONException, InvalidLayoutException {
    String name  = json.getString("name");
    String title = json.getString("title");
    
    if(name == null)
      throw new InvalidLayoutException("Layout property `name` can't be null");
    
    layout = new Layout(name, title);
    
    /* routes */
    JSONObject routes = json.optJSONObject("routes");
    
    if(routes != null) {
      @SuppressWarnings("unchecked")
      Iterator<String> addresses = routes.keys();
      
      while(addresses.hasNext()) {
        String address = addresses.next();
        layout.addRoute(routeFromJSON(address, routes.getJSONArray(address)));
      }
    }
    
    /* sections */
    JSONArray sections = json.getJSONArray("sections");
    
    for(int i = 0; i < sections.length(); i++) {
      JSONObject s = sections.getJSONObject(i);

      name = s.getString("name");
      title = s.getString("title");
      
      if(name == null)
        throw new InvalidLayoutException("Section property `name` can't be null");
      
      if(title == null)
        title = name;
      
      Section section = new Section(name, title);
      section.setId(layout.getId() + "." + String.valueOf(i));
      
      /* elements */
      JSONArray elements = s.getJSONArray("elements");
      
      for(int j = 0; j < elements.length(); j++) {
        JSONObject e = elements.getJSONObject(j);
        Element<?> element = elementFromJSON(section, e);
        Log.v("JSONLayoutParser", "loaded element " + element.getId());
        section.addElement(element);
      }
      layout.addSection(section);
    }
    
    /* sensors */
    JSONObject sensors = json.optJSONObject("sensors");
    if(sensors != null) {
      SensorManager manager = (SensorManager)this.context.getSystemService(Context.SENSOR_SERVICE);
      
      @SuppressWarnings("unchecked")
      Iterator<String> sensorTypes = sensors.keys();
      while(sensorTypes.hasNext()) {
        String type = sensorTypes.next();
        JSONObject s = sensors.optJSONObject(type);
        if(s != null) {
          Sensor sensor = sensorFromJSON(manager, type, s);
          if(sensor != null)
            layout.addSensor(sensor);
        }
      }
    }
    
    return layout;
  }
  

}
