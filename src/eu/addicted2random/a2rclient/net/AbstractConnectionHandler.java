package eu.addicted2random.a2rclient.net;

import java.net.URI;

import eu.addicted2random.a2rclient.utils.Promise;

/**
 * Abstract implementation of {@link ConnectionHandler}.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public abstract class AbstractConnectionHandler implements ConnectionHandler {

  private final URI mUri;

  private final Promise<ConnectionHandler> mOpenPromise = new Promise<ConnectionHandler>();

  private final Promise<ConnectionHandler> mClosePromise = new Promise<ConnectionHandler>();

  private boolean mIsOpening = false;

  private boolean mIsClosing = false;

  public AbstractConnectionHandler(URI uri) {
    if (uri == null)
      throw new NullPointerException();
    mUri = uri;
  }

  /**
   * Open handler. This will be called synchronized and only once on first call
   * to {@link AbstractConnectionHandler#open()}.
   */
  protected abstract void doOpen();

  /**
   * Close handler. This will be called synchronized and only once on first call
   * to {@link AbstractConnectionHandler#close()} and only if {@link AbstractConnectionHandler#open()} is called before.
   */
  protected abstract void doClose();

  /*
   * (non-Javadoc)
   * @see eu.addicted2random.a2rclient.net.ConnectionHandler#open()
   */
  @Override
  public synchronized Promise<ConnectionHandler> open() {
    if (mIsOpening)
      return mOpenPromise;

    mIsOpening = true;

    doOpen();

    return mOpenPromise;
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.net.ConnectionHandler#close()
   */
  @Override
  public synchronized Promise<ConnectionHandler> close() {
    checkIsOpen();
    
    if (mIsClosing)
      return mClosePromise;

    mIsClosing = true;

    doClose();

    return mClosePromise;
  }
  
  /**
   * Throws an {@link IllegalStateException} if the connection isn't open.
   * 
   * @throws IllegalStateException
   */
  protected void checkIsOpen() {
    if(!isOpen())
      throw new IllegalStateException("Not open");
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.net.ConnectionHandler#isOpen()
   */
  @Override
  public synchronized boolean isOpen() {
    return mOpenPromise.isSuccess() && !mClosePromise.isDone();
  }

  /* (non-Javadoc)
   * @see eu.addicted2random.a2rclient.net.ConnectionHandler#isClosed()
   */
  @Override
  public boolean isClosed() {
    return mClosePromise.isDone();
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.net.ConnectionHandler#getUri()
   */
  @Override
  public URI getUri() {
    return mUri;
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.net.ConnectionHandler#getOpenPromise()
   */
  @Override
  public Promise<ConnectionHandler> getOpenPromise() {
    return mOpenPromise;
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.net.ConnectionHandler#getClosePromise()
   */
  @Override
  public Promise<ConnectionHandler> getClosePromise() {
    return mClosePromise;
  }

}
