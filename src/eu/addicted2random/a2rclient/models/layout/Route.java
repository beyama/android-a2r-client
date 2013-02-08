package eu.addicted2random.a2rclient.models.layout;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import eu.addicted2random.a2rclient.osc.Pack;

public class Route implements Serializable {
  private static final long serialVersionUID = 5875699041119277777L;

  private final String address;
  private final Pack pack;
  private List<ElementRouteConnection> connections;
  
  public Route(String address, Pack pack) {
    super();
    this.address = address;
    this.pack = pack;
  }

  /**
   * Get address.
   * @return
   */
  public String getAddress() {
    return address;
  }

  /**
   * Get pack.
   * @return
   */
  public Pack getPack() {
    return pack;
  }

  /**
   * Get {@link ElementRouteConnection} list.
   * @return
   */
  public List<ElementRouteConnection> getConnections() {
    return connections;
  }
  
  /**
   * Add an {@link ElementRouteConnection} to the connections list.
   * 
   * @param connection
   */
  public void addElementRouteConnection(ElementRouteConnection connection) {
    if(connections == null) connections = new LinkedList<ElementRouteConnection>();
    connection.setRoute(this);
    connections.add(connection);
  }
  
}
