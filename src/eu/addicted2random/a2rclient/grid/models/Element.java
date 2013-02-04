package eu.addicted2random.a2rclient.grid.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.view.View;
import eu.addicted2random.a2rclient.GridFragment;

/**
 * Represents an element in a {@link GridFragment}.
 * 
 * @author Alexander Jentz
 *
 * @param <V>
 */
public abstract class Element<V extends View> implements Serializable {
  private static final long serialVersionUID = -3267800382148748457L;
  
  private String id;
  private int viewId;
  private final String type;
  private final int x;
  private final int y;
  private final int cols;
  private final int rows;
  private final Map<String, Object> options = new HashMap<String, Object>();

  public Element(String type, int x, int y, int cols, int rows) {
    super();
    this.type = type;
    this.x = x;
    this.y = y;
    this.cols = cols;
    this.rows = rows;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getViewId() {
    return viewId;
  }

  public void setViewId(int viewId) {
    this.viewId = viewId;
  }

  public String getType() {
    return type;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getCols() {
    return cols;
  }

  public int getRows() {
    return rows;
  }
  
  public Object getOption(Object key) {
    return options.get(key);
  }

  public boolean hasOptions() {
    return options.isEmpty();
  }
  
  public boolean hasOption(String key) {
    return options.containsKey(key);
  }

  public Set<String> getOptionskeySet() {
    return options.keySet();
  }

  public Object putOption(String key, Object value) {
    return options.put(key, value);
  }

  /**
   * Create a new view instance.
   * 
   * @param context
   * @return
   */
  public abstract V createInstance(Context context);
  
}
