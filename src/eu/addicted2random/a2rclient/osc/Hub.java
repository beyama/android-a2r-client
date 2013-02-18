package eu.addicted2random.a2rclient.osc;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.jboss.netty.channel.ChannelFuture;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;

import eu.addicted2random.a2rclient.net.AbstractConnection;


public class Hub implements OSCPacketListener, OSCMessageListener {
  
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
      for(OSCPacket packet : bundle.getPackets())
        this.listener.onOSCPacket(packet);
    }
    
  }

  private AbstractConnection connection;
  
  private final Map<String, Token> tokenByAddress = new HashMap<String, Token>(100);
  private final Token root = new Token(null, null);
  
  private final Timer timer = new Timer();
  
  public Hub() {
    super();
  }
  
  public void setConnection(AbstractConnection connection) {
    // remove OSC packet listener from previously set connection
    if(this.connection != null)
      this.connection.setOscPacketListener(null);
    
    this.connection = connection;
    this.connection.setOscPacketListener(this);
  }

  public void dispose() {
    synchronized (this) {
      connection.setOscPacketListener(null);
      root.dispose(false);
      connection = null;
    }
  }

  public Token getOrCreateTokenForAddress(String address) {
    synchronized (this) {
      String[] tokens = address.split("/");
      Token parent = root;
      
      for(int i = 1; i < tokens.length; i++) {
        String token = tokens[i];
        
        Token t = parent.getChild(token);
        if(t == null) {
          parent = new Token(parent, token);
          tokenByAddress.put(parent.getAddress(), parent);
        } else {
          parent = t;
        }
      }
      return parent;
    }
  }
  
  public ChannelFuture sendOSC(OSCPacket packet) {
    if(connection ==  null) return null;
    return connection.sendOSC(packet);
  }

  public ChannelFuture sendOSC(String address, Object[] args) {
    if(connection == null) return null;
    return connection.sendOSC(address, args);
  }

  public void onOSCBundle(OSCBundle bundle) {
    Date now = new Date();
    
    if(bundle.getTimestamp().getTime() <= now.getTime()) {
      for(OSCPacket packet : bundle.getPackets())
        onOSCPacket(packet);
    } else {
      timer.schedule(new BundleTask(this, bundle), bundle.getTimestamp());
    }
  }
  
  @Override
  public void onOSCMessage(OSCMessage message) {
    synchronized (this) {
      Token token = tokenByAddress.get(message.getAddress());
      if(token != null && token.getNode() != null)
        token.getNode().onOSCMessage(message);
    }
  }

  @Override
  public synchronized void onOSCPacket(OSCPacket packet) {
    if(packet instanceof OSCMessage) {
      onOSCMessage((OSCMessage)packet);
    } else {
      onOSCBundle((OSCBundle)packet);
    }
    
  }

}
