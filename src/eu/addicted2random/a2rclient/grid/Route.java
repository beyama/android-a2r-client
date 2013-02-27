package eu.addicted2random.a2rclient.grid;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.addicted2random.a2rclient.osc.Pack;

public class Route implements Serializable {
  private static final long serialVersionUID = 5875699041119277777L;

  private final String address;

  @JsonProperty
  private List<Type> signature = new LinkedList<Type>();

  private Pack pack;
  
  private List<ServableRouteConnection> connections;

  @JsonCreator
  public Route(@JsonProperty(value = "address", required = true) String address) {
    super();
    this.address = address;
  }
  
  public Route(Servable servable) {
    this(servable.getAddress());
    this.pack = servable.getPack();
  }

  /**
   * Get address.
   * 
   * @return
   */
  public String getAddress() {
    return address;
  }

  /**
   * Get pack.
   * 
   * @return
   */
  public Pack getPack() {
    return pack;
  }

  /**
   * Set pack.
   * 
   * @param pack
   */
  public void setPack(Pack pack) {
    this.pack = pack;
  }

  /**
   * Get {@link ServableRouteConnection} list.
   * 
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
    if (connections == null)
      connections = new LinkedList<ServableRouteConnection>();
    connection.setRoute(this);
    connections.add(connection);
  }

  public List<Type> getSignature() {
    return signature;
  }

  public void setSignature(List<Type> signature) {
    this.signature = signature;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((address == null) ? 0 : address.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Route other = (Route) obj;
    if (address == null) {
      if (other.address != null)
        return false;
    } else if (!address.equals(other.address))
      return false;
    return true;
  }

}
