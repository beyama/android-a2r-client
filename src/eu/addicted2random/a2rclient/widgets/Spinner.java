package eu.addicted2random.a2rclient.widgets;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class Spinner extends TextView implements SensorEventListener {
  
  public interface SpinnerEventListener {
    void onSpinnerLengthCalculated(float length);
  }
  
  static final String TAG = "Spinner";
  
  static void v(String message) {
    Log.v(TAG, message);
  }
  
  static void v(String message, Object ...args) {
    Log.v(TAG, String.format(message, args));
  }
  
  private SensorManager mSensorManager;
  
  private Sensor mSensor;
  
  private float[] mLast = null;
  
  private volatile float mLength = 0f;
  
  private float mLastLength = 0f;
  
  private SpinnerEventListener mListener = null;
  
  private Timer mTimer = new Timer();
  
  // sync length with view
  private Runnable mSync = new Runnable() {
    
    @Override
    public void run() {
      Spinner s = Spinner.this;
      s.setText(String.valueOf(s.mLastLength));
    }
  };
  
  // reset length and call spinner event handler
  private TimerTask mTimerTask = new TimerTask() {
    
    private int count = 0;
    
    @Override
    public void run() {
      count ++;
      
      Spinner s = Spinner.this;
      
      float length = s.mLength;
      SpinnerEventListener listener = s.mListener;
      
      s.mLength = 0f;
      s.mLastLength = length;
      
      if(count == 10) {
        s.post(s.mSync);
        count = 0;
      }
      
      if(listener != null)
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
    mSensorManager = (SensorManager)getContext().getSystemService(Context.SENSOR_SERVICE);
    mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    
    if(mSensor != null) {
      mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
      
      // call the timer task every 100ms
      mTimer.schedule(mTimerTask, 100, 100);
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    
    if(mSensor != null) {
      mSensorManager.unregisterListener(this);
      mTimer.cancel();
      mTimer.purge();
    }
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    float[] current = event.values;
    
    if(mLast != null) {
      // sqrt((B[0] - A[0])² + (B[1] - A[1])² + (B[2] - A[2])²)
      double x = Math.pow((double)(current[0] - mLast[0]), 2d);
      double y = Math.pow((double)(current[1] - mLast[1]), 2d);
      double z = Math.pow((double)(current[2] - mLast[2]), 2d);
      mLength += Math.sqrt(x + y + z);
      
      // copy values; it seems that the values-array is reused by the sensor so we can't use the reference.
      mLast[0] = current[0];
      mLast[1] = current[1];
      mLast[2] = current[2];
    } else {
      mLast = new float[] { current[0], current[1], current[2] };
    }
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
