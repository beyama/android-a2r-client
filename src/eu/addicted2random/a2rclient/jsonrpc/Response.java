package eu.addicted2random.a2rclient.jsonrpc;


/**
 * Abstract base class of all JSON-RPC 2 response objects.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public abstract class Response extends Message {

  private static final String ID_IS_NULL_EXCEPTION = "id must not be null";

  public Response(Object id, Object payload) {
    super(id, payload);
    
    if(id == null)
      throw new IllegalArgumentException(ID_IS_NULL_EXCEPTION);
  }
  
  public Response(Object id) {
    super(id);
  }

}
  