package eu.addicted2random.a2rclient.widgets;

import java.math.BigDecimal;
import java.math.RoundingMode;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import eu.addicted2random.a2rclient.R;
import eu.addicted2random.a2rclient.Range;

/**
 * @author Alexander Jentz, beyama.de
 * 
 * <p>
 * <b>XML attributes</b>
 * </p>
 * See {@link eu.addicted2random.a2rclient.R.styleable#Knob Knob Attributes},
 * {@link android.R.styleable#View View Attributes}
 * 
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#Knob_minimum}
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#Knob_maximum}
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#Knob_step}
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#Knob_outlineColor}
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#Knob_sweepColor}
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#Knob_showValue}
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#Knob_suffix}
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#Knob_textSize}
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#Knob_showSteps}
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#Knob_stepLength}
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#Knob_strokeWidth}
 * 
 */
public class Knob extends View {

  public interface OnKnobChangeListener {
    public void onKnobChanged(Knob knob, BigDecimal value);
  }

  private static class SavedState extends BaseSavedState {
    BigDecimal value;

    SavedState(Parcelable superState) {
      super(superState);
    }
  }

  /* value range */
  private Range mRange = null;
  
  /* knob range */
  private Range mKnobRange = new Range(90, 360, 2700); // step size 0.1

  /* current value of the knob */
  private BigDecimal mValue = null;

  private Paint mTextPaint = null;

  private Paint mKnobPaint = null;

  private RectF mBounds = new RectF();

  private Rect mTextBounds = new Rect();

  /* sweep angle */
  private double mAngle = 90d;

  /* used to calculate the distance moved by touch move events */
  private float mLastTouchY;

  /* multiplier for move distance (moved distance * mMoveRatio) */
  private float mMoveRatio = 1f;

  private int mOutlineColor = Color.argb(0xff, 0xa9, 0xc0, 0x2b);

  private int mSweepColor = Color.argb(0xff, 0x34, 0x3a, 0x37);

  private boolean mShowSteps = false;

  /* length of visual step mark */
  private float mStepLength = 5.0f;

  /* show the text */
  private boolean mShowValue = true;

  private float mTextSize = 14.0f;

  private float mStrokeWidth = 4.0f;

  /* value suffix to sufix value for text output */
  private String mSuffix = "";
  
  private OnKnobChangeListener mOnKnobChangeListener = null;

  public Knob(Context context) {
    super(context);
    init();
  }

  public Knob(Context context, AttributeSet attrs) {
    super(context, attrs);

    TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Knob, 0, 0);

    /* set properties from attribute set */
    try {
      String suffix = a.getString(R.styleable.Knob_suffix);
      mSuffix = (suffix == null) ? mSuffix : suffix;

      mOutlineColor = a.getInteger(R.styleable.Knob_outlineColor, mOutlineColor);
      mSweepColor = a.getInteger(R.styleable.Knob_sweepColor, mSweepColor);
      mShowValue = a.getBoolean(R.styleable.Knob_showValue, mShowValue);
      mShowSteps = a.getBoolean(R.styleable.Knob_showSteps, mShowSteps);
      mStepLength = a.getFloat(R.styleable.Knob_stepLength, mStepLength);
      mTextSize = a.getFloat(R.styleable.Knob_textSize, mTextSize);
      mStrokeWidth = a.getFloat(R.styleable.Knob_strokeWidth, mStrokeWidth);
      
      
      String minimum = a.getString(R.styleable.Knob_minimum);
      String maximum = a.getString(R.styleable.Knob_maximum);
      String step = a.getString(R.styleable.Knob_step);
      
      if(minimum != null && maximum != null) {
        setRange(new Range(minimum, maximum, step));
      }
      
    } finally {
      a.recycle();
    }

