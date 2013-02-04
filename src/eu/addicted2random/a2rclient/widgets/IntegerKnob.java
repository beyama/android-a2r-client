package eu.addicted2random.a2rclient.widgets;

import eu.addicted2random.a2rclient.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

public class IntegerKnob extends Knob<Integer> {

  public IntegerKnob(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public IntegerKnob(Context context, AttributeSet attrs) {
    super(context, attrs);
    
    TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Knob, 0, 0);

    try {
      /* set properties from attribute set */
      String minimum = a.getString(R.styleable.Knob_minimum);
      String maximum = a.getString(R.styleable.Knob_maximum);
      String stepSize = a.getString(R.styleable.Knob_stepSize);
      
      if(minimum != null && maximum != null && stepSize != null) {
        setRange(Integer.valueOf(minimum), Integer.valueOf(maximum), Integer.valueOf(stepSize));
        setValue(getRange().start);
      } else {
        setRange(0, 100, 1);
      }

    } finally {
      a.recycle();
    }
  }

  public IntegerKnob(Context context) {
    super(context);
    setRange(0, 100, 1);
  }
  
  @SuppressLint("DefaultLocale")
  public String getText() {
    return String.format("%d %s", getValue(), getSuffix());
  }

}
