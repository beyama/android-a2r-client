package eu.addicted2random.a2rclient.services;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;

public interface OnOSCMessageListener {
  
  public void onOSCMessage(OSCMessage message);
  
  public void onOSCBundle(OSCBundle bundle);

}
