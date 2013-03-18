package eu.addicted2random.a2rclient.grid;

import java.util.concurrent.locks.ReentrantLock;

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

  public SpaceElement(String type, int x, int y, int cols, int rows) {
    super(type, x, y, cols, rows);
  }

  @Override
  protected Space createInstance(Context context) {
    return new Space(context);
  }

  @Override
  protected void onSync() {
  }

  @Override
  protected Pack createPack(ReentrantLock lock) {
    return null;
  }

}
