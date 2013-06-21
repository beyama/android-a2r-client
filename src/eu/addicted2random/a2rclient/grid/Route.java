package eu.addicted2random.a2rclient.grid;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.addicted2random.a2rclient.osc.Pack;
import eu.addicted2random.a2rclient.osc.PackSupport;
import eu.addicted2random.a2rclient.osc.Types;
import eu.addicted2random.a2rclient.utils.Range;

/**
 * This class represents an OSC route and holds informations about
 * {@link Element}s and {@link Sensor}s that are mapped to it.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class Route implements Serializable, Servable {
  private static final long serialVersionUID = 5875699041119277777L;

  @JsonBackReference("layout")
  private Layout layout;
  
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

  public Layout getLayout() {
		return layout;
	}

	public void setLayout(Layout layout) {
		this.layout = layout;
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
   * Create and return a pack.
   * 
   * This is called by {@link Layout#fromJSON()}.
   * 
   * Returns null if something goes wrong.
   * 
   * @return
   */
  public Pack onCreatePack() {
  	// create and set a pack for signature
    eu.addicted2random.a2rclient.osc.Type[] types = new eu.addicted2random.a2rclient.osc.Type[signature.size()];
    Object[] values = new Object[signature.size()];

    for (int i = 0; i < signature.size(); i++) {
      Type type = signature.get(i);

      eu.addicted2random.a2rclient.osc.Type oscType = Types.getTypeByName(type.getType());

      if (oscType == null) // invalid signature
      	return null;

      // set range
      if (type.getMinimum() != null && type.getMaximum() != null)
        oscType = oscType.setRange(new Range(type.getMinimum(), type.getMaximum(), type.getStep()));

      // set default value
      if (type.getDefaultValue() != null) {
        if (oscType.canCast(type.getDefaultValue())) {
          values[i] = oscType.cast(type.getDefaultValue());
        }
      }

      types[i] = oscType;
    }
    
    pack = new PackSupport(types, values, layout.getLock());
    
    return pack;
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
