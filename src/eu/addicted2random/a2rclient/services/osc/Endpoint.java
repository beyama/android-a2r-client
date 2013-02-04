package eu.addicted2random.a2rclient.services.osc;

import org.jboss.netty.channel.Channel;

import com.illposed.osc.OSCMessage;

/**
 * OSC endpoint handler interface
 * 
 * @author Alexander Jentz
 */
public interface Endpoint {
  
  /**
   * Handle OSC message
   * 
   * @param channel Connection channel
   * @param message OSC message
   */
  public void call(Channel channel, OSCMessage message);
}
