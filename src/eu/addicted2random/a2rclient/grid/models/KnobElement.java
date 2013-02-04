package eu.addicted2random.a2rclient.grid.models;

import android.content.Context;
import eu.addicted2random.a2rclient.widgets.Knob;

public class KnobElement extends Element<Knob> {
  private static final long serialVersionUID = -196128424898301544L;

  public KnobElement(String type, int x, int y, int cols, int rows) {
    super(type, x, y, cols, rows);
  }

  @Override
  public Knob createInstance(Context context) {
   return new Knob(context);
  }
}
