package eu.addicted2random.a2rclient.services.osc;

import com.illposed.osc.OSCPacket;

public interface OSCPacketListener {
  
  public void onOSCPacket(OSCPacket packet);

}
