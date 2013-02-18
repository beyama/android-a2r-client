package eu.addicted2random.a2rclient.net;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


public class ConnectionService extends Service {
  
  final static String TAG = "A2RService";
  
  @SuppressWarnings("unused")
  private static void v(String message) {
    Log.v(TAG, message);
  }
  
  @SuppressWarnings("unused")
  private static void v(String message, Object ...args) {
    Log.v(TAG, String.format(message, args));
  }

  private ConnectionServiceBinding mBinder = null;
  
  public ConnectionService() {
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if(mBinder != null)
      mBinder.closeAllConnections();
  }
  
  public synchronized void handleCommand(Intent intent) {
    if(mBinder == null)
      mBinder = new ConnectionServiceBinding();
  }
  

  @Override
  @Deprecated
  public void onStart(Intent intent, int startId) {
    handleCommand(intent);
    super.onStart(intent, startId);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    handleCommand(intent);
    return START_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    handleCommand(intent);
    return mBinder;
  }

}
