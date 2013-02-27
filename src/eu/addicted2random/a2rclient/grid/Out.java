package eu.addicted2random.a2rclient.grid;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.addicted2random.a2rclient.osc.Pack;

/**
 * Out configuration to describe how to map a {@link Pack} to a {@link Route}.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class Out {

  private String address;

  @SuppressLint("UseSparseArrays")
  private Map<Integer, Integer> map = new HashMap<Integer, Integer>();

  @JsonCreator
  public Out(@JsonProperty(value = "address", required = true) String address,
      @JsonProperty(value = "map", required = true) Map<Integer, Integer> map) {
    this.address = address;
    this.map = map;
  }

  /**
   * Get OSC address.
   * 
   * @return
   */
  public String getAddress() {
    return address;
  }

  /**
   * Set OSC address.
   * 
   * @param address
   */
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * Get pack to route parameter map.
   * 
   * @return
   */
  public Map<Integer, Integer> getMap() {
    return map;
  }

  /**
   * Set pack to route parameter map.
   * 
   * @param map
   */
  public void setMap(Map<Integer, Integer> map) {
    this.map = map;
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
    Out other = (Out) obj;
    if (address == null) {
      if (other.address != null)
        return false;
    } else if (!address.equals(other.address))
      return false;
    return true;
  }

}
