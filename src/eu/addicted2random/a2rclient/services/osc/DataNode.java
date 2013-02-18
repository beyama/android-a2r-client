package eu.addicted2random.a2rclient.services.osc;

import android.util.Log;

import com.illposed.osc.OSCMessage;

import eu.addicted2random.a2rclient.Range;
import eu.addicted2random.a2rclient.osc.Pack;
import eu.addicted2random.a2rclient.osc.PackSupport;
import eu.addicted2random.a2rclient.osc.Type;

public class DataNode extends Node implements Pack.PackListener {

  private final Pack pack;
  
  public DataNode(Hub hub, String address, Pack pack) {
    super(hub, address);
    this.pack = pack;
    pack.addPackListener(this);
  }
  
  public DataNode(Hub hub, String address, Type[] types, Object[] values) {
    this(hub, address, new PackSupport(types, values));
  }
  
  public DataNode(Hub hub, String address, Type type, int length) {
    this(hub, address, new PackSupport(type, length));
  }
  
  public DataNode(Hub hub, String address, Type type) {
    this(hub, address, new PackSupport(type));
  }
  
  public DataNode(Hub hub, String address, Type type, Object value) {
    this(hub, address, new PackSupport(type, value));
  }
  
  public DataNode(Hub hub, String address, Type[] types) {
    this(hub, address, new PackSupport(types));
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
    getHub().sendOSC(getAddress(), pack.getValues());
  }

  public Pack getPack() {
    return pack;
  }

}
