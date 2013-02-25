package eu.addicted2random.a2rclient.jsonrpc;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a JSON-RPC 2 request.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class Request extends Message {

  private static final String PARAMS = "params";

  private static final String METHOD = "method";

  private final String METHOD_IS_NULL_EXCEPTION = "method must not be null";

  private final String METHOD_IS_EMPTY_EXCEPTION = "method must not be an empty string";

  private final String method;

  /**
   * Construct a new instance of {@link Request}.
   * 
   * @param id
   *          The request id must be null, a {@link String} of a {@link Number}.
   *          A numerical ID should not have fractional parts, the number will
   *          be converted to a {@link Long}.
   * @param method
   * @param payload
   */
  public Request(Object id, String method, Object payload) {
    super(id, payload);
    if (method == null)
      throw new IllegalArgumentException(METHOD_IS_NULL_EXCEPTION);
    if (method.length() == 0)
      throw new IllegalArgumentException(METHOD_IS_EMPTY_EXCEPTION);
    this.method = method;

    if (payload != null && payload != JSONObject.NULL && !(isMap()) && !(isList()))
      throw new IllegalArgumentException("request payload must be null, a list or a map");
  }

  /**
   * Construct a new instance of {@link Request} without params.
   * 
   * @see Request#Request(Object, String, Object)
   * 
   * @param id
   * @param method
   */
  public Request(Object id, String method) {
    this(id, method, null);
  }

  /**
   * Construct a new instance of {@link Request} without an id (a notification).
   * 
   * @see Request#Request(Object, String, Object)
   * 
   * @param id
   * @param method
   */
  public Request(String method, Object payload) {
    this(null, method, payload);
  }

  /**
   * Construct a new instance of {@link Request} without an id (a notification)
   * and without params.
   * 
   * @see Request#Request(Object, String, Object)
   * 
   * @param id
   * @param method
   */
  public Request(String method) {
    this((Object) null, method);
  }

  /**
   * This returns true if id is null.
   * 
   * @return
   */
  public boolean isNotification() {
    return getId() == null;
  }

  /**
   * Get the JSON-RPC method name.
   * @return
   */
  public String getMethod() {
    return method;
  }

  /**
   * An alias for {@link Request#getPayload()}.
   * @return
   */
  public Object getParams() {
    return getPayload();
  }

  /*
   * (non-Javadoc)
   * @see eu.addicted2random.a2rclient.jsonrpc.Message#toJSON()
   */
  @Override
  public Object toJSON() throws JSONException {
    JSONObject object = (JSONObject) super.toJSON();

    object.put(METHOD, method);

    if (isList())
      object.put(PARAMS, Message.wrap(getList()));
    else if (isMap())
      object.put(PARAMS, Message.wrap(getMap()));

    return object;
  }

  /*
   * (non-Javadoc)
   * @see eu.addicted2random.a2rclient.jsonrpc.Message#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((method == null) ? 0 : method.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * @see eu.addicted2random.a2rclient.jsonrpc.Message#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    Request other = (Request) obj;
    if (method == null) {
      if (other.method != null)
        return false;
    } else if (!method.equals(other.method))
      return false;
    return true;
  }

}
