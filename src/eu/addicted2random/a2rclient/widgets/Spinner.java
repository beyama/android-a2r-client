package eu.addicted2random.a2rclient.widgets;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import eu.addicted2random.a2rclient.utils.RingBuffer;

/**
 * Spinner widget.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public class Spinner extends View implements SensorEventListener {

	public interface SpinnerEventListener {
		void onSpinnerLengthCalculated(float length);
	}

	static final String TAG = "Spinner";

	static final int X = 0;

	static final int Y = 1;

	static final int Z = 2;

	static void v(String message) {
		Log.v(TAG, message);
	}

	static void v(String message, Object... args) {
		Log.v(TAG, String.format(message, args));
	}

	private SensorManager mSensorManager;

	private Sensor mSensor;

	private float[] mLast = null;

	private volatile float mLength = 0f;

	private volatile float mLastLength = 0f;

	private SpinnerEventListener mListener = null;

	private Timer mTimer = new Timer();

	private RectF mBounds = new RectF();

	private RectF mXBounds = new RectF();

	private RectF mYBounds = new RectF();

	private RectF mZBounds = new RectF();
	
	private RectF[] mBoundsArray = new RectF[] { mXBounds, mYBounds, mZBounds };

	private RingBuffer<float[]> mBuffer = new RingBuffer<float[]>(256);

	private Paint mXPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

	private Paint mYPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

	private Paint mZPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

	private Paint[] mPaints = new Paint[] { mXPaint, mYPaint, mZPaint };

	private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private Rect mTextBounds = new Rect();

	private PointF mXCurPoint = new PointF();

	private PointF mYCurPoint = new PointF();

	private PointF mZCurPoint = new PointF();

	private PointF[] mCurPoints = new PointF[] { mXCurPoint, mYCurPoint, mZCurPoint };
	
	private String[] mTexts = new String[] { "Shake", "Your", "Phone" };

	// reset length and call spinner event handler
	private TimerTask mTimerTask = new TimerTask() {
		@Override
		public void run() {
			Spinner s = Spinner.this;

			float length = s.mLength;
			SpinnerEventListener listener = s.mListener;

			s.mLength = 0f;
			s.mLastLength = length;

			if (listener != null)
				listener.onSpinnerLengthCalculated(length);
		}
	};

	public Spinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public Spinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public Spinner(Context context) {
		super(context);
		init();
	}

	private void init() {
		mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

		mXPaint.setColor(getResources().getColor(android.R.color.holo_blue_bright));
		mYPaint.setColor(getResources().getColor(android.R.color.holo_green_light));
		mZPaint.setColor(getResources().getColor(android.R.color.holo_red_light));

		for (int i = 0; i < 3; i++) {
			Paint paint = mPaints[i];
			paint.setStyle(Paint.Style.FILL);
			paint.setDither(true);
			paint.setStrokeJoin(Paint.Join.ROUND);
			paint.setStrokeCap(Paint.Cap.BUTT);
			paint.setAntiAlias(true);
		}

		mTextPaint.setTextSize(12f);
		mTextPaint.setColor(Color.WHITE);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		if (mSensor != null) {
			mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

			// call the timer task every 100ms
			mTimer.schedule(mTimerTask, 100, 100);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		if (mSensor != null) {
			mSensorManager.unregisterListener(this);
			mTimer.cancel();
			mTimer.purge();
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mBounds.set(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), (h - getPaddingBottom()));
		
		mXBounds.set(mBounds.left, mBounds.top, mBounds.left + (mBounds.width() / 3), mBounds.bottom);
		mYBounds.set(mXBounds.right, mBounds.top, mXBounds.right + mXBounds.width(), mBounds.bottom);
		mZBounds.set(mYBounds.right, mBounds.top, mYBounds.right + mYBounds.width(), mBounds.bottom);
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		RectF bound;
		PointF point;
		Paint paint;
		float value;
		float[] values;
		float[] lastValues;
		
		float y = mBounds.top + 32f;
		
		// set start points
		for (int i = 0; i < 3; i++) {
			point = mCurPoints[i];
			bound = mBoundsArray[i];
			
			point.set(bound.centerX(), bound.top + y);
		}
		
		boolean stoped = false;
		
		// draw lines
		for (int i = 0; i < mBuffer.length() && !stoped; i++) {
			values = mBuffer.get(mBuffer.length() - i - 1);
			
			for (int j = 0; j < 3; j++) {
				paint = mPaints[j];
				point = mCurPoints[j];
				bound = mBoundsArray[j];
				value = values[j] * 3f;

				float nextX = value + bound.centerX();
				float nextY = y + (i * 2);

				// stop if we reach the bottom
				if(nextY > bound.height()) {
					stoped = true;
					break;
				}
				
				canvas.drawLine(point.x, point.y, nextX, nextY, paint);
				
				// save x & y for the next round
				point.set(nextX, nextY);
			}
		}

		// draw text above the lines
		lastValues = mBuffer.last();

		if(lastValues != null) {
			for (int i = 0; i < 3; i++) {
				String text = mTexts[i];
				
				paint = mPaints[i];
				bound = mBoundsArray[i];
	
				paint.setStrokeWidth(1);
	
				value = lastValues[i];
	
				if (value < 0)
					value = value * -1f;
				if (value > 48)
					value = 48;
	
				paint.setTextSize(16f * (1f + value));
	
				paint.getTextBounds(text, 0, text.length(), mTextBounds);
	
				canvas.drawText(text, bound.centerX() - (mTextBounds.width() / 2), y, paint);
			}
		}

		// draw length in bottom left corner
		canvas.drawText(String.format("%.2f", mLastLength), mBounds.left, mBounds.bottom, mTextPaint);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float[] current = event.values;

		if (mLast != null) {
			// sqrt((B[0] - A[0])² + (B[1] - A[1])² + (B[2] - A[2])²)
			double x = Math.pow((double) (current[X] - mLast[X]), 2d);
			double y = Math.pow((double) (current[Y] - mLast[Y]), 2d);
			double z = Math.pow((double) (current[Z] - mLast[Z]), 2d);
			mLength += Math.sqrt(x + y + z);

			// copy values; it seems that the values-array is reused by the sensor so
			// we can't use the reference.
			mLast[0] = current[X];
			mLast[1] = current[Y];
			mLast[2] = current[Z];
		} else {
			mLast = new float[] { current[X], current[Y], current[Z] };
		}

		// recycle float arrays
		float[] values = mBuffer.getValueFromEndPointer();

		// create new array if
		if (values == null)
			values = new float[3];

		values[X] = current[X];
		values[Y] = current[Y];
		values[Z] = current[Z];

		mBuffer.add(values);
		invalidate();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public SpinnerEventListener getListener() {
		return mListener;
	}

	public void setListener(SpinnerEventListener listener) {
		this.mListener = listener;
	}

}
