package eu.addicted2random.a2rclient.osc;

import com.illposed.osc.OSCPacket;

public interface OSCPacketListener {
  
  public void onOSCPacket(OSCPacket packet);

}
