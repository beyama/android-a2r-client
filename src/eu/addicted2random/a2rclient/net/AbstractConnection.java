package eu.addicted2random.a2rclient.net;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.util.ExternalResourceReleasable;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;

import eu.addicted2random.a2rclient.osc.Hub;
import eu.addicted2random.a2rclient.osc.OSCPacketListener;
import eu.addicted2random.a2rclient.utils.Promise;
import eu.addicted2random.a2rclient.utils.PromiseListener;

public abstract class AbstractConnection {

  private enum State {
    NEW, OPENING, OPEN, CLOSING, CLOSED
  }

  private final URI mUri;

  private final Promise<AbstractConnection> mOpenPromise = new Promise<AbstractConnection>();

  // listener to handle fulfillment of the open promise
  private final PromiseListener<AbstractConnection> mOpenListener = new PromiseListener<AbstractConnection>() {

    @Override
    public void opperationComplete(Promise<AbstractConnection> result) {
      if (result.isSuccess()) {
        mState = State.OPEN;
        mHub.setConnection(AbstractConnection.this);
      } else {
        mState = State.CLOSED;
        // fulfill the close promise to run all close listeners and release
        // external resources
        mClosePromise.success(AbstractConnection.this);
      }
    }

  };

  private final Promise<AbstractConnection> mClosePromise = new Promise<AbstractConnection>();

  // listener to handle fulfillment of the close promise
  private final PromiseListener<AbstractConnection> mCloseListener = new PromiseListener<AbstractConnection>() {

    @Override
    public void opperationComplete(Promise<AbstractConnection> result) {
      mState = State.CLOSED;

      // we release all external resources from a new thread
      if (mReleaseOnCloseResources != null) {
        new Thread(new Runnable() {

          @Override
          public void run() {
            releaseExternalResources();
          }

        }).start();
      }

    }

  };

  private State mState = State.NEW;

  private OSCPacketListener mOscPacketListener = null;

  private List<ExternalResourceReleasable> mReleaseOnCloseResources = null;

  private final LayoutService mLayoutService = new LayoutService(this);

  private Hub mHub = new Hub();

  public AbstractConnection(URI uri) {
    mUri = uri;
    mOpenPromise.addListener(mOpenListener);
    mClosePromise.addListener(mCloseListener);
  }

  abstract protected void doClose(Promise<AbstractConnection> promise);

  abstract protected void doOpen(Promise<AbstractConnection> promise);

  /**
   * Close connection
   */
  public synchronized Promise<AbstractConnection> close() throws Exception {
    if (isClosing() || isClosed())
      return mClosePromise;

    // throw exception if this connection isn't opened
    if (!isOpen())
      throw new IllegalStateException("Connection is not open");

    mState = State.CLOSING;

    try {
      doClose(mClosePromise);
    } catch (Throwable t) {
      if (!mClosePromise.isDone())
        mClosePromise.failure(t);
    }

    // close layout service
    try {
      mLayoutService.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    // dispose hub
    if (mHub != null) {
      try {
        mHub.dispose();
      } catch (Exception e) {
      }
    }

    return mClosePromise;
  }

  /**
   * Open connection
   */
  public synchronized Promise<AbstractConnection> open() {
    if (isClosing() || isClosed())
      throw new IllegalStateException("This connection is closing or closed");

    if (isOpening() || isOpen())
      return mOpenPromise;

    mState = State.OPENING;

    // we do it in a thread to prevent a NetworkOnMainThreadException
    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          doOpen(mOpenPromise);
        } catch (Throwable t) {
          if (!mOpenPromise.isDone())
            mOpenPromise.failure(t);
        }
      }
    }).start();

    return mOpenPromise;
  }

  /**
   * Is connection opening?
   * 
   * @return
   */
  public boolean isOpening() {
    return mState == State.OPENING;
  }

  /**
   * Is connection open?
   * 
   * @return
   */
  public boolean isOpen() {
    return mState == State.OPEN;
  }

  /**
   * Is connection closed?
   * 
   * @return
   */
  public boolean isClosed() {
    return mState == State.CLOSED;
  }

  /**
   * Is connection closing?
   * 
   * @return
   */
  public boolean isClosing() {
    return mState == State.CLOSING;
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
  public ChannelFuture sendOSC(String address, Collection<Object> args) {
    return sendOSC(new OSCMessage(address, args));
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
   * Get the layout service.
   * 
   * @return
   */
  public LayoutService getLayoutService() {
    return mLayoutService;
  }

  /**
   * Register a {@link ExternalResourceReleasable} resource that should be
   * released after closing the connection.
   * 
   * @param resource
   */
  protected synchronized void releaseOnClose(ExternalResourceReleasable resource) {
    if (resource == null)
      throw new NullPointerException();

    if (isClosed())
      throw new IllegalStateException("Connection is already closed");
    if (mReleaseOnCloseResources == null)
      mReleaseOnCloseResources = new LinkedList<ExternalResourceReleasable>();

    mReleaseOnCloseResources.add(resource);
  }

  private synchronized void releaseExternalResources() {
    if (mReleaseOnCloseResources != null) {
      for (ExternalResourceReleasable resource : mReleaseOnCloseResources) {
        resource.releaseExternalResources();
      }
    }
  }

  public Promise<AbstractConnection> getOpenPromise() {
    return mOpenPromise;
  }

  public Promise<AbstractConnection> getClosePromise() {
    return mClosePromise;
  }

}
