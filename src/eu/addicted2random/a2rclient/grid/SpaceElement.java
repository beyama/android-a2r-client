package eu.addicted2random.a2rclient.grid;

import java.util.concurrent.locks.ReentrantLock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import android.content.Context;
import android.support.v7.widget.Space;
import eu.addicted2random.a2rclient.osc.Pack;

/**
 * An element that simply creates a {@link Space} in the grid.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class SpaceElement extends Element<Space> {
  private static final long serialVersionUID = 2048688883770493728L;

  @JsonCreator
  public SpaceElement(@JsonProperty("type") String type, @JsonProperty("x") int x, @JsonProperty("y") int y,
      @JsonProperty("cols") int cols, @JsonProperty("rows") int rows) {
    super(type, x, y, cols, rows);
  }

  @Override
  protected Space onCreateView(Context context) {
    return new Space(context);
  }

  @Override
  protected void onSync() {
  }

  @Override
  protected Pack onCreatePack(ReentrantLock lock) {
    return null;
  }

}
