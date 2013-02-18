package eu.addicted2random.a2rclient.models.layout;

import eu.addicted2random.a2rclient.osc.Pack;
import eu.addicted2random.a2rclient.services.osc.DataNode;

/**
 * Interface for all objects that can be served by a
 * {@link DataNode}.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public interface Servable {

  /**
   * Get OSC address.
   * 
   * @return
   */
  public String getAddress();
  
  /**
   * Get an instance of {@link Pack}.
   * 
   * @return
   */
  public Pack getPack();
  
}
