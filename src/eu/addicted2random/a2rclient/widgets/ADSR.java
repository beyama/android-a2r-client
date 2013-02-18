package eu.addicted2random.a2rclient.widgets;

import java.math.BigDecimal;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.PointF;
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
 * See {@link eu.addicted2random.a2rclient.R.styleable#ADSR ADSR Attributes},
 * {@link android.R.styleable#View View Attributes}
 * 
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#ADSR_minimum}
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#ADSR_maximum}
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#ADSR_step}
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#ADSR_attackColor}
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#ADSR_decayColor}
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#ADSR_sustainColor}
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#ADSR_releaseColor}
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#ADSR_textSize}
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#ADSR_showValue}
 * @attr ref {@link eu.addicted2random.a2rclient.R.styleable#ADSR_strokeWidth}
 * 
 */
public class ADSR extends View {

	public enum Parameter { ATTACK, DECAY, SUSTAIN, RELEASE }
	
	public interface OnADSRChangeListener {
		public void onADSRParameterChanged(ADSR view, Parameter param, BigDecimal value);
	}
	
	private static class SavedState extends BaseSavedState {
	  Range range;
		BigDecimal attack;
		BigDecimal decay;
		BigDecimal sustain;
		BigDecimal release;
		
		SavedState(Parcelable superState) {
			super(superState);
		}
	}
	
	// value range
	private Range mRange;

	// attack value
	private BigDecimal mAttack;

	// decay value
	private BigDecimal mDecay;

	// sustain value
	private BigDecimal mSustain;

	// release value
	private BigDecimal mRelease;

	private int mAttackColor = Color.argb(0xff, 0xd4, 0x00, 0x00);

	private int mDecayColor = Color.argb(0xff, 0xa9, 0xc0, 0x2b);

	private int mSustainColor = Color.argb(0xff, 0xff, 0xff, 0x00);

	private int mReleaseColor = Color.argb(0xff, 0xf4, 0x48, 0x00);

	private RectF mAttackBounds = new RectF();

	private RectF mDecayBounds = new RectF();

	private RectF mSustainBounds = new RectF();

	private RectF mReleaseBounds = new RectF();
	
	private Range mAttackBoundsRange = null;

  private Range mDecayBoundsRange = null;

  private Range mSustainBoundsRange = null;

  private Range mReleaseBoundsRange = null;

	private PointF mAttackDecayVertex = new PointF();

	private PointF mDecaySustainVertex = new PointF();

	private PointF mSustainReleaseVertex = new PointF();

	private PointF mReleaseEnd = new PointF();

	private Paint mBorder;

	private Paint mPaint;
	
	private Paint mTextPaint;

	private RectF mBounds = new RectF();

	private float mLastTouchY;
	
	private float mYUpdate;
	
	private Parameter mZone;
	
	private boolean mOnTouch = false;
	
	private boolean mShowValue = true;
	
	private float mTextSize = 12f;
	
	private float mStrokeWidth = 4f;
	
	private OnADSRChangeListener mOnADSRChangeListener = null;

	public ADSR(Context context) {
		super(context);
		init();
	}

	public ADSR(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ADSR, 0, 0);
		
		/* set properties from attribute set */
    try {
      mShowValue   = a.getBoolean(R.styleable.ADSR_showValue, mShowValue);
      mTextSize    = a.getFloat(R.styleable.ADSR_textSize, mTextSize);
      mStrokeWidth = a.getFloat(R.styleable.ADSR_strokeWidth, mStrokeWidth);
      
      mAttackColor  = a.getInt(R.styleable.ADSR_attackColor, mAttackColor);
      mDecayColor   = a.getInt(R.styleable.ADSR_decayColor, mDecayColor);
      mSustainColor = a.getInt(R.styleable.ADSR_sustainColor, mSustainColor);
      mReleaseColor = a.getInt(R.styleable.ADSR_releaseColor, mReleaseColor);
      
      String minimum = a.getString(R.styleable.ADSR_minimum);
      String maximum = a.getString(R.styleable.ADSR_maximum);
      String step    = a.getString(R.styleable.ADSR_step);
      
      if(minimum != null && maximum != null) {
        setRange(new Range(minimum, maximum, step));
      }
      
    } finally {
      a.recycle();
    }
		
		init();
	}

	public ADSR(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	protected void init() {
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setDither(true);
		mPaint.setStrokeCap(Cap.ROUND);
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeWidth(mStrokeWidth);

		mBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBorder.setStrokeWidth(0);
		mBorder.setStyle(Style.STROKE);
		mBorder.setColor(Color.LTGRAY);
		
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextSize(mTextSize);
		mTextPaint.setColor(Color.WHITE);
		
		if(mRange == null)
		  setRange(new Range(0, 100));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		mBounds.set(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(),
				(h - getPaddingBottom()));

		float sw = mBorder.getStrokeWidth();

		sw = (sw == 0f) ? 1.0f : sw;

		/**
		 * Zone width is bounds width - (2 * border width) / 4
		 */
		float zw = (mBounds.width() - (2f * sw)) / 4f;

		/**
		 * Zone top is bounds top + border width;
		 */
		float zt = mBounds.top + sw;

		/**
		 * Zone bottom is bounds bottom - border width;
		 */
		float zb = mBounds.bottom - sw;

		// parameter zone boundaries
		mAttackBounds.set(mBounds.left + sw, zt, mBounds.left + zw, zb);
		mDecayBounds.set(mAttackBounds.right, zt, mAttackBounds.right + zw, zb);
		mSustainBounds.set(mDecayBounds.right, zt, mDecayBounds.right + zw, zb);
		mReleaseBounds.set(mSustainBounds.right, zt, mSustainBounds.right + zw, zb);

		// parameter zone ranges
		mAttackBoundsRange  = new Range(mAttackBounds.left, mAttackBounds.right);
		mDecayBoundsRange   = new Range(mDecayBounds.left, mDecayBounds.right);
		mSustainBoundsRange = new Range(mSustainBounds.bottom, mSustainBounds.top);
		mReleaseBoundsRange = new Range(mReleaseBounds.left, mReleaseBounds.right);
		
		// vertex
		mAttackDecayVertex.y = mAttackBounds.top;
		mSustainReleaseVertex.x = mSustainBounds.right;
		mReleaseEnd.y = mReleaseBounds.bottom;
		
		set(mAttack, mDecay, mSustain, mRelease, true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		/* draw vertical lines */
		if(mOnTouch) {
			float top = mBounds.top;
			float bottom = mBounds.bottom;
			canvas.drawLine(mAttackBounds.right, top, mAttackBounds.right, bottom,
					mBorder);
			canvas.drawLine(mDecayBounds.right, top, mDecayBounds.right, bottom,
					mBorder);
			canvas.drawLine(mSustainBounds.right, top, mSustainBounds.right,
					bottom, mBorder);
		}

		/* draw attack */
		mPaint.setColor(mAttackColor);
		canvas.drawLine(mAttackBounds.left, mAttackBounds.bottom,
				mAttackDecayVertex.x, mAttackDecayVertex.y, mPaint);

		/* draw decay */
		mPaint.setColor(mDecayColor);
		canvas.drawLine(mAttackDecayVertex.x, mAttackDecayVertex.y,
				mDecaySustainVertex.x, mDecaySustainVertex.y, mPaint);

		/* draw sustain */
		mPaint.setColor(mSustainColor);
		canvas.drawLine(mDecaySustainVertex.x, mDecaySustainVertex.y,
				mSustainReleaseVertex.x, mSustainReleaseVertex.y, mPaint);

		/* draw release */
		mPaint.setColor(mReleaseColor);
		canvas.drawLine(mSustainReleaseVertex.x, mSustainReleaseVertex.y,
				mReleaseEnd.x, mReleaseEnd.y, mPaint);
		
		
		if(mShowValue) {
  		canvas.drawText(mAttack.toPlainString(), mAttackBounds.left + 5f, mAttackBounds.bottom - 5f, mTextPaint);
  		canvas.drawText(mDecay.toPlainString(), mDecayBounds.left + 5f, mDecayBounds.bottom - 5f, mTextPaint);
  		canvas.drawText(mSustain.toPlainString(), mSustainBounds.left + 5f, mSustainBounds.bottom - 5f, mTextPaint);
  		canvas.drawText(mRelease.toPlainString(), mReleaseBounds.left + 5f, mReleaseBounds.bottom - 5f, mTextPaint);
		}

		/* draw border */
		if(mOnTouch) {
			canvas.drawLine(mBounds.left, mBounds.bottom, mBounds.right, mBounds.bottom, mBorder);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		final int action = event.getActionMasked();
		final float x = event.getX(event.getActionIndex());
		final float y = event.getY(event.getActionIndex());
		
		boolean handled = false;
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (x >= mAttackBounds.left && x <= mAttackBounds.right) {
				mZone = Parameter.ATTACK;
			} else if (x >= mDecayBounds.left && x <= mDecayBounds.right) {
				mZone = Parameter.DECAY;
			} else if (x >= mSustainBounds.left && x <= mSustainBounds.right) {
				mZone = Parameter.SUSTAIN;
			} else if (x >= mReleaseBounds.left && x <= mReleaseBounds.right) {
				mZone = Parameter.RELEASE;
			} else {
				return true;
			}
			getParent().requestDisallowInterceptTouchEvent(true);

			mLastTouchY = y;
			mYUpdate = 0.0f;
			
			mOnTouch = true;
			invalidate();

			handled = true;
			break;
		case MotionEvent.ACTION_UP:
			if(mZone == null) {
				handled = false;
				break;
			}
			
			mZone = null;
			mOnTouch = false;
			
			invalidate();
			handled = true;
			break;
		case MotionEvent.ACTION_MOVE:
			if(mZone == null) {
				handled = false;
				break;
			}
			
			float dy = y - mLastTouchY;
			
			dy *= -1;

			/* direction changed? */
			if(mYUpdate < 0f && dy > 0f)
				mYUpdate = 0f;
			else if(mYUpdate > 0f && dy < 0f)
				mYUpdate = 0f;
			
			mYUpdate += dy;
			
			switch (mZone) {
			case ATTACK:
				if((mYUpdate < 0 ? mYUpdate * -1 : mYUpdate) > 2f) {
					setAttack(mAttack.add(new BigDecimal(mYUpdate / 2f)));
					mYUpdate = 0f;
				}
				break;
			case DECAY:
				if((mYUpdate < 0 ? mYUpdate * -1 : mYUpdate) > 2f) {
					setDecay(mDecay.add(new BigDecimal(mYUpdate / 2f)));
					mYUpdate = 0f;
				}
				break;
			case SUSTAIN:
				if((mYUpdate < 0 ? mYUpdate * -1 : mYUpdate) > 3f) {
					setSustain(mSustain.add(new BigDecimal(mYUpdate / 3f)));
					mYUpdate = 0f;
				}
				break;
			case RELEASE:
				if((mYUpdate < 0 ? mYUpdate * -1 : mYUpdate) > 2f) {
					setRelease(mRelease.add(new BigDecimal(mYUpdate / 2f)));
					mYUpdate = 0f;
				}
				break;
			}
			mLastTouchY = y;
			handled = true;
			break;
		}

		return handled;
	}
	
	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		SavedState ss = new SavedState(superState);
		ss.range   = mRange;
		ss.attack  = mAttack;
		ss.decay   = mDecay;
		ss.sustain = mSustain;
		ss.release = mRelease;
		
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
		
		mRange   = ss.range;
		mAttack  = ss.attack;
		mDecay   = ss.decay;
		mSustain = ss.sustain;
		mRelease = ss.release;
	}

  public void setAttack(BigDecimal value, boolean silent) {
    mAttack = mRange.round(value);
    
		mAttackDecayVertex.x = mAttackBoundsRange.scale(mRange, mAttack).floatValue();
				
		if(!silent && mOnADSRChangeListener != null)
			mOnADSRChangeListener.onADSRParameterChanged(this, Parameter.ATTACK, mAttack);
		
		invalidate();
	}
	
	public void setAttack(BigDecimal value) {
		setAttack(value, false);
	}
	
	public BigDecimal getAttack() {
	  return mAttack;
	}

	public void setDecay(BigDecimal value, boolean silent) {
	  mDecay = mRange.round(value);

		mDecaySustainVertex.x = mDecayBoundsRange.scale(mRange, mDecay).floatValue();
		
		if(!silent && mOnADSRChangeListener != null)
			mOnADSRChangeListener.onADSRParameterChanged(this, Parameter.DECAY, mDecay);
		
		invalidate();
	}
	
	public void setDecay(BigDecimal value) {
		setDecay(value, false);
	}
	
	 public BigDecimal getDecay() {
	   return mDecay;
	 }

	public void setSustain(BigDecimal value, boolean silent) {
	  mSustain = mRange.round(value);
	  
		mSustainReleaseVertex.y = mSustainBoundsRange.scale(mRange, mSustain).floatValue();
		mDecaySustainVertex.y = mSustainReleaseVertex.y;
		
		if(!silent && mOnADSRChangeListener != null)
			mOnADSRChangeListener.onADSRParameterChanged(this, Parameter.SUSTAIN, mSustain);
		
		invalidate();
	}
	
	public void setSustain(BigDecimal value) {
		setSustain(value, false);
	}
	
	public BigDecimal getSustain() {
	  return mSustain;
	}

	public void setRelease(BigDecimal value, boolean silent) {
	  mRelease = mRange.round(value);

		mReleaseEnd.x = mReleaseBoundsRange.scale(mRange, mRelease).floatValue();
		
		if(!silent && mOnADSRChangeListener != null)
			mOnADSRChangeListener.onADSRParameterChanged(this, Parameter.RELEASE, mRelease);
		
		invalidate();
	}
	
	public void setRelease(BigDecimal value) {
		setRelease(value, false);
	}
	
	public BigDecimal getRelease() {
	  return mRelease;
	}
	
	public void set(BigDecimal attack, BigDecimal decay, BigDecimal sustain, BigDecimal release, boolean silent) {
		setAttack(attack, silent);
		setDecay(decay, silent);
		setSustain(sustain, silent);
		setRelease(release, silent);
	}
	
	public void set(BigDecimal attack, BigDecimal decay, BigDecimal sustain, BigDecimal release) {
		set(attack, decay, sustain, release, false);
	}
	
	/**
	 * Get attack color.
	 * @return
	 */
	public int getAttackColor() {
    return mAttackColor;
  }

	/**
	 * Set attack color.
	 * @param attackColor
	 */
  public void setAttackColor(int attackColor) {
    this.mAttackColor = attackColor;
    invalidate();
  }

  /**
   * Get decay color.
   * @return
   */
  public int getDecayColor() {
    return mDecayColor;
  }

  /**
   * Set decay color.
   * @param mDecayColor
   */
  public void setDecayColor(int decayColor) {
    this.mDecayColor = decayColor;
    invalidate();
  }

  /**
   * Get sustain color.
   */
  public int getSustainColor() {
    return mSustainColor;
  }

  /**
   * Set sustain color.
   * @param sustainColor
   */
  public void setSustainColor(int sustainColor) {
    this.mSustainColor = sustainColor;
    invalidate();
  }

  /**
   * Get release color.
   * @return
   */
  public int getReleaseColor() {
    return mReleaseColor;
  }

  /**
   * Set release color.
   * @param mReleaseColor
   */
  public void setReleaseColor(int releaseColor) {
    this.mReleaseColor = releaseColor;
    invalidate();
  }

  /**
   * Get range.
   * @return
   */
  public Range getRange() {
    return mRange;
  }

  /**
   * Set range.
   * @param range
   */
  public void setRange(Range range) {
    this.mRange = range;
    
    if(mAttack == null)  mAttack  = range.start;
    if(mDecay == null)   mDecay   = range.start;
    if(mSustain == null) mSustain = range.start;
    if(mRelease == null) mRelease = range.start;
    
    invalidate();
  }

  /**
   * Get show value.
   * 
   * @return
   */
  public boolean isShowValue() {
    return mShowValue;
  }

  /**
   * Set show value.
   * 
   * @param showValue
   */
  public void setShowValue(boolean showValue) {
    this.mShowValue = showValue;
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
   * @param textSize
   */
  public void setTextSize(float textSize) {
    this.mTextSize = textSize;
  }

  /**
   * Get stroke width.
   * @return
   */
  public float getStrokeWidth() {
    return mStrokeWidth;
  }

  /**
   * Set stroke width.
   * 
   * @param strokeWidth
   */
  public void setStrokeWidth(float strokeWidth) {
    this.mStrokeWidth = strokeWidth;
  }

  /**
   * Set ADSR change listener.
   * 
   * @param onADSRChangeListener
   */
  public void setOnADSRChangeListener(OnADSRChangeListener onADSRChangeListener) {
		this.mOnADSRChangeListener = onADSRChangeListener;
	}

}
