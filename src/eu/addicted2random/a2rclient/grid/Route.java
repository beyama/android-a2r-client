package eu.addicted2random.a2rclient.grid;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import eu.addicted2random.a2rclient.osc.Pack;

public class Route implements Serializable {
  private static final long serialVersionUID = 5875699041119277777L;

  private final String address;
  private final Pack pack;
  private List<ServableRouteConnection> connections;
  
  public Route(String address, Pack pack) {
    super();
    this.address = address;
    this.pack = pack;
  }
  
  public Route(Servable servable) {
    this(servable.getAddress(), servable.getPack());
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
   * Get {@link ServableRouteConnection} list.
   * @return
   */
  public List<ServableRouteConnection> getConnections() {
    return connections;
  }
  
  /**
   * Add an {@link ServableRouteConnection} to the connections list.
   * 
   * @param connection
   */
  public void addServableRouteConnection(ServableRouteConnection connection) {
    if(connections == null) connections = new LinkedList<ServableRouteConnection>();
    connection.setRoute(this);
    connections.add(connection);
  }
  
}
