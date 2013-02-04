package eu.addicted2random.a2rclient.grid.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


public class Layout implements Serializable {
  private static final long serialVersionUID = 5291560734856103190L;
  
  private static final Class<?>[] ARGUMENTS = { String.class, int.class, int.class, int.class, int.class };

  /**
   * Create a {@link Layout} instance form JSON input stream.
   * @param in
   * @return
   * @throws IOException
   * @throws JSONException
   * @throws InvalidLayoutException
   */
  static public Layout fromJSON(InputStream in) throws IOException, JSONException, InvalidLayoutException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

    StringBuffer buffer = new StringBuffer();
    char[] buf = new char[1024];

    int numRead = 0;
    
    while((numRead = reader.read(buf)) != -1)
      buffer.append(buf, 0, numRead);
    
    String json = new String(buffer.toString());
    return fromJSON(json);
  }
  
  /**
   * Create a {@link Layout} instance from JSON.
   * 
   * @param JSON string
   * @return
   * @throws JSONException
   * @throws InvalidLayoutException
   */
  static public Layout fromJSON(String json) throws JSONException, InvalidLayoutException {
    
    JSONObject object = (JSONObject)new JSONTokener(json).nextValue();
    
    String name  = object.getString("name");
    String title = object.getString("title");
    
    if(name == null)
      throw new InvalidLayoutException("Layout property `name` can't be null");
    
    Layout layout = new Layout(name, title);
    
    /* sections */
    JSONArray sections = object.getJSONArray("sections");
    
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
        
        String type = e.getString("type");
        int x = e.getInt("x");
        int y = e.getInt("y");
        int cols = e.getInt("cols");
        int rows = e.getInt("rows");
        

        Element<?> element = null;
        
        try {
          /* Element class name for type name */
          String className = String.format("%s.%sElement", Layout.class.getPackage().getName(), type);
          
          @SuppressWarnings("unchecked")
          Class<Element<?>> clz = (Class<Element<?>>) Class.forName(className);
          Constructor<Element<?>> constructor = clz.getConstructor(ARGUMENTS);
          element = constructor.newInstance(type, x, y, cols, rows);
          element.setId(section.getId() + "." + String.valueOf(j));
          
          @SuppressWarnings("unchecked")
          Iterator<String> iterator = (Iterator<String>)e.keys();
          while(iterator.hasNext()) {
            String property = iterator.next();
            
            if(!property.equals("id") && !property.equals("Id") && !property.equals("x") && !property.equals("y") && !property.equals("cols") && !property.equals("rows") && !property.equals("type")) {
              Object value = e.get(property);
              trySet(element, property, value);
            }
            
          }
          
        } catch (Throwable e1) {
          e1.printStackTrace();
          element = new SpaceElement(type, x, y, cols, rows);
        }
        
        section.addElement(element);
      }
      layout.addSection(section);
    }
    return layout;
  }
  
  private static boolean trySet(Element<?> element, String property, Object value) {
    String setter = String.format("set%s%s", Character.toUpperCase(property.charAt(0)), property.substring(1));

    for (Method m : element.getClass().getDeclaredMethods()) {
      Option option = m.getAnnotation(Option.class);

      try {
        boolean call = false;

        if (option != null) {
          if (option.value().length() > 0) {
            if (option.value().equals(property))
              call = true;
          } else if (m.getName().equals(setter)) {
            call = true;
          }
          if (call) {
            m.invoke(element, value);
            return true;
          }
        }
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  /* Layout name */
  private final String name;
  
  /* Layout title */
  private final String title;
  
  private final List<Section> sections = new LinkedList<Section>();

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
  
  /**
   * Add a {@link Section} to the sections list
   * @param section
   */
  public void addSection(Section section) {
    sections.add(section);
  }

}
