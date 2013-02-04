package eu.addicted2random.a2rclient.grid.models;

import android.content.Context;
import eu.addicted2random.a2rclient.widgets.FloatKnob;
import eu.addicted2random.a2rclient.widgets.IntegerKnob;
import eu.addicted2random.a2rclient.widgets.Knob;

public class KnobElement extends Element<Knob<?>> {
  private static final long serialVersionUID = -196128424898301544L;

  static boolean hasFraction(Double value) {
    return (value - Math.round(value) != value);
  }
  
  public KnobElement(String type, int x, int y, int cols, int rows) {
    super(type, x, y, cols, rows);
  }

  @Override
  public Knob<?> createInstance(Context context) {
    if(hasOption("minimum") && hasOption("maximum") && hasOption("stepSize")) {
      Double min = ((Number)getOption("minimum")).doubleValue();
      Double max = ((Number)getOption("maximum")).doubleValue();
      Double stepSize = ((Number)getOption("stepSize")).doubleValue();
      
      if(hasFraction(min) || hasFraction(max) || hasFraction(stepSize)) {
        FloatKnob knob = new FloatKnob(context);
        knob.setRange(min.floatValue(), max.floatValue(), stepSize.floatValue());
        return knob;
      } else {
        IntegerKnob knob = new IntegerKnob(context);
        knob.setRange(min.intValue(), max.intValue(), stepSize.intValue());
        return knob;
      }
    }
    return new IntegerKnob(context);
  }
}
