package eu.addicted2random.a2rclient;

import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SensorListAdapter extends BaseAdapter {

  private Context mContext;
  
  private SensorManager mManager;
  
  private List<Sensor> mSensors;
  
  private LayoutInflater mLayoutInflater;
  
  
  public SensorListAdapter(Context context) {
    mContext = context;
    mManager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
    mLayoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    mSensors = mManager.getSensorList(Sensor.TYPE_ALL);
  }

  @Override
  public int getCount() {
    return mSensors.size();
  }

  @Override
  public Sensor getItem(int position) {
    return mSensors.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    Log.v("SensorListAdapter:", String.format("convertView is %s", convertView == null ? "null" : "not null"));
    Sensor sensor = mSensors.get(position);
    
    View view = null;
    
    if(convertView != null && convertView instanceof LinearLayout) {
      view = convertView;
    } else {
      view = mLayoutInflater.inflate(R.layout.activity_sensor_list_item, parent, false);
    }
  
    TextView sensorName = (TextView)view.findViewById(R.id.sensorName);
    sensorName.setText(sensor.getName());
    
    return view;
  }
  
  public SensorManager getManager() {
    return mManager;
  }
  
  public int getId(Sensor sensor) {
    return mSensors.indexOf(sensor);
  }
  
  public Collection<Sensor> getSensors() {
    return mSensors;
  }

}