    init();
  }

  public Knob(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  private void init() {
    mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mTextPaint.setDither(true);
    mTextPaint.setTextSize(mTextSize);
    mTextPaint.setColor(mSweepColor);
    mTextPaint.setSubpixelText(true);
    mTextPaint.setFakeBoldText(true);

    mKnobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mKnobPaint.setDither(true);
    mKnobPaint.setStrokeCap(Cap.SQUARE);
    mKnobPaint.setStyle(Style.STROKE);
    mKnobPaint.setStrokeWidth(mStrokeWidth);
    
    if(mRange == null)
      setRange(new Range(0, 100));
    
    if(mValue == null)
      setValue(mRange.start);

    validate();
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    calculateBoundaries(w, h);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    /* draw outline */
    mKnobPaint.setColor(mOutlineColor);
    
    canvas.drawArc(mBounds, 90, 360 - 90, false, mKnobPaint);

    /* draw the steps */
    if (mShowSteps && mRange.step != null) {
      int count = mRange.distance.divide(mRange.step, 0, RoundingMode.DOWN).intValue();
      
      float perStep = 270.0f / (float) count;

      float angle = -90.0f;

      /* count plus start */
      count += 1;

      for (int i = 0; i < count; i++) {
        canvas.save();

        if ((angle + 180.0f) <= mAngle) {
          mKnobPaint.setColor(mSweepColor);
        } else {
          mKnobPaint.setColor(mOutlineColor);
        }

        canvas.rotate(angle, mBounds.centerX(), mBounds.centerY());
        canvas.drawLine(mBounds.left, mBounds.centerY(), mBounds.left + 5.0f, mBounds.centerY(), mKnobPaint);

        canvas.restore();

        angle += perStep;
      }
    }

    /* draw the text */
    if (mShowValue) {
      String text = getText();
      mTextPaint.getTextBounds(text, 0, text.length(), mTextBounds);
      
      float x = mBounds.right - mTextBounds.width();
      float y = mBounds.bottom - ((mBounds.height() / 4f) - (mTextPaint.getTextSize()));
      
      canvas.drawText(text, x, y, mTextPaint);
    }

    /* draw the sweep */
    mKnobPaint.setColor(mSweepColor);
    canvas.drawArc(mBounds, 90, (float)mAngle - 90f, false, mKnobPaint);

    canvas.save();
    canvas.rotate((float)mAngle, mBounds.centerX(), mBounds.centerY());
    canvas.drawLine(mBounds.right, mBounds.centerY(), mBounds.centerX(), mBounds.centerY(), mKnobPaint);
    canvas.restore();
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {

    final int action = event.getActionMasked();

    switch (action) {
    case MotionEvent.ACTION_DOWN:
      mLastTouchY = event.getY(event.getActionIndex());
      getParent().requestDisallowInterceptTouchEvent(true);
      return true;
    case MotionEvent.ACTION_MOVE:
      final float y = event.getY(event.getActionIndex());

      // Calculate the distance moved, reverse sign and multiply with move ratio
      final float dy = ((y - mLastTouchY) * -1) * mMoveRatio;

      BigDecimal newValue = mRange.round(mValue.add(new BigDecimal(dy)));
      
      if(!newValue.equals(mValue)) {
        setRawValue(newValue, false);
        mLastTouchY = y;
      }
      return true;
    }
    return false;
  }

  protected void recalculateBoundaries() {
    calculateBoundaries(getWidth(), getHeight());
  }

  protected void calculateBoundaries(int w, int h) {
    /* Android draws the center of the stroke at the boundaries */
    float xpad = getPaddingLeft() + getPaddingRight() + mKnobPaint.getStrokeWidth();
    float ypad = getPaddingTop() + getPaddingBottom() + mKnobPaint.getStrokeWidth();

    float ww = (float) w - xpad;
    float hh = (float) h - ypad;

    float min = Math.min(ww, hh);

    mBounds.set((w / 2) - (min / 2), (h / 2) - (min / 2), (w / 2) + (min / 2), (h / 2) + (min / 2));
  }

  /**
   * Get formated text string.
   * 
   * @return
   */
  public String getText() {
    return String.format("%s %s", getValue().toPlainString(), mSuffix);
  }

  /**
   * Validate view properties.
   */
  protected void validate() {
  }

  @Override
  public Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();

    SavedState ss = new SavedState(superState);
    ss.value = mValue;

    return ss;
  }

  @Override
  public void onRestoreInstanceState(Parcelable state) {
    if (!(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }

    SavedState ss = (SavedState) state;
    super.onRestoreInstanceState(ss.getSuperState());

    setValue(ss.value);
  }
  
  public void setRange(Range range) {
    mRange = range;
    
    float step = mRange.step.floatValue();
    if(step < 0)
      step = step * -1;
    
    float divisor;
    
    if(step < 1)
      divisor = 6f;
    else
      divisor = 2f;
      
    mMoveRatio = step / divisor;
    
    if(mValue == null)
      mValue = mRange.start;
    
    invalidate();
  }
  
  public Range getRange() {
    return mRange;
  }

  /**
   * Get current value of knob.
   * 
   * @return
   */
  public BigDecimal getValue() {
    return mValue;
  }

  /**
   * Set current value of knob.
   * 
   * @param value
   * @param silent Call knob change listener?
   */
  public void setValue(BigDecimal value, boolean silent) {
    BigDecimal rounded = mRange.round(value);
    
    if(mValue.equals(rounded)) return;
    
    setRawValue(rounded, silent);
  }
  
  /**
   * Set current value of knob without rounding.
   * 
   * @param value
   * @param silent
   */
  protected void setRawValue(BigDecimal value, boolean silent) {
    mValue = value;
    
    mAngle = mKnobRange.scale(mRange, mValue).doubleValue();
    
    if (mOnKnobChangeListener != null && silent == false)
      mOnKnobChangeListener.onKnobChanged(this, mValue);

    invalidate();
  }
  
  /**
   * Set current value of knob.
   * 
   * @param value
   */
  public void setValue(BigDecimal value) {
    this.setValue(value, false);
  }

  /**
   * Get outline color.
   * 
   * @return
   */
  public int getOutlineColor() {
    return mOutlineColor;
  }

  /**
   * Set outline color.
   * 
   * @param outlineColor
   */
  public void setOutlineColor(int outlineColor) {
    this.mOutlineColor = outlineColor;
    invalidate();
  }

  /**
   * Get sweep color.
   * 
   * @return
   */
  public int getSweepColor() {
    return mSweepColor;
  }

  /**
   * Set sweep color
   * 
   * @param sweepColor
   */
  public void setSweepColor(int sweepColor) {
    this.mSweepColor = sweepColor;
    invalidate();
  }
  
  /**
   * Get show step.
   * @return
   */
  public boolean isShowSteps() {
    return mShowSteps;
  }
  
  /**
   * Set show steps.
   * @param show
   */
  public void setShowSteps(boolean show) {
    mShowSteps = show;
    invalidate();
  }

  /**
   * Get suffix for value output.
   * 
   * @return
   */
  public String getSuffix() {
    return mSuffix;
  }

  /**
   * Set suffix for value output.
   * 
   * @param suffix
   */
  public void setSuffix(String suffix) {
    mSuffix = suffix;
    invalidate();
  }

  /**
   * Is output of value with prefix is enabled?
   * 
   * @return
   */
  public boolean isShowValue() {
    return mShowValue;
  }

  /**
   * Enable or disable output of value with prefix.
   * 
   * @param show
   */
  public void setShowValue(boolean show) {
    mShowValue = show;
    invalidate();
  }

  /**
   * Get text size.
   * 
   * @return
   */
  public float getTextSize() {
    return mTextSize;
  }

  /**
   * Set text size.
   * 
   * @param size
   */
  public void setTextSize(float size) {
    mTextSize = size;
    mTextPaint.setTextSize(size);
    invalidate();
  }

  /**
   * Get the width for stroking.
   * 
   * @return
   */
  public float getStrokeWidth() {
    return mStrokeWidth;
  }

  /**
   * Set the width for stroking.
   * 
   * @param width
   */
  public void setStrokeWidth(float width) {
    mStrokeWidth = width;
    mKnobPaint.setStrokeWidth(width);
    recalculateBoundaries();
    invalidate();
  }

  public void setOnKnobChangeListener(OnKnobChangeListener onKnobChangeListener) {
    this.mOnKnobChangeListener = onKnobChangeListener;
  }

}
