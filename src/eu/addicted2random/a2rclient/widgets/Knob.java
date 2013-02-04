package eu.addicted2random.a2rclient.widgets;

import java.math.BigDecimal;

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
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#Knob_stepSize}
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
public abstract class Knob<T extends Number> extends View {

  public interface OnKnobChangeListener<T extends Number> {
    public void onKnobChanged(Knob<T> knob, T value);
  }

  private static class SavedState extends BaseSavedState {
    BigDecimal value;

    SavedState(Parcelable superState) {
      super(superState);
    }
  }

  /* value range */
  private Range<T> mRange = null;
  
  /* knob range */
  private Range<Double> mKnobRange = new Range<Double>(90d, 360d, 0.1d);

  /* current value of the knob */
  private BigDecimal mValue = null;

  private Paint mTextPaint = null;

  private Paint mKnobPaint = null;

  private RectF mBounds = new RectF();

  private Rect mTextBounds = new Rect();

  /* incremented or decremented by touch moves until a full step is reached */
  private double mAngle = 90d;

  /* the angle of the sweep */
  private double mCurrentAngle = 90d;

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

  private OnKnobChangeListener<T> mOnKnobChangeListener = null;

  public Knob(Context context) {
    super(context);
    init();
  }

  public Knob(Context context, AttributeSet attrs) {
    super(context, attrs);

    TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Knob, 0, 0);

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
    if (mShowSteps) {
      int count = mRange.stepCount.intValue();
      float perStep = 270.0f / (float) count;

      float angle = -90.0f;

      /* count plus start */
      count += 1;

      for (int i = 0; i < count; i++) {
        canvas.save();

        if ((angle + 180.0f) <= mCurrentAngle) {
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
    canvas.drawArc(mBounds, 90, (float)mCurrentAngle - 90f, false, mKnobPaint);

    canvas.save();
    canvas.rotate((float)mCurrentAngle, mBounds.centerX(), mBounds.centerY());
    canvas.drawLine(mBounds.right, mBounds.centerY(), mBounds.centerX(), mBounds.centerY(), mKnobPaint);
    canvas.restore();
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {

    final int action = event.getActionMasked();

    boolean handled = false;

    switch (action) {
    case MotionEvent.ACTION_DOWN:
      mLastTouchY = event.getY(event.getActionIndex());
      getParent().requestDisallowInterceptTouchEvent(true);
      handled = true;
      break;
    case MotionEvent.ACTION_MOVE:
      final float y = event.getY(event.getActionIndex());

      // Calculate the distance moved and reverse sign
      final float dy = (y - mLastTouchY) * -1;

      mLastTouchY = y;

      updateAngle(dy * mMoveRatio);
      handled = true;
    }

    return handled;
  }

  /**
   * Update the sweep angle.
   * 
   * This is internally used to update the value by setting the angle of the
   * sweep.
   * 
   * @param by
   *          Value to add to the angle
   */
  protected void updateAngle(float by) {
    T newValue;

    /* boundary check */
    if (mAngle >= 90 && mAngle < 360.0 && by > 0) {
      mAngle += by;
      if (mAngle > 360)
        mAngle = 360;
    } else if (mAngle >= 90 && by < 0) {
      mAngle += by;
      if (mAngle < 90)
        mAngle = 90;
    } else {
      return;
    }

    /* scale from angle range to value range */
    newValue = mRange.scale(mKnobRange, mAngle);

    setValue(newValue);
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
  public abstract String getText();

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

  /**
   * Set range.
   * 
   * @param minimum
   *          Minimum value
   * @param maximum
   *          Maximum value
   * @param stepSize
   *          Step size
   */
  public void setRange(T start, T end, T step) {
    this.setRange(new Range<T>(start, end, step));
  }
  
  public void setRange(Range<T> range) {
    mRange = range;
    
    mMoveRatio = 270f / mRange.stepCount.floatValue();

    if (mMoveRatio > 3f)
      mMoveRatio = 3f;
    else if (mMoveRatio < 0.2f)
      mMoveRatio = 0.2f;
    
    if(mValue == null)
      mValue = mRange.start;
    
    invalidate();
  }
  
  public Range<T> getRange() {
    return mRange;
  }

  /**
   * Get current value of knob.
   * 
   * @return
   */
  public T getValue() {
    return mRange.cast(mValue);
  }

  /**
   * Set current value of knob.
   * 
   * @param value
   */
  public void setValue(BigDecimal value) {
    BigDecimal rounded = mRange.round(value);
    
    if(mValue.equals(rounded)) return;
    
    mValue = rounded;
    
    mCurrentAngle = mKnobRange.scale(mRange, mValue).doubleValue();
    mAngle = mCurrentAngle;

    if (mOnKnobChangeListener != null)
      mOnKnobChangeListener.onKnobChanged(this, mRange.cast(mValue));

    invalidate();
  }
  
  public void setValue(T value) {
    setValue(Range.valueOf(value));
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

  public void setOnKnobChangeListener(OnKnobChangeListener<T> onKnobChangeListener) {
    this.mOnKnobChangeListener = onKnobChangeListener;
  }

}
