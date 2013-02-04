package eu.addicted2random.a2rclient.services;

import java.net.URI;

import org.jboss.netty.channel.ChannelFuture;

import android.content.Intent;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;

public abstract class AbstractConnection implements Runnable {
  private URI mUri;
  
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
  
  abstract public boolean isOpen();

  @Override
  public void run() {
    open();
  }
  
  public URI getURI() {
    return mUri;
  }
  
  abstract public ChannelFuture write(Object object);
  
  public ChannelFuture sendOSC(OSCPacket packet) {
    return write(packet);
  }
  
  public ChannelFuture sendOSC(String address, Object[] args) {
    return sendOSC(new OSCMessage(address, args));
  }
  
}
