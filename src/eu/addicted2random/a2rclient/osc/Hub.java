package eu.addicted2random.a2rclient.osc;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.jboss.netty.channel.ChannelFuture;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;

import eu.addicted2random.a2rclient.net.Connection;

/**
 * Implementation of an OSC Hub.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class Hub implements OSCPacketListener, OSCMessageListener {

  /**
   * Task to handle bundles at a specific time.
   */
  class BundleTask extends TimerTask {

    private final OSCPacketListener listener;
    private final OSCBundle bundle;

    public BundleTask(OSCPacketListener listener, OSCBundle bundle) {
      super();
      this.listener = listener;
      this.bundle = bundle;
    }

    @Override
    public void run() {
      for (OSCPacket packet : bundle.getPackets())
        this.listener.onOSCPacket(packet);
    }

  }

  private Connection connection;

  private final Map<String, Token> tokenByAddress = new HashMap<String, Token>(100);
  private final Token root = new Token(null, null);

  private final Timer timer = new Timer();

  public Hub() {
    super();
  }

  /**
   * Set connection.
   * 
   * @param connection
   */
  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  /**
   * Dispose this hub.
   */
  public synchronized void dispose() {
    root.dispose(false);
    connection = null;
  }

  public synchronized Token getOrCreateTokenForAddress(String address) {
    if (!Address.isValidAddress(address))
      throw new IllegalArgumentException("Invalid address `" + String.valueOf(address) + "`");

    String[] tokens = address.split("/");
    Token parent = root;

    for (int i = 1; i < tokens.length; i++) {
      String token = tokens[i];

      Token t = parent.getChild(token);
      if (t == null) {
        parent = new Token(parent, token);
        tokenByAddress.put(parent.getAddress(), parent);
      } else {
        parent = t;
      }
    }
    return parent;
  }

  /**
   * Send an OSC packet to the remote hub.
   * 
   * @param packet
   * @return
   */
  public ChannelFuture sendOSC(OSCPacket packet) {
    if (connection == null)
      return null;
    return connection.sendOSC(packet);
  }

  /**
   * Send an OSC message to the remote hub.
   * 
   * @param address
   * @param args
   * @return
   */
  public ChannelFuture sendOSC(String address, Collection<Object> args) {
    if (connection == null)
      return null;
    return connection.sendOSC(address, args);
  }

  /**
   * Handle an OSC bundle.
   * 
   * @param bundle
   */
  protected void onOSCBundle(OSCBundle bundle) {
    Date now = new Date();
    
    if (bundle.getTimestamp().getTime() <= now.getTime()) {
      for (OSCPacket packet : bundle.getPackets())
        onOSCPacket(packet);
    } else {
      timer.schedule(new BundleTask(this, bundle), bundle.getTimestamp());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.addicted2random.a2rclient.osc.OSCMessageListener#onOSCMessage(com.illposed
   * .osc.OSCMessage)
   */
  @Override
  public synchronized void onOSCMessage(OSCMessage message) {
    Token token = tokenByAddress.get(message.getAddress());
    if (token != null && token.getNode() != null)
      token.getNode().onOSCMessage(message);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.addicted2random.a2rclient.osc.OSCPacketListener#onOSCPacket(com.illposed
   * .osc.OSCPacket)
   */
  @Override
  public synchronized void onOSCPacket(OSCPacket packet) {
    if (packet instanceof OSCMessage) {
      onOSCMessage((OSCMessage) packet);
    } else {
      onOSCBundle((OSCBundle) packet);
    }

  }

}
