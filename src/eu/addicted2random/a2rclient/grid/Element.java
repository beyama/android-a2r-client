package eu.addicted2random.a2rclient.grid;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import eu.addicted2random.a2rclient.fragments.GridFragment;
import eu.addicted2random.a2rclient.osc.Pack;

/**
 * Represents an UI element in a {@link GridFragment} and acts as glue layer
 * between the OSC hub and the GUI element.
 * 
 * An instance of a concrete implementation of this class will be instantiated
 * by the JSON layout mapper for each element in the JSON layout file. All
 * properties from JSON are mapped to the element if a corresponding JSON
 * property annotation is found.
 * 
 * @author Alexander Jentz
 * 
 * @param <V>
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
		// space element
    @Type(value = SpaceElement.class, name = "Space"),
    // text element
    @Type(value = TextElement.class, name = "Text"),
    // knob element
    @Type(value = KnobElement.class, name = "Knob"),
    // adsr element
    @Type(value = ADSRElement.class, name = "ADSR"),
    // spinner element
    @Type(value = SpinnerElement.class, name = "Spinner"),
    // toggle button element
    @Type(value = ToggleButtonElement.class, name = "ToggleButton") })
public abstract class Element<V extends View> implements Servable, Pack.PackListener, Serializable, Runnable {
  private static final long serialVersionUID = -3267800382148748457L;

  private String id;
  private int viewId;
  private Pack pack;
  private V view;

  @JsonProperty(required = true)
  private String type;

  @JsonProperty(required = true)
  private final int x;

  @JsonProperty(required = true)
  private final int y;

  @JsonProperty(required = true)
  private final int cols;

  @JsonProperty(required = true)
  private final int rows;
  
  // The logical density of the display.
  private float density;

  private boolean synced = true;

  /**
   * Shouldn't this be obsolete?
   */
  @JsonProperty
  private String address = null;

  private List<ServableRouteConnection> connections = new LinkedList<ServableRouteConnection>();

  private Integer backgroundColor = null;
  
  @JsonProperty
  private Integer paddingTop = null;
  
  @JsonProperty
  private Integer paddingRight = null;
  
  @JsonProperty
  private Integer paddingBottom = null;
  
  @JsonProperty
  private Integer paddingLeft = null;

  @JsonProperty
  private Set<Out> outs = new HashSet<Out>();

  private Section section;

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
   * 
   * @return
   */
  public final String getId() {
    return id;
  }

  /**
   * Set id of element.
   * 
   * @param id
   */
  public final void setId(String id) {
    this.id = id;
  }

  /**
   * Get view id of element.
   * 
   * @return
   */
  public final int getViewId() {
    return viewId;
  }

  /**
   * Set view id of element.
   * 
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
   * 
   * @return
   */
  public final String getType() {
    return type;
  }

  /**
   * Get x position in grid.
   * 
   * @return
   */
  public final int getX() {
    return x;
  }

  /**
   * Get y position in grid.
   * 
   * @return
   */
  public final int getY() {
    return y;
  }

  /**
   * Get number of columns in grid.
   * 
   * @return
   */
  public final int getCols() {
    return cols;
  }

  /**
   * Get number of rows in grid.
   * 
   * @return
   */
  public final int getRows() {
    return rows;
  }

  /**
   * Get the logical density of the display.
   * @return
   */
  public float getDensity() {
		return density;
	}

	/**
   * Get OSC address.
   * 
   * @return
   */
  @Override
  public String getAddress() {
    return address;
  }

  /**
   * Set OSC address.
   * 
   * @param address
   */
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * Get {@link ServableRouteConnection} list.
   * 
   * @return
   */
  public List<ServableRouteConnection> getConnections() {
    return connections;
  }

  /**
   * Add an {@link ServableRouteConnection} to the connections list.
   * 
   * @param connection
   */
  public void addServableRouteConnection(ServableRouteConnection connection) {
    if (connections == null)
      connections = new LinkedList<ServableRouteConnection>();
    connection.setServable(this);
    connections.add(connection);
  }

  /**
   * Set background color of view.
   * 
   * @param color
   */
  @JsonProperty
  public void setBackgroudColor(String color) {
    backgroundColor = Color.parseColor(color);
  }

  /**
   * Get top padding of view.
   * 
   * @return
   */
  public Integer getPaddingTop() {
		return paddingTop;
	}

  /**
   * Set top padding of view.
   * 
   * @param paddingTop
   */
	public void setPaddingTop(Integer paddingTop) {
		this.paddingTop = paddingTop;
	}

	/**
	 * Get right padding of view.
	 * 
	 * @return
	 */
	public Integer getPaddingRight() {
		return paddingRight;
	}

	/**
	 * Set right padding of view.
	 * 
	 * @param paddingRight
	 */
	public void setPaddingRight(Integer paddingRight) {
		this.paddingRight = paddingRight;
	}

	/**
	 * Get bottom padding of view.
	 * 
	 * @return
	 */
	public Integer getPaddingBottom() {
		return paddingBottom;
	}

	/**
	 * Set bottom padding of view.
	 * 
	 * @param paddingBottom
	 */
	public void setPaddingBottom(Integer paddingBottom) {
		this.paddingBottom = paddingBottom;
	}

	/**
	 * Get left padding of view.
	 * 
	 * @return
	 */
	public Integer getPaddingLeft() {
		return paddingLeft;
	}

	/**
	 * Set left padding of view.
	 * 
	 * @param paddingLeft
	 */
	public void setPaddingLeft(Integer paddingLeft) {
		this.paddingLeft = paddingLeft;
	}
	
	@JsonProperty
	public void setPadding(Integer padding) {
		paddingLeft = paddingTop = paddingRight = paddingBottom = padding;
	}

	public Set<Out> getOuts() {
    return outs;
  }

  public void setOuts(Set<Out> outs) {
    this.outs = outs;
  }

  /**
   * Get section.
   * 
   * @return
   */
  public Section getSection() {
    return section;
  }

  /**
   * Set section.
   * 
   * @param section
   */
  @JsonBackReference
  public void setSection(Section section) {
    this.section = section;
  }

  /**
   * Setup view. Override this to set custom options.
   * 
   * @param view
   */
  protected void setupView() {
    View view = getView();

    if (backgroundColor != null)
      view.setBackgroundColor(backgroundColor);
    
    int left   = paddingLeft  == null ? 0 : Math.round(density * paddingLeft),
    		top    = paddingTop   == null ? 0 : Math.round(density * paddingTop),
    		right  = paddingRight == null ? 0 : Math.round(density * paddingRight),
    		bottom = paddingTop   == null ? 0 : Math.round(density * paddingBottom);
    
    view.setPadding(left, top, right, bottom);
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
   * @param lock
   *          Lock to synchronize access
   * @return
   */
  protected abstract Pack createPack(ReentrantLock lock);

  /**
   * Create a new {@link View} instance.
   * 
   * @param context
   * @return
   */
  public V newInstance(Context context) {
  	density = context.getResources().getDisplayMetrics().density;
    
  	V view = createInstance(context);
    setView(view);
    setupView();
    if (!synced)
      view.post(this);
    return view;
  }

  /**
   * Get element pack.
   * 
   * @return
   */
  @Override
  public Pack getPack() {
    if (pack == null) {
      pack = createPack(getSection().getLayout().getLock());
      pack.addPackListener(this);
    }
    return pack;
  }

  /**
   * Sets view and pack to null.
   */
  public void dispose() {
    resetView();

    if (pack != null) {
      pack.removePackListener(this);
      pack = null;
    }
  }

  /**
   * Call {@link Element#onResetView()} and set view reference to null.
   */
  public void resetView() {
    if (view != null) {
      onResetView();
      view = null;
    }
  }

  /**
   * Called before view is set to null.
   * 
   * Override this to disconnect grid elements from event handlers.
   * 
   * @param view
   */
  protected void onResetView() {
  }

  @Override
  public void onPacked(Pack source) {
  }

  @Override
  public synchronized void onValueChanged(Pack source, Object actor, int index, Object oldValue, Object newValue) {
    if (actor == view)
      return;

    synced = false;
    if (view != null)
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
