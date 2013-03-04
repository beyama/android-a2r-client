package eu.addicted2random.a2rclient.jsonrpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a JSON-RPC 2 error response.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class Error extends Response {

  private final RPCError error;

  /**
   * Construct a new {@link Error} response.
   * 
   * @param id
   *          The response id.
   * @param error
   *          The {@link RPCError}.
   */
  @JsonCreator
  public Error(@JsonProperty("id") Object id, @JsonProperty(value = "error", required = true) RPCError error) {
    super(id);

    if (error == null)
      throw new IllegalArgumentException("error must not be null");
    this.error = error;
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
  @JsonProperty
  public RPCError getError() {
    return error;
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

}
