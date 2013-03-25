package eu.addicted2random.a2rclient.net;

import java.net.URI;
import java.util.List;

import android.content.Context;
import eu.addicted2random.a2rclient.grid.Layout;
import eu.addicted2random.a2rclient.jam.Jam;
import eu.addicted2random.a2rclient.utils.Promise;
import eu.addicted2random.a2rclient.utils.PromiseListener;

/**
 * WebSocket connection handler implementation.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class WebSocketConnectionHandler extends AbstractConnectionHandler {

  @SuppressWarnings("unused")
  private static final String TAG = "WebSocketConnectionHandler";

  private final Context mContext;

  private WebSocketConnection mConnection;
  
  private Jam mSelectedJam;

  private Layout mSelectedLayout;

  private Promise<List<Jam>> mJams;

  private Promise<List<Layout>> mLayouts;

  private Promise<Layout> mLayout;

  private Promise<Boolean> mJoin;

  public WebSocketConnectionHandler(Context context, URI uri) {
    super(uri);
    mContext = context;
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.net.AbstractConnectionHandler#doOpen()
   */
  @Override
  protected void doOpen() {
    mConnection = new WebSocketConnection(getUri());

    mConnection.getOpenPromise().addListener(new PromiseListener<Connection>() {
      @Override
      public void opperationComplete(Promise<Connection> promise) {
        if (promise.isSuccess()) {
          getOpenPromise().success(WebSocketConnectionHandler.this);
        } else {
          getOpenPromise().failure(promise.getCause());
        }
      }
    });

    mConnection.getClosePromise().addListener(new PromiseListener<Connection>() {

      @Override
      public void opperationComplete(Promise<Connection> promise) {
        if (promise.isSuccess()) {
          getClosePromise().success(WebSocketConnectionHandler.this);
        } else {
          getClosePromise().failure(promise.getCause());
        }
      }
    });

    mConnection.open();
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.net.AbstractConnectionHandler#doClose()
   */
  @Override
  protected void doClose() {
    mConnection.close();
  }

  /*
   * (non-Javadoc)
   * @see eu.addicted2random.a2rclient.net.ConnectionHandler#hasJams()
   */
  @Override
  public boolean hasJams() {
    return true;
  }

  /*
   * (non-Javadoc)
   * @see eu.addicted2random.a2rclient.net.ConnectionHandler#hasLayouts()
   */
  @Override
  public boolean hasLayouts() {
    return true;
  }

  /*
   * (non-Javadoc)
   * @see eu.addicted2random.a2rclient.net.ConnectionHandler#getJams()
   */
  @Override
  public synchronized Promise<List<Jam>> getJams() {
    checkIsOpen();

    if (mJams != null)
      return mJams;

    mJams = mConnection.getJamService().getAll();
    return mJams;
  }

  /*
   * (non-Javadoc)
   * @see eu.addicted2random.a2rclient.net.ConnectionHandler#getLayouts()
   */
  @Override
  public synchronized Promise<List<Layout>> getLayouts() {
    checkIsOpen();

    if (mLayouts != null)
      return mLayouts;

    if (getSelectedJam() == null)
      throw new IllegalStateException("No jam selected");

    mLayouts = mConnection.getJamService().getLayouts(getSelectedJam().getId());
    return mLayouts;
  }

  /*
   * (non-Javadoc)
   * @see eu.addicted2random.a2rclient.net.ConnectionHandler#getLayout()
   */
  @Override
  public synchronized Promise<Layout> getLayout() {
    checkIsOpen();

    if (mLayout != null)
      return mLayout;

    final Jam selectedJam = mSelectedJam;
    final Layout selectedLayout = mSelectedLayout;

    if (selectedJam == null)
      throw new IllegalStateException("No jam selected.");

    if (selectedLayout == null)
      throw new IllegalStateException("No layout selected.");

    mLayout = mConnection.getJamService().getLayout(mContext, selectedJam.getId(), selectedLayout.getId());

    mLayout.addListener(new PromiseListener<Layout>() {

      @Override
      public void opperationComplete(Promise<Layout> promise) {
        if (promise.isSuccess()) {
          Layout layout = promise.getResult();
          layout.setJam(selectedJam);
          if (selectedLayout == getSelectedLayout())
            mConnection.setCurrentSelectedLayout(layout);
        }
      }
    });

    return mLayout;
  }

  /*
   * (non-Javadoc)
   * @see eu.addicted2random.a2rclient.net.ConnectionHandler#join()
   */
  @Override
  public Promise<Boolean> join() {
    checkIsOpen();

    if (mJoin != null)
      return mJoin;

    if (mSelectedJam == null)
      throw new IllegalStateException("No jam selected.");

    if (mLayout == null)
      throw new IllegalStateException("Layout is not loaded.");

    mJoin = mConnection.getJamService().join(mSelectedJam.getId(), mLayout.getResult().getId());
    return mJoin;
  }

  /*
   * (non-Javadoc)
   * @see eu.addicted2random.a2rclient.net.ConnectionHandler#leave()
   */
  @Override
  public synchronized void leave() {
    checkIsOpen();

    if (mJoin != null && mLayout.isSuccess())
      mConnection.getJamService().leave(mSelectedJam.getId(), mLayout.getResult().getId());
    mJoin = null;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.net.ConnectionHandler#setSelectedJam(eu.
   * addicted2random.a2rclient.jam.Jam)
   */
  @Override
  public synchronized void setSelectedJam(Jam jam) {
    if(mSelectedJam != null && (jam == null || !mSelectedJam.equals(jam))) {
      if(mJoin != null)
        leave();
      mLayouts = null;
      mLayout = null;
      mConnection.setCurrentSelectedLayout(null);
      mSelectedLayout = null;
    }
    mSelectedJam = jam;
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.net.ConnectionHandler#getSelectedJam()
   */
  @Override
  public Jam getSelectedJam() {
    return mSelectedJam;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.addicted2random.a2rclient.net.ConnectionHandler#setSelectedLayout(eu
   * .addicted2random.a2rclient.grid.Layout)
   */
  @Override
  public synchronized void setSelectedLayout(Layout layout) {
    mSelectedLayout = layout;
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.net.ConnectionHandler#getSelectedLayout()
   */
  @Override
  public Layout getSelectedLayout() {
    return mSelectedLayout;
  }

}
