package eu.addicted2random.a2rclient.osc;

import com.illposed.osc.OSCMessage;


/**
 * Represents a node in the OSC hub.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public class Node implements OSCMessageListener {

  private final Hub hub;
  private final Token token;
  private OSCMessageListener messageListener = null;
  
  public Node(Hub hub, String address) {
    super();
    this.hub = hub;
    this.token = hub.getOrCreateTokenForAddress(address);
    this.token.setNode(this);
  }
  
  /**
   * Dispose this node.
   */
  public void dispose() {
    synchronized (hub) {
      token.setNode(null);
    }
  }

  /**
   * Get the root node (the ancestor without parent)
   * or self if this node is a root node.
   * @return
   */
  public Hub getHub() {
    return hub;
  }
  
  /**
   * Get token of this node.
   * 
   * @return
   */
  public Token getToken() {
    return token;
  }

  /**
   * Get full address in OSC hub.
   * @return
   */
  public String getAddress() {
    return token.getAddress();
  }

  public OSCMessageListener getMessageListener() {
    return messageListener;
  }

  public void setMessageListener(OSCMessageListener messageListener) {
    this.messageListener = messageListener;
  }

  @Override
  public void onOSCMessage(OSCMessage message) {
    if(messageListener != null)
      messageListener.onOSCMessage(message);
  }
  
}
