package eu.addicted2random.a2rclient.services;

import java.net.URI;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import eu.addicted2random.a2rclient.services.osc.UdpOscConnection;


public class ConnectionService extends Service {
  
  final static String TAG = "A2RService";
  
  @SuppressWarnings("unused")
  private static void v(String message) {
    Log.v(TAG, message);
  }
  
  private static void v(String message, Object ...args) {
    Log.v(TAG, String.format(message, args));
  }

  private ConnectionServiceBinding mBinder = null;
  
  public ConnectionService() {
  }

  @Override
  public void onDestroy() {
    closeBinding();
    super.onDestroy();
  }

  public synchronized boolean closeBinding() {
    if(mBinder != null) {
      try {
        mBinder.close();
      } catch (InterruptedException e) {
        return false;
      }
    }
    return true;
  }
  
  public synchronized void handleCommand(Intent intent) {
    URI uri = (URI)intent.getSerializableExtra("uri");
    
    if(uri == null) {
      throw new NullPointerException("Intent extra 'uri' can't be null.");
    }
    
    v("Open connection to %s", uri.toString());
    
    if(mBinder != null) {
      if(mBinder.getURI().equals(uri)) return;
      
      closeBinding();
    }
    
    mBinder = new ConnectionServiceBinding(this.getBaseContext(), new UdpOscConnection(intent));
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
