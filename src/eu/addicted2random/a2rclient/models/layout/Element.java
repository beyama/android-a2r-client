package eu.addicted2random.a2rclient.models.layout;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import eu.addicted2random.a2rclient.fragments.GridFragment;
import eu.addicted2random.a2rclient.osc.Pack;

/**
 * Represents an element in a {@link GridFragment}.
 * 
 * @author Alexander Jentz
 *
 * @param <V>
 */
public abstract class Element<V extends View> implements Pack.PackListener, Serializable, Runnable {
  private static final long serialVersionUID = -3267800382148748457L;
  
  /**
   * Utility method. Tries to set a with {@link Option} annotated property.
   * 
   * @param element
   * @param property
   * @param value
   * @return
   */
  public static boolean trySet(Element<?> element, String property, Object value) {
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
      }
    }
    return false;
  }
  
  private String id;
  private int viewId;
  private Pack pack;
  private V view;
  private final String type;
  private final int x;
  private final int y;
  private final int cols;
  private final int rows;
  private boolean synced = true;
  private List<ElementRouteConnection> connections;
  
  private int backgroundColor = -1;

  public Element(String type, int x, int y, int cols, int rows) {
    super();
    this.type = type;
    this.x = x;
    this.y = y;
    this.cols = cols;
    this.rows = rows;
  }

  /**
   * Get id of element.
   * @return
   */
  public final String getId() {
    return id;
  }

  /**
   * Set id of element.
   * @param id
   */
  public final void setId(String id) {
    this.id = id;
  }

  /**
   * Get view id of element.
   * @return
   */
  public final int getViewId() {
    return viewId;
  }

  /**
   * Set view id of element.
   * @param viewId
   */
  public final void setViewId(int viewId) {
    this.viewId = viewId;
  }

  /**
   * Get the view instance for this element.
   * 
   * @return
   */
  public V getView() {
    return view;
  }

  /**
   * Set the current view on {@link Element#createInstance(Context) }.
   * 
   * @param view
   */
  private void setView(V view) {
    this.view = view;
  }
  
  /**
   * Get type name of element.
   * @return
   */
  public final String getType() {
    return type;
  }

  /**
   * Get x position in grid.
   * @return
   */
  public final int getX() {
    return x;
  }

  /**
   * Get y position in grid.
   * @return
   */
  public final int getY() {
    return y;
  }

  /**
   * Get number of columns in grid.
   * @return
   */
  public final int getCols() {
    return cols;
  }

  /**
   * Get number of rows in grid.
   * @return
   */
  public final int getRows() {
    return rows;
  }
  
  /**
   * Get {@link ElementRouteConnection} list.
   * @return
   */
  public List<ElementRouteConnection> getConnections() {
    return connections;
  }
  
  /**
   * Add an {@link ElementRouteConnection} to the connections list.
   * 
   * @param connection
   */
  public void addElementRouteConnection(ElementRouteConnection connection) {
    if(connections == null) connections = new LinkedList<ElementRouteConnection>();
    connection.setElement(this);
    connections.add(connection);
  }

  /**
   * Set background color of view.
   * @param color
   */
  @Option
  public void setBackgroudColor(String color) {
    backgroundColor = Color.parseColor(color);
  }
  
  /**
   * Setup view. Override this to set custom options.
   * @param view
   */
  protected void setupView(View view) {
    if(backgroundColor != -1)
      view.setBackgroundColor(backgroundColor);
  }

  /**
   * Create a new {@link View} instance.
   * 
   * @param context
   * @return
   */
  protected abstract V createInstance(Context context);
  
  /**
   * Create a new {@link Pack} instance.
   * 
   * @return
   */
  protected abstract Pack createPack();
  
  /**
   * Create a new {@link View} instance.
   * 
   * @param context
   * @return
   */
  public V newInstance(Context context) {
    V view = createInstance(context);
    setView(view);
    setupView(view);
    if(!synced)
      view.post(this);
    return view;
  }
  
  /**
   * Get element pack.
   * @return
   */
  public Pack getPack() {
    if(pack == null) {
      pack = createPack();
      pack.addPackListener(this);
    }
    return pack;
  }

  /**
   * Sets view and pack to null.
   */
  public void dispose() {
    resetView();
    
    if(pack != null) {
      pack.removePackListener(this);
      pack = null;
    }
  }
  
  /**
   * Destroy view.
   */
  public void resetView() {
    if(view != null) {
      onResetView(view);
      view = null;
    }
  }
  
  /**
   * Called before view is set to null.
   * 
   * Override this to disconnect grid elements
   * from event handlers.
   * 
   * @param view
   */
  protected void onResetView(View view) {
  }

  @Override
  public void onPacked(Pack source) {
  }
  
  @Override
  public synchronized void onValueChanged(Pack source, Object actor, int index, Object oldValue, Object newValue) {
    if(actor == view) return;
    
    synced = false;
    if(view != null)
      view.post(this);
  }
  
  /**
   * Calls onSync in the UI thread.
   */
  @Override
  public void run() {
    getPack().lock(this);
    try {
      onSync();
    } finally {
      synced = true;
      getPack().unlock();
    }
  }
  
  /**
   * Sync back values from pack.
   */
  abstract protected void onSync();
  
}
