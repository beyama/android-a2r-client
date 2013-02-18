package eu.addicted2random.a2rclient.grid;

import android.content.Context;
import android.support.v7.widget.Space;
import eu.addicted2random.a2rclient.osc.Pack;

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
  protected Pack createPack() {
    return null;
  }

}
