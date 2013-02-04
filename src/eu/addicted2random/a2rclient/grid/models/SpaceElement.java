package eu.addicted2random.a2rclient.grid.models;

import android.content.Context;
import android.support.v7.widget.Space;

public class SpaceElement extends Element<Space> {
  private static final long serialVersionUID = 2048688883770493728L;

  public SpaceElement(String type, int x, int y, int cols, int rows) {
    super(type, x, y, cols, rows);
  }

  @Override
  public Space createInstance(Context context) {
    return new Space(context);
  }

}
