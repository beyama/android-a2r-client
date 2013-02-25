package eu.addicted2random.a2rclient.jsonrpc;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a JSON-RPC 2 error response.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class Error extends Response {
  private final static String ERROR = "error";

  /**
   * Construct a new {@link Error} response.
   * 
   * @param id
   *          The response id.
   * @param rpcError
   *          The {@link RPCError}.
   */
  public Error(Object id, RPCError rpcError) {
    super(id, rpcError);

    if (rpcError == null)
      throw new IllegalArgumentException("rpcError must not be null");
    if (!(rpcError instanceof RPCError))
      throw new IllegalArgumentException("rpcError must be an instance of RPCError");
  }

  /**
   * Construct a new {@link Error} response.
   * 
   * @param id
   *          The response ID.
   * @param code
   *          The RPC error code.
   * @param message
   *          The RPC error message.
   * @param payload
   *          The RPC error data
   */
  public Error(Object id, int code, String message, Object payload) {
    this(id, new RPCError(code, message, payload));
  }
  
  /**
   * Construct a new {@link Error} response without payload.
   * 
   * @param id
   *          The response ID.
   * @param code
   *          The RPC error code.
   * @param message
   *          The RPC error message.
   */
  public Error(Object id, int code, String message) {
    this(id, code, message, null);
  }

  /**
   * Get the {@link RPCError}
   * 
   * @return
   */
  public RPCError getError() {
    return (RPCError) getPayload();
  }

  /**
   * Get the RPC error code.
   * 
   * @return
   */
  public int getCode() {
    return getError().getCode();
  }

  /**
   * Get the RPC error message.
   * 
   * @return
   */
  public String getMessage() {
    return getError().getMessage();
  }

  /**
   * Get the RPC error data.
   * 
   * @return
   */
  public Object getData() {
    return getError().getData();
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.jsonrpc.Message#toJSON()
   */
  @Override
  public Object toJSON() throws JSONException {
    JSONObject object = (JSONObject) super.toJSON();

    object.put(ERROR, getError().toJSON());

    return object;
  }
}
