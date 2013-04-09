package eu.addicted2random.a2rclient.jsonrpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a JSON-RPC 2 error.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class RPCError extends RuntimeException {

  public static final int PARSE_ERROR_CODE = -32700;

  public static final String PARSE_ERROR_MESSAGE = "Parse error";

  public static final int METHOD_NOT_FOUND_CODE = -32601;

  public static final String METHOD_NOT_FOUND_MESSAGE = "Method not found";

  public static final int SERVER_ERROR_CODE = -32000;

  public static final String SERVER_ERROR_MESSAGE = "Server error";

  public static final int INVALID_PARAMS_CODE = -32602;

  public static final String INVALID_PARAMS_MESSAGE = "Invalid params";

  private static final long serialVersionUID = 7593028657234184051L;

  private final int code;

  private final Object data;

  /**
   * Privat constructor for Jackson.
   * 
   * @param code
   * @param message
   * @param data
   */
  @SuppressWarnings("unused")
  @JsonCreator
  private RPCError(@JsonProperty(value = "code", required = true) int code, @JsonProperty("message") String message,
      @JsonProperty("data") JsonNode data) {
    this(code, message, (Object) data);
  }

  /**
   * Construct a new instance of {@link RPCError}.
   * 
   * @param code
   *          The RPC error code.
   * @param message
   *          The RPC error message.
   * @param data
   *          The RPC error data.
   * 
   * @param throwable
   *          The causer of the error.
   */
  public RPCError(int code, String message, Object data, Throwable throwable) {
    super(message, throwable);

    this.code = code;
    this.data = data;
  }

  /**
   * Construct a new instance of {@link RPCError}.
   * 
   * @param code
   *          The RPC error code.
   * @param message
   *          The RPC error message.
   * @param data
   *          The RPC error data.
   */
  public RPCError(int code, String message, Object data) {
    this(code, message, data, null);
  }

  /**
   * Construct a new instance of {@link RPCError} without error data.
   * 
   * @param code
   *          The RPC error code.
   * @param message
   *          The RPC error message.
   */
  public RPCError(int code, String message) {
    this(code, message, (Object) null);
  }

  /**
   * Construct a new instance of {@link RPCError} without error data.
   * 
   * @param code
   *          The RPC error code.
   * @param message
   *          The RPC error message.
   * @param throwable
   *          The causer of the error.
   */
  public RPCError(int code, String message, Throwable throwable) {
    this(code, message, null, throwable);
  }

  /**
   * Get RPC error code.
   * 
   * @return
   */
  @JsonProperty
  public int getCode() {
    return code;
  }

  /**
   * Get RPC error message.
   */
  @Override
  @JsonProperty
  public String getMessage() {
    return super.getMessage();
  }

  /**
   * An alias for {@link RPCError#getPayload()}.
   * 
   * @return
   */
  @JsonProperty
  public Object getData() {
    return data;
  }

}
