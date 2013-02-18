package eu.addicted2random.a2rclient.grid;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

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
public class Sensor implements Servable, SensorEventListener {

  /* map sensor names to sensor constants */
  static private final Map<String, Integer> sensorTypeByName = new HashMap<String, Integer>();
  
  static {
    sensorTypeByName.put("accelerometer", android.hardware.Sensor.TYPE_ACCELEROMETER);
    sensorTypeByName.put("magneticField", android.hardware.Sensor.TYPE_MAGNETIC_FIELD);
    sensorTypeByName.put("gyroscope", android.hardware.Sensor.TYPE_GYROSCOPE);
    sensorTypeByName.put("gravity", android.hardware.Sensor.TYPE_GRAVITY);
    sensorTypeByName.put("linearAcceleration", android.hardware.Sensor.TYPE_LINEAR_ACCELERATION);
    sensorTypeByName.put("rotationVector", android.hardware.Sensor.TYPE_ROTATION_VECTOR);
  }
  
  private final String name;
  private final SensorManager manager;
  private final android.hardware.Sensor sensor;
  
  private String address;
  private Pack pack;
  private List<ServableRouteConnection> connections;
  
  public Sensor(String name, SensorManager manager) {
    this.name = name;
    this.manager = manager;
    Integer type = sensorTypeByName.get(name);
    if(type != null)
      this.sensor = manager.getDefaultSensor(type);
    else
      this.sensor = null;
  }

  /**
   * Get sensor name.
   * @return
   */
  public String getName() {
    return name;
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

  /**
   * Get OSC address.
   * 
   * @return
   */
  @Override
  public String getAddress() {
    return address;
  }

  /**
   * Set OSC address.
   */
  @Option
  public void setAddress(String address) {
    this.address = address;
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
      
      pack = new PackSupport(type, 3);
      
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
