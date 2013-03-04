package eu.addicted2random.a2rclient.jsonrpc;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Abstract base class of all JSON-RPC 2 response objects.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public abstract class Response extends Message {
  
  private final Object id;

  public Response(Object id) {
    super();
    if(id instanceof Number)
      this.id = ((Number) id).longValue();
    else
      this.id = id;
  }
  
  @JsonProperty
  public Object getId() {
    return id;
  }

}
  