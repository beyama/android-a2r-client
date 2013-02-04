package eu.addicted2random.a2rclient.services;

import java.net.URI;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;


public class ConnectionService extends Service implements OnOSCMessageListener {
  
  public class Binder extends android.os.Binder {
    
    /**
     * Get the instance of {@link ConnectionService}.
     * 
     * @return
     */
    public ConnectionService getService() {
      return ConnectionService.this;
    }
  }
  
  final static String TAG = "A2RService";
  
  private static void v(String message) {
    Log.v(TAG, message);
  }
  
  private static void v(String message, Object ...args) {
    Log.v(TAG, String.format(message, args));
  }

  private final IBinder mBinder = new Binder();
  
  private AbstractConnection mConnection = null;

  public ConnectionService() {
  }
  
  @Override
  public boolean onUnbind(Intent intent) {
    // TODO Auto-generated method stub
    return super.onUnbind(intent);
  }

  @Override
  public void onDestroy() {
    closeChannel();
    super.onDestroy();
  }

  public synchronized boolean closeChannel() {
    boolean ret = true;
    
    if(mConnection != null && mConnection.isOpen()) {
      try {
        mConnection.close();
      } catch (InterruptedException e) {
        ret = false;
      }
    }
    return ret;
  }
  
  public synchronized void handleCommand(Intent intent) {
    URI uri = (URI)intent.getSerializableExtra("uri");
    
    if(uri == null) {
      throw new NullPointerException("Intent extra 'uri' can't be null.");
    }
    
    v("Open connection to %s", uri.toString());
    
    if(mConnection != null) {
      if(mConnection.getURI().equals(uri)) return;
      
      v("Closing previous connection %s", mConnection.getURI().toString());
      closeChannel();
    }
    
    mConnection = new UdpOscConnection(intent, this);
    new Thread(mConnection).start();
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
  
  public AbstractConnection getConnection() {
    return mConnection;
  }

  @Override
  public void onOSCMessage(OSCMessage message) {
    Log.v(TAG, "Got message " + message.getAddress());
  }

  @Override
  public void onOSCBundle(OSCBundle bundle) {
    Log.v(TAG, "Got bundle");
  }

}
