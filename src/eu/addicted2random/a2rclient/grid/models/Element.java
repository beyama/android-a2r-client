package eu.addicted2random.a2rclient.grid.models;

import java.io.Serializable;

import android.content.Context;
import android.graphics.Color;
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
   * Set background color of view.
   * @param color
   */
  @Option
  public void setBackgroudColor(String color) {
    backgroundColor = Color.parseColor(color);
  }
  
  /**
   * Setup view. Overwrite this to set custom options.
   * @param view
   */
  public void setupView(View view) {
    if(backgroundColor != -1)
      view.setBackgroundColor(backgroundColor);
  }

  /**
   * Create a new view instance.
   * 
   * @param context
   * @return
   */
  public abstract V createInstance(Context context);
  
}
