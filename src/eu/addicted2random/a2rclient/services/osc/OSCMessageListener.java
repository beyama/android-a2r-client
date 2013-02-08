package eu.addicted2random.a2rclient.services.osc;

import com.illposed.osc.OSCMessage;

public interface OSCMessageListener {
  
  public void onOSCMessage(OSCMessage message);

}
