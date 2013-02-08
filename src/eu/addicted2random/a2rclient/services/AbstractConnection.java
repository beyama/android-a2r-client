package eu.addicted2random.a2rclient.services;

import java.net.URI;

import org.jboss.netty.channel.ChannelFuture;

import android.content.Intent;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;

import eu.addicted2random.a2rclient.services.osc.OSCPacketListener;

public abstract class AbstractConnection {
  private URI mUri;
  
  private OSCPacketListener mOscPacketListener = null;
  
  public AbstractConnection(Intent intent) {
    mUri = (URI)intent.getSerializableExtra("uri");
    
    if(mUri == null) {
      throw new NullPointerException("Intent extra 'uri' can't be null.");
    }
  }
  
  /**
   * Close connection
   */
  abstract public void close() throws InterruptedException;
  
  /**
   * Open connection
   */
  abstract public void open();
  
  /**
   * Is connection open?
   * @return
   */
  abstract public boolean isOpen();

  /**
   * Get the URI of this connection.
   * @return
   */
  public URI getURI() {
    return mUri;
  }
  
  abstract public ChannelFuture write(Object object);
  
  public void setOscPacketListener(OSCPacketListener oscPacketListener) {
    mOscPacketListener = oscPacketListener;
  }
  
  public OSCPacketListener getOscPacketListener() {
    return mOscPacketListener;
  }

  /**
   * Send an OSC packet to the server.
   * @param packet
   * @return
   */
  public ChannelFuture sendOSC(OSCPacket packet) {
    return write(packet);
  }
  
  /**
   * Send an OSC message to the server.
   * 
   * @param address The destination endpoint address.
   * @param args OSC arguments
   * @return
   */
  public ChannelFuture sendOSC(String address, Object[] args) {
    return sendOSC(new OSCMessage(address, args));
  }
  
}
