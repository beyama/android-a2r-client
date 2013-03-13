package eu.addicted2random.a2rclient.net;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Binder;
import eu.addicted2random.a2rclient.exceptions.ProtocolNotSupportedException;
import eu.addicted2random.a2rclient.utils.Promise;
import eu.addicted2random.a2rclient.utils.PromiseListener;

public class ConnectionServiceBinding extends Binder {

  private final Map<String, AbstractConnection> connections = new HashMap<String, AbstractConnection>();
  
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
    return connections.get(uri.toString());
  }
  
  public synchronized AbstractConnection createConnection(final URI uri) throws ProtocolNotSupportedException {
    if(hasConnection(uri)) return getConnection(uri);
    
    final AbstractConnection connection;
    
    if(uri.getScheme().equals("udp+osc"))
      connection = new UdpOscConnection(uri);
    else if(uri.getScheme().equals("ws"))
      connection = new WebSocketConnection(uri);
    else
      throw new ProtocolNotSupportedException(uri);
    
    connections.put(uri.toString(), connection);
    
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
    for(Entry<String, AbstractConnection> entry : connections.entrySet()) {
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
