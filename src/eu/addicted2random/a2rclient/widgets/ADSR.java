package eu.addicted2random.a2rclient.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.BaseSavedState;
import eu.addicted2random.a2rclient.Utils;

/**
 * @author Alexander Jentz, beyama.de
 * 
 */
public class ADSR extends View {

	public enum Parameter { ATTACK, DECAY, SUSTAIN, RELEASE }
	
	public interface OnADSRChangeListener {
		public void onADSRParameterChanged(ADSR view, Parameter param, int value);
	}
	
	private static class SavedState extends BaseSavedState {
		int attack;
		int decay;
		int sustain;
		int release;
		
		SavedState(Parcelable superState) {
			super(superState);
		}
	}

	private int mMinimum = 0;

	private int mMaximum = 127;

	private int mAttack = 0;

	private int mDecay = 0;

	private int mSustain = 0;

	private int mRelease = 0;

	private int mAttackColor = Color.argb(0xff, 0xd4, 0x00, 0x00);

	private int mDecayColor = Color.argb(0xff, 0xa9, 0xc0, 0x2b);

	private int mSustainColor = Color.argb(0xff, 0xff, 0xff, 0x00);

	private int mReleaseColor = Color.argb(0xff, 0xf4, 0x48, 0x00);

	private RectF mAttackBounds = new RectF();

	private RectF mDecayBounds = new RectF();

	private RectF mSustainBounds = new RectF();

	private RectF mReleaseBounds = new RectF();

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
	
	private OnADSRChangeListener mOnADSRChangeListener = null;

	public ADSR(Context context) {
		super(context);
		init();
	}

