package eu.addicted2random.a2rclient.net;

import android.os.Binder;

/**
 * Connection service binder.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public class ConnectionServiceBinder extends Binder {

  private final ConnectionService mService;
  
  public ConnectionServiceBinder(ConnectionService service) {
    super();
    mService = service;
  }
  
  /**
   * Get connection service.
   * 
   * @return
   */
  public ConnectionService getService() {
    return mService;
  }

}
