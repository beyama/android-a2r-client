package eu.addicted2random.a2rclient.net;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Binder;
import eu.addicted2random.a2rclient.utils.Promise;
import eu.addicted2random.a2rclient.utils.PromiseListener;

public class ConnectionServiceBinding extends Binder {

  private final Map<URI, AbstractConnection> connections = new HashMap<URI, AbstractConnection>();
  
  public ConnectionServiceBinding() {
  }
  
  /**
   * Does a connection with this URI exist?
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
    
    connection.getClosePromise().addListener(new PromiseListener<AbstractConnection>() {
      @Override
      public void opperationComplete(Promise<AbstractConnection> result) {
        removeConnection(uri);
      }
    });
    
    return connection;
  }
  
  private synchronized boolean removeConnection(URI uri) {
    return connections.remove(uri) != null;
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
