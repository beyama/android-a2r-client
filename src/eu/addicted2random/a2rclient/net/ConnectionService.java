package eu.addicted2random.a2rclient.net;

import java.net.URI;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import eu.addicted2random.a2rclient.exceptions.ProtocolNotSupportedException;
import eu.addicted2random.a2rclient.utils.Promise;
import eu.addicted2random.a2rclient.utils.PromiseListener;

/**
 * Connection service.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class ConnectionService extends Service {

  private ConnectionHandler mHandler;

  private ConnectionServiceBinder mBinder;

  public ConnectionService() {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Service#onDestroy()
   */
  @Override
  public void onDestroy() {
    super.onDestroy();
    closeHandler();
  }

  /**
   * Open a connection handler for given uri.
   * 
   * @param uri
   * @return
   * @throws ProtocolNotSupportedException
   */
  public synchronized ConnectionHandler open(final URI uri) throws ProtocolNotSupportedException {
    if (mHandler != null) {
      // return existing connection handler if uri is equal
      if (mHandler.getUri().equals(uri))
        return mHandler;
      // close previous connection handler
      else
        mHandler.close();
    }
    
    mHandler = null;

    if (uri.getScheme().equals("ws"))
      mHandler = new WebSocketConnectionHandler(getApplicationContext(), uri);
    else
      throw new ProtocolNotSupportedException(uri);

    mHandler.open();
    final ConnectionHandler handler = mHandler;
    
    mHandler.getClosePromise().addListener((PromiseListener<ConnectionHandler>) new PromiseListener<ConnectionHandler>() {

      @Override
      public void opperationComplete(Promise<ConnectionHandler> promise) {
        synchronized (ConnectionService.this) {
          if(handler == mHandler)
            mHandler = null;
        }
      }
    });

    return mHandler;
  }

  /**
   * Close current handler.
   */
  public synchronized void closeHandler() {
    if (mHandler != null) {
      mHandler.close();
      mHandler = null;
    }
  }

  /**
   * Get current connection handler.
   * 
   * @return
   */
  public ConnectionHandler getHandler() {
    return mHandler;
  }

  @Override
  public IBinder onBind(Intent intent) {
    if (mBinder != null)
      return mBinder;

    mBinder = new ConnectionServiceBinder(this);
    return mBinder;
  }

}
