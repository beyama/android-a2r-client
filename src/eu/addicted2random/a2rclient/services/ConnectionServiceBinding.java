package eu.addicted2random.a2rclient.services;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Binder;
import android.util.Log;
import eu.addicted2random.a2rclient.services.AbstractConnection.ConnectionListener;
import eu.addicted2random.a2rclient.services.osc.UdpOscConnection;
import eu.addicted2random.a2rclient.services.osc.WebSocketConnection;

public class ConnectionServiceBinding extends Binder {

  private final Map<URI, AbstractConnection> connections = new HashMap<URI, AbstractConnection>();
  
  public ConnectionServiceBinding() {
  }
  
  /**
   * Does a connection exist?
   * 
   * @param uri Connection URI.
   * 
   * @return
   */
  public boolean hasConnection(URI uri) {
    return connections.containsKey(uri);
  }
  
  /**
   * Get connection.
   * 
   * @param uri
   * @return
   */
  public AbstractConnection getConnection(URI uri) {
    return connections.get(uri);
  }
  
  public synchronized AbstractConnection createConnection(final URI uri) {
    if(hasConnection(uri)) return getConnection(uri);
    
    final AbstractConnection connection;
    
    if(uri.getScheme().equals("udp+osc"))
      connection = new UdpOscConnection(uri);
    else if(uri.getScheme().equals("ws"))
      connection = new WebSocketConnection(uri);
    else
      throw new RuntimeException("Unsupported protocol " + uri.getScheme());
    
    connections.put(uri, connection);
    
    connection.addConnectionListener(new ConnectionListener() {
      
      @Override
      public void onConnectionOpened() {
      }
      
      @Override
      public void onConnectionError(Throwable e) {
      }
      
      @Override
      public void onConnectionClosed() {
        Log.v("ConnectionServiceBinding", "onConnectionClosed");
        ConnectionServiceBinding.this.connections.remove(uri);
      }
    });
    
    return connection;
  }
  
  /**
   * Create and open a connection.
   * 
   * @param uri
   * @return
   * @throws Exception
   */
  public AbstractConnection openConnection(URI uri) throws Exception {
    AbstractConnection connection = createConnection(uri);
    connection.open();
    return connection;
  }

  /**
   * Close all connections.
   * 
   * @throws Exception
   */
  public synchronized void closeAllConnections() {
    for(Entry<URI, AbstractConnection> entry : connections.entrySet()) {
      AbstractConnection connection = entry.getValue();
      
      if(connection.isOpen()) {
        try {
          connection.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    connections.clear();
  }
  
}
