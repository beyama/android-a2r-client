package eu.addicted2random.a2rclient.net;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.ChannelFuture;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;

import eu.addicted2random.a2rclient.grid.Layout;
import eu.addicted2random.a2rclient.osc.Hub;
import eu.addicted2random.a2rclient.osc.OSCPacketListener;

public abstract class AbstractConnection {

  /**
   * Connection life cycle listener.
   */
  public interface ConnectionListener {
    /**
     * Called if connection is opened.
     */
    void onConnectionOpened();

    /**
     * Called if connection is closed.
     */
    void onConnectionClosed();

    /**
     * Called on connection error.
     * 
     * @param e
     */
    void onConnectionError(Throwable e);
  }

  private final URI mUri;

  private boolean mOpen = false;

  private OSCPacketListener mOscPacketListener = null;

  private List<ConnectionListener> mConnectionListeners = new ArrayList<ConnectionListener>(10);

  private Hub mHub;

  private Layout mLayout;

  public AbstractConnection(URI uri) {
    mUri = uri;
  }

  abstract protected void doClose() throws Throwable;

  abstract protected void doOpen() throws Throwable;

  /**
   * Close connection
   */
  public void close() throws Exception {
    synchronized (this) {
      if (!isOpen())
        return;
      
      mOpen = false;

      // dispose layout
      if (mLayout != null) {
        try {
          mLayout.dispose();
        } catch (Exception e) {
          onError(e);
        }
      }

      // dispose hub
      if (mHub != null) {
        try {
          mHub.dispose();
        } catch (Exception e) {
          onError(e);
        }
      }
      
      try {
        doClose();
      } catch (Throwable t) {
        onError(t);
        throw new RuntimeException(t);
      } finally {
        onClosed();
      }
    }
  }

  /**
   * Open connection
   */
  public void open() throws Exception {
    synchronized (this) {
      if (isOpen())
        return;

      try {
        doOpen();
        mOpen = true;
        onOpened();
      } catch (Throwable e) {
        onError(e);
        onClosed();
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Is connection open?
   * 
   * @return
   */
  public boolean isOpen() {
    return mOpen;
  }

  /**
   * Get the URI of this connection.
   * 
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
   * 
   * @param packet
   * @return
   */
  public ChannelFuture sendOSC(OSCPacket packet) {
    return write(packet);
  }

  /**
   * Send an OSC message to the server.
   * 
   * @param address
   *          The destination endpoint address.
   * @param args
   *          OSC arguments
   * @return
   */
  public ChannelFuture sendOSC(String address, Object[] args) {
    return sendOSC(new OSCMessage(address, args));
  }

  /**
   * Call {@link ConnectionListener#onConnectionOpened()} on all registered
   * connection listeners.
   */
  protected void onOpened() {
    for (ConnectionListener listener : mConnectionListeners)
      listener.onConnectionOpened();
  }

  /**
   * Call {@link ConnectionListener#onConnectionClosed()} on all registered
   * connection listeners.
   */
  protected void onClosed() {
    for (ConnectionListener listener : mConnectionListeners)
      listener.onConnectionClosed();
  }

  /**
   * Call {@link ConnectionListener#onConnectionClosed()} on all registered
   * connection listeners.
   */
  protected void onError(Throwable e) {
    for (ConnectionListener listener : mConnectionListeners)
      listener.onConnectionError(e);
  }

  /**
   * Register a {@link ConnectionListener}.
   * 
   * {@link ConnectionListener#onConnectionOpened()} will be called immediately
   * if connection is already opened.
   * 
   * @param listener
   */
  public void addConnectionListener(ConnectionListener listener) {
    synchronized (mConnectionListeners) {
      if (mConnectionListeners.contains(listener))
        return;
      mConnectionListeners.add(listener);
      if (isOpen())
        listener.onConnectionOpened();
    }
  }

  /**
   * Unregister a {@link ConnectionListener}.
   * 
   * @param listener
   */
  public void removeConnectionListener(ConnectionListener listener) {
    synchronized (mConnectionListeners) {
      int index = mConnectionListeners.indexOf(listener);
      if (index != -1)
        mConnectionListeners.remove(index);
    }
  }

  /**
   * Get list of registered {@link ConnectionListener}.
   * 
   * @return
   */
  public List<ConnectionListener> getConnectionListeners() {
    return mConnectionListeners;
  }

  /**
   * Get hub.
   * 
   * @return
   */
  public Hub getHub() {
    return mHub;
  }

  /**
   * Set hub.
   * 
   * @param hub
   */
  public void setHub(Hub hub) {
    hub.setConnection(this);
    mHub = hub;
  }

  /**
   * Get layout.
   * 
   * @return
   */
  public Layout getLayout() {
    return mLayout;
  }

  /**
   * Set layout.
   * 
   * @param layout
   */
  public void setLayout(Layout layout) {
    mLayout = layout;
  }

}
