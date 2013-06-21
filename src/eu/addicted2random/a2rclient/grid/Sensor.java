package eu.addicted2random.a2rclient.grid;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.addicted2random.a2rclient.osc.Pack;
import eu.addicted2random.a2rclient.osc.PackSupport;
import eu.addicted2random.a2rclient.osc.Type;
import eu.addicted2random.a2rclient.osc.Types;
import eu.addicted2random.a2rclient.utils.Range;

/**
 * Sensor model. Connects a named {@link android.hardware.Sensor} with
 * a {@link Pack}.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class Sensor implements Servable, SensorEventListener {
  
  /* map sensor names to sensor constants */
  static private final Map<String, Integer> sensorTypeByName = new HashMap<String, Integer>();
  
  static {
    sensorTypeByName.put("accelerometer", android.hardware.Sensor.TYPE_ACCELEROMETER);
    sensorTypeByName.put("magneticField", android.hardware.Sensor.TYPE_MAGNETIC_FIELD);
    sensorTypeByName.put("gyroscope", android.hardware.Sensor.TYPE_GYROSCOPE);

    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
      sensorTypeByName.put("gravity", android.hardware.Sensor.TYPE_GRAVITY);
      sensorTypeByName.put("linearAcceleration", android.hardware.Sensor.TYPE_LINEAR_ACCELERATION);
      sensorTypeByName.put("rotationVector", android.hardware.Sensor.TYPE_ROTATION_VECTOR);
    }
  }
  
  private String type;
  
  private SensorManager manager;
  
  private android.hardware.Sensor sensor;
  
  private Pack pack;
  
  private List<ServableRouteConnection> connections;
  
  private List<Out> outs = new LinkedList<Out>();
  
  @JsonBackReference("layout")
  private Layout layout;
  
  @JsonCreator
  public Sensor(@JsonProperty(value="type", required=true) String type) {
    this.type = type;
  }
  
  public void setSensorManager(SensorManager manager) {
    this.manager = manager;
    Integer type = sensorTypeByName.get(this.type);
    if(type != null)
      this.sensor = manager.getDefaultSensor(type);
    else
      this.sensor = null;
  }

  /**
   * Get sensor type.
   * @return
   */
  public String getType() {
    return type;
  }
  
  /**
   * Get sensor manager.
   * 
   * @return
   */
  public SensorManager getManager() {
    return manager;
  }

  /**
   * Get underlying sensor.
   * @return
   */
  public android.hardware.Sensor getSensor() {
    return sensor;
  }

  @Override
  public Pack getPack() {
    synchronized (this) {
      if(pack != null) return pack;
      
      if(sensor == null) return null;
      
      float maxRange   = sensor.getMaximumRange();
      float resolution = sensor.getResolution();
      
      Range range = new Range(maxRange * -1f, maxRange, resolution);
      
      Type type = Types.FLOAT_TYPE.setRange(range);
      
      pack = new PackSupport(type, 3, getLayout().getLock());
      
      manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
      
      return pack;
    }
  }
  
  /**
   * Get {@link ServableRouteConnection} list.
   * @return
   */
  public List<ServableRouteConnection> getConnections() {
    return connections;
  }
  
  public List<Out> getOuts() {
    return outs;
  }

  public void setOuts(List<Out> outs) {
    this.outs = outs;
  }

  public Layout getLayout() {
    return layout;
  }

  public void setLayout(Layout layout) {
    this.layout = layout;
  }

  /**
   * Add an {@link ServableRouteConnection} to the connections list.
   * 
   * @param connection
   */
  public void addServableRouteConnection(ServableRouteConnection connection) {
    if(connections == null) connections = new LinkedList<ServableRouteConnection>();
    connection.setServable(this);
    connections.add(connection);
  }
  
  /**
   * Unregister event listener and set pack to null.
   */
  public void dispose() {
    synchronized (this) {
      if(pack == null) return;
      
      manager.unregisterListener(this);
      pack = null;
    }
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    synchronized (this) {
      if(pack == null) return;
      
      pack.lock(this);
      for(int i = 0; i < event.values.length; i++)
        pack.set(i, event.values[i]);
      pack.unlock();
    }
  }

  @Override
  public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) { 
  }
  
}