	public ADSR(Context context, AttributeSet attrs) {
		super(context, attrs);
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
		mPaint.setStrokeWidth(4.0f);

		mBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBorder.setStrokeWidth(0);
		mBorder.setStyle(Style.STROKE);
		mBorder.setColor(Color.LTGRAY);
		
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setColor(Color.WHITE);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		Log.v("ADSR: ", String.format("Size changed w: %d h: %d", w, h));
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

		mAttackBounds.set(mBounds.left + sw, zt, mBounds.left + zw, zb);
		mDecayBounds.set(mAttackBounds.right, zt, mAttackBounds.right + zw, zb);
		mSustainBounds.set(mDecayBounds.right, zt, mDecayBounds.right + zw, zb);
		mReleaseBounds.set(mSustainBounds.right, zt, mSustainBounds.right + zw, zb);

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
		
		
		canvas.drawText(String.format("%d", mAttack), mAttackBounds.left + 5f, mAttackBounds.bottom - 5f, mTextPaint);
		canvas.drawText(String.format("%d", mDecay), mDecayBounds.left + 5f, mDecayBounds.bottom - 5f, mTextPaint);
		canvas.drawText(String.format("%d", mSustain), mSustainBounds.left + 5f, mSustainBounds.bottom - 5f, mTextPaint);
		canvas.drawText(String.format("%d", mRelease), mReleaseBounds.left + 5f, mReleaseBounds.bottom - 5f, mTextPaint);

		/* draw border */
		// canvas.drawRect(mBounds, mBorder);
		if(mOnTouch) {
			//canvas.drawLine(mBounds.left, mBounds.top, mBounds.left, mBounds.bottom, mBorder);
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
			if(mYUpdate < 0f && dy > 0.0f)
				mYUpdate = 0.0f;
			else if(mYUpdate > 0f && dy < 0.0f)
				mYUpdate = 0.0f;
			
			mYUpdate += dy;
			
			switch (mZone) {
			case ATTACK:
				if((mYUpdate < 0 ? mYUpdate * -1 : mYUpdate) > 2f) {
					setAttack((int)((float)mAttack + (mYUpdate / 2f)));
					mYUpdate = 0f;
				}
				break;
			case DECAY:
				if((mYUpdate < 0 ? mYUpdate * -1 : mYUpdate) > 2f) {
					setDecay((int)((float)mDecay + (mYUpdate / 2f)));
					mYUpdate = 0f;
				}
				break;
			case SUSTAIN:
				if((mYUpdate < 0 ? mYUpdate * -1 : mYUpdate) > 3f) {
					setSustain((int)(mSustain + (mYUpdate / 3f)));
					mYUpdate = 0f;
				}
				break;
			case RELEASE:
				if((mYUpdate < 0 ? mYUpdate * -1 : mYUpdate) > 2f) {
					setRelease((int)((float)mRelease + (mYUpdate / 2f)));
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
		ss.attack = mAttack;
		ss.decay = mDecay;
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
		
		set(ss.attack, ss.decay, ss.sustain, ss.release, true);
	}

	public void setAttack(int value, boolean silent) {
		if (value < mMinimum)
			mAttack = mMinimum;
		else if (value > mMaximum)
			mAttack = mMaximum;
		else
			mAttack = value;

		mAttackDecayVertex.x = Utils.scale(mAttack, mMinimum, mMaximum,
				mAttackBounds.left, mAttackBounds.right);
		
		if(silent) return;
		
		if(mOnADSRChangeListener != null)
			mOnADSRChangeListener.onADSRParameterChanged(this, Parameter.ATTACK, mAttack);
		
		invalidate();
	}
	
	public void setAttack(int value) {
		setAttack(value, false);
	}
	
	public int getAttack() {
	  return mAttack;
	}

	public void setDecay(int value, boolean silent) {
		if (value < mMinimum)
			mDecay = mMinimum;
		else if (value > mMaximum)
			mDecay = mMaximum;
		else
			mDecay = value;

		mDecaySustainVertex.x = Utils.scale(mDecay, mMinimum, mMaximum,
				mDecayBounds.left, mDecayBounds.right);
		
		if(silent) return;
		
		if(mOnADSRChangeListener != null)
			mOnADSRChangeListener.onADSRParameterChanged(this, Parameter.DECAY, mDecay);
		
		invalidate();
	}
	
	public void setDecay(int value) {
		setDecay(value, false);
	}
	
	 public int getDecay() {
	   return mDecay;
	 }

	public void setSustain(int value, boolean silent) {
		if (value < mMinimum)
			mSustain = mMinimum;
		else if (value > mMaximum)
			mSustain = mMaximum;
		else
			mSustain = value;

		mSustainReleaseVertex.y = Utils.scale(mSustain, mMinimum, mMaximum,
				mSustainBounds.bottom, mSustainBounds.top);
		mDecaySustainVertex.y = mSustainReleaseVertex.y;
		
		if(silent) return;
		
		if(mOnADSRChangeListener != null)
			mOnADSRChangeListener.onADSRParameterChanged(this, Parameter.SUSTAIN, mSustain);
		
		invalidate();
	}
	
	public void setSustain(int value) {
		setSustain(value, false);
	}
	
	public int getSustain() {
	  return mSustain;
	}

	public void setRelease(int value, boolean silent) {
		if (value < mMinimum)
			mRelease = mMinimum;
		else if (value > mMaximum)
			mRelease = mMaximum;
		else
			mRelease = value;

		mReleaseEnd.x = Utils.scale(mRelease, mMinimum, mMaximum,
				mReleaseBounds.left, mReleaseBounds.right);
		
		if(silent) return;
		
		if(mOnADSRChangeListener != null)
			mOnADSRChangeListener.onADSRParameterChanged(this, Parameter.RELEASE, mRelease);
		invalidate();
	}
	
	public void setRelease(int value) {
		setRelease(value, false);
	}
	
	public int getRelease() {
	  return mRelease;
	}
	
	public void set(int attack, int decay, int sustain, int release, boolean silent) {
		setAttack(attack, silent);
		setDecay(decay, silent);
		setSustain(sustain, silent);
		setRelease(release, silent);
	}
	
	public void set(int attack, int decay, int sustain, int release) {
		set(attack, decay, sustain, release, false);
	}
	
	public void setOnADSRChangeListener(OnADSRChangeListener onADSRChangeListener) {
		this.mOnADSRChangeListener = onADSRChangeListener;
	}

}
