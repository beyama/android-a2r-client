package eu.addicted2random.a2rclient.net;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.util.ExternalResourceReleasable;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;

import eu.addicted2random.a2rclient.grid.Layout;
import eu.addicted2random.a2rclient.osc.Hub;
import eu.addicted2random.a2rclient.utils.Promise;
import eu.addicted2random.a2rclient.utils.PromiseListener;

/**
 * Abstract Addicted²Random connection.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public abstract class Connection {

  private enum State {
    NEW, OPENING, OPEN, CLOSING, CLOSED
  }

  private final URI mUri;

  private final Promise<Connection> mOpenPromise = new Promise<Connection>();

  // listener to handle fulfillment of the open promise
  private final PromiseListener<Connection> mOpenListener = new PromiseListener<Connection>() {

    @Override
    public void opperationComplete(Promise<Connection> result) {
      if (result.isSuccess()) {
        mState = State.OPEN;
      } else {
        mState = State.CLOSED;
        // fulfill the close promise to run all close listeners and release
        // external resources
        mClosePromise.success(Connection.this);
      }
    }

  };

  private final Promise<Connection> mClosePromise = new Promise<Connection>();

  // listener to handle fulfillment of the close promise
  private final PromiseListener<Connection> mCloseListener = new PromiseListener<Connection>() {

    @Override
    public void opperationComplete(Promise<Connection> result) {
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

  private List<ExternalResourceReleasable> mReleaseOnCloseResources = null;

  private Layout mCurrentSelectedLayout;

  public Connection(URI uri) {
    mUri = uri;
    mOpenPromise.addListener(mOpenListener);
    mClosePromise.addListener(mCloseListener);
  }

  abstract protected void doClose(Promise<Connection> promise);

  abstract protected void doOpen(Promise<Connection> promise);

  /**
   * Close connection
   */
  public synchronized Promise<Connection> close() {
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

    // dispose layout
    if (mCurrentSelectedLayout != null)
      mCurrentSelectedLayout.dispose();

    return mClosePromise;
  }

  /**
   * Open connection
   */
  public synchronized Promise<Connection> open() {
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

  /**
   * Get connections open promise.
   * 
   * @return
   */
  public Promise<Connection> getOpenPromise() {
    return mOpenPromise;
  }

  /**
   * Get connections close promise.
   * 
   * @return
   */
  public Promise<Connection> getClosePromise() {
    return mClosePromise;
  }

  /**
   * Get current selected layout.
   * 
   * @return
   */
  public Layout getCurrentSelectedLayout() {
    return mCurrentSelectedLayout;
  }

  /**
   * Get hub from current selected layout.
   * 
   * @return
   */
  public Hub getHub() {
    if (mCurrentSelectedLayout != null)
      return mCurrentSelectedLayout.getHub();
    return null;
  }

  /**
   * Set current selected layout.
   * 
   * @param currentSelectedLayout
   */
  public synchronized void setCurrentSelectedLayout(Layout currentSelectedLayout) {
    if (mCurrentSelectedLayout != null && mCurrentSelectedLayout != currentSelectedLayout)
      mCurrentSelectedLayout.dispose();

    if (mCurrentSelectedLayout == currentSelectedLayout)
      return;

    mCurrentSelectedLayout = currentSelectedLayout;
    if (mCurrentSelectedLayout != null)
      mCurrentSelectedLayout.getHub().setConnection(this);
  }

}
