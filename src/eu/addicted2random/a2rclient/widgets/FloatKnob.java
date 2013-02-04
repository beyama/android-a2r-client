package eu.addicted2random.a2rclient.widgets;

import eu.addicted2random.a2rclient.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

public class FloatKnob extends Knob<Float> {

  public FloatKnob(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public FloatKnob(Context context, AttributeSet attrs) {
    super(context, attrs);
    
    TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Knob, 0, 0);

    try {
      /* set properties from attribute set */
      String minimum = a.getString(R.styleable.Knob_minimum);
      String maximum = a.getString(R.styleable.Knob_maximum);
      String stepSize = a.getString(R.styleable.Knob_stepSize);
      
      if(minimum != null && maximum != null && stepSize != null) {
        setRange(Float.valueOf(minimum), Float.valueOf(maximum), Float.valueOf(stepSize));
        setValue(getRange().start);
      } else {
        setRange(0f, 10f, 0.1f);
      }

    } finally {
      a.recycle();
    }
  }

  public FloatKnob(Context context) {
    super(context);
    setRange(0f, 10f, 0.1f);
  }

  @SuppressLint("DefaultLocale")
  @Override
  public String getText() {
    return String.format("%." + getRange().step.scale() + "f %s", getValue(), getSuffix());
  }

}
