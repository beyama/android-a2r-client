package eu.addicted2random.a2rclient.osc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

import com.illposed.osc.OSCMessage;

import eu.addicted2random.a2rclient.grid.Servable;
import eu.addicted2random.a2rclient.utils.Range;

public class DataNode extends Node implements Pack.PackListener {

  private final Pack pack;
  
  public DataNode(Hub hub, String address, Pack pack) {
    super(hub, address);
    this.pack = pack;
    pack.addPackListener(this);
  }
  
  public DataNode(Hub hub, Servable servable) {
    this(hub, servable.getAddress(), servable.getPack());
  }
  
  public DataNode(Hub hub, String address, Type[] types, Object[] values, ReentrantLock lock) {
    this(hub, address, new PackSupport(types, values, lock));
  }
  
  public DataNode(Hub hub, String address, Type type, int length, ReentrantLock lock) {
    this(hub, address, new PackSupport(type, length, lock));
  }
  
  public DataNode(Hub hub, String address, Type type, ReentrantLock lock) {
    this(hub, address, new PackSupport(type, lock));
  }
  
  public DataNode(Hub hub, String address, Type type, Object value, ReentrantLock lock) {
    this(hub, address, new PackSupport(type, value, lock));
  }
  
  public DataNode(Hub hub, String address, Type[] types, ReentrantLock lock) {
    this(hub, address, new PackSupport(types, lock));
  }
  
  @Override
  public void onOSCMessage(OSCMessage message) {
    super.onOSCMessage(message);
    
    Object[] values = message.getArguments();
    
    pack.lock(this);
    
    try {
      for(int i = 0; i < values.length; i++) {
        Object value = values[i];
        Type type = pack.getTypeAt(i);
        
        if(type.isInstance(value)) {
          if(type.getRange() != null)
            value = type.getRange().round(Range.valueOf(value));
          pack.set(i, value);
        } else if(type.canCast(value)) {
          value = type.cast(value);
          if(type.getRange() != null)
            value = type.getRange().round(Range.valueOf(value));
          pack.set(i, value);
        } else {
          Log.v("DataNode", "Got invalid value on " + getAddress() + " " + value.toString());
        }
      }
    } finally {
      pack.unlock();
    }
  }

  @Override
  public void onValueChanged(Pack source, Object actor, int index, Object oldValue, Object newValue) {
  }

  @Override
  public void onPacked(Pack source) {
    Object[] values = pack.getValues();
    List<Object> args = new ArrayList<Object>(values.length);
    Collections.addAll(args, values);
    getHub().sendOSC(getAddress(), args);
  }

  public Pack getPack() {
    return pack;
  }

}
