package eu.addicted2random.a2rclient;

import android.app.ListActivity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class SensorActivity extends ListActivity implements SensorEventListener {
  
  SensorListAdapter mAdapter;
  
  Sensor mCurrent = null;
  
  long lastUpdate = 0;
  
  TextView mText1 = null;
  TextView mText2 = null;
  TextView mText3 = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    mAdapter = new SensorListAdapter(this);
    setListAdapter(mAdapter);
  }

  @Override
  protected void onPause() {
    super.onPause();
    mAdapter.getManager().unregisterListener(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    
    if(mCurrent != null)
      mAdapter.getManager().registerListener(this, mCurrent, SensorManager.SENSOR_DELAY_UI);
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    
    if(mCurrent != null)
      mAdapter.getManager().unregisterListener(this);
    
    mCurrent = mAdapter.getItem(position);
    Log.v("SensorActivity: ", String.format("pos: %d id: %d", position, id));
    
    mText1 = (TextView)v.findViewById(R.id.sensorValue1);
    mText2 = (TextView)v.findViewById(R.id.sensorValue2);
    mText3 = (TextView)v.findViewById(R.id.sensorValue3);
    
    mAdapter.getManager().registerListener(this, mCurrent, SensorManager.SENSOR_DELAY_UI);      
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.activity_sensor, menu);
    return true;
  }
  
  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if(lastUpdate == 0) {
      lastUpdate = event.timestamp;
    } else {
      if((event.timestamp - lastUpdate) < 250000)
        return;
      lastUpdate = event.timestamp;
    }
    
    for(int i = 0; i < event.values.length; i++) {
      switch(i) {
      case 0:
        mText1.setText(String.valueOf(event.values[i]));
        break;
      case 1:
        mText2.setText(String.valueOf(event.values[i]));
        break;
      case 2:
        mText3.setText(String.valueOf(event.values[i]));
        break;
      }
      
    }
      
  }

}
