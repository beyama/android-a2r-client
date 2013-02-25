package eu.addicted2random.a2rclient.jsonrpc;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a JSON-RPC 2 error.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class RPCError extends RuntimeException implements IPayload {

  public static final int PARSE_ERROR_CODE = -32700;

  public static final String PARSE_ERROR_MESSAGE = "Parse error";
  
  public static final int METHOD_NOT_FOUND_CODE = -32601;
  
  public static final String METHOD_NOT_FOUND_MESSAGE = "Method not found";
  
  public static final int SERVER_ERROR_CODE = -32000;
  
  public static final String SERVER_ERROR_MESSAGE = "Server error";
  
  public static final int INVALID_PARAMS_CODE = -32602;
  
  public static final String INVALID_PARAMS_MESSAGE = "Invalid params";

  private static final long serialVersionUID = 7593028657234184051L;

  private static final String CODE = "code";

  private static final String MESSAGE = "message";

  private static final String DATA = "data";

  private final int code;

  private final Payload data;

  /**
   * Construct a new instance of {@link RPCError}.
   * 
   * @param code
   *          The RPC error code.
   * @param message
   *          The RPC error message.
   * @param data
   *          The RPC error payload.
   * @param throwable
   *          The causer of the error.
   */
  private RPCError(int code, String message, Payload data, Throwable throwable) {
    super(message, throwable);
    this.code = code;
    this.data = data;
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
    this(code, message, (Payload) null, null);
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
    this(code, message, (Payload) null, throwable);
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
  public RPCError(int code, String message, Object payload) {
    this(code, message, new Payload(payload), null);
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
   * @param throwable
   *          The causer of the error.
   */

  public RPCError(int code, String message, Object payload, Throwable throwable) {
    this(code, message, new Payload(payload), throwable);
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.jsonrpc.IPayload#hasPayload()
   */
  @Override
  public boolean hasPayload() {
    return data != null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.jsonrpc.IPayload#isList()
   */
  @Override
  public boolean isList() {
    if (!hasPayload())
      return false;
    return data.isList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.jsonrpc.IPayload#isMap()
   */
  @Override
  public boolean isMap() {
    if (!hasPayload())
      return false;
    return data.isMap();
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.jsonrpc.IPayload#getMap()
   */
  @Override
  public Map<String, Object> getMap() {
    if (!hasPayload())
      return null;
    return data.getMap();
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.jsonrpc.IPayload#getList()
   */
  @Override
  public List<Object> getList() {
    if (!hasPayload())
      return null;
    return data.getList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.jsonrpc.IPayload#getPayload()
   */
  @Override
  public Object getPayload() {
    if (!hasPayload())
      return null;
    return data.getPayload();
  }

  /**
   * Get RPC error code.
   * 
   * @return
   */
  public int getCode() {
    return code;
  }

  /**
   * An alias for {@link RPCError#getPayload()}.
   * 
   * @return
   */
  public Object getData() {
    return getPayload();
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.jsonrpc.JSONSerializable#toJSON()
   */
  @Override
  public Object toJSON() throws JSONException {
    JSONObject object = new JSONObject();

    object.put(CODE, code);

    if (getMessage() != null)
      object.put(MESSAGE, getMessage());

    if (hasPayload()) {
      if (isList())
        object.put(DATA, Message.wrap(getList()));
      else if (isMap())
        object.put(DATA, Message.wrap(getMap()));
      else
        object.put(DATA, getPayload());
    }
    return object;
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.jsonrpc.JSONSerializable#toJSONString()
   */
  @Override
  public String toJSONString() throws JSONException {
    return toJSON().toString();
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + code;
    result = prime * result + ((data == null) ? 0 : data.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RPCError other = (RPCError) obj;
    if (code != other.code)
      return false;
    if (data == null) {
      if (other.data != null)
        return false;
    } else if (!data.equals(other.data))
      return false;
    return true;
  }

}
