package eu.addicted2random.a2rclient.jsonrpc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a JSON-RPC 2 request.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class Request extends Message {

  private static final String METHOD_IS_NULL_EXCEPTION = "method must not be null";

  private static final String METHOD_IS_EMPTY_EXCEPTION = "method must not be an empty string";
  
  private static final String INVALID_ID_EXCEPTION = "id must be null, a number or a string";

  private final Object id;

  private final String method;

  private final Object params;

  /**
   * Private constructor for Jackson.
   * 
   * @param id
   * @param method
   * @param params
   */
  @JsonCreator
  private Request(@JsonProperty("id") Object id, @JsonProperty(value = "method", required = true) String method,
      @JsonProperty("params") JsonNode params) {
    this(id, method, (Object) params);
  }

  /**
   * Construct a new instance of {@link Request}.
   * 
   * @param id
   *          The request id must be null, a {@link String} or a {@link Number}.
   *          A numerical ID should not have fractional parts, the number will
   *          be converted to a {@link Long}.
   * @param method
   * @param params
   */
  public Request(Object id, String method, Object params) {
    super();

    if(id != null) {
      if(id instanceof String)
        if(((String) id).length() == 0)
          throw new IllegalArgumentException(INVALID_ID_EXCEPTION);
        else
          this.id = id;
      else if(id instanceof Number)
        this.id = ((Number) id).longValue();
      else
        throw new IllegalArgumentException(INVALID_ID_EXCEPTION);
    } else {
      this.id = null;
    }

    if (method == null)
      throw new IllegalArgumentException(METHOD_IS_NULL_EXCEPTION);
    if (method.length() == 0)
      throw new IllegalArgumentException(METHOD_IS_EMPTY_EXCEPTION);
    this.method = method;

    if(params instanceof Object[]) {
      List<Object> list = new ArrayList<Object>();
      Collections.addAll(list, (Object[])params);
      this.params = list;
    } else {
      this.params = params;
    }
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
   * @param method
   * @param params
   */
  public Request(String method, Object params) {
    this(null, method, params);
  }

  /**
   * Construct a new instance of {@link Request} without an id (a notification)
   * and without params.
   * 
   * @see Request#Request(Object, String, Object)
   * 
   * @param method
   */
  public Request(String method) {
    this((Object) null, method);
  }

  /**
   * Get the JSON-RPC id.
   * 
   * @return
   */
  @JsonProperty(required = false)
  @JsonInclude(Include.NON_NULL)
  public Object getId() {
    return id;
  }

  /**
   * Get the JSON-RPC method name.
   * 
   * @return
   */
  @JsonProperty(required = true)
  public String getMethod() {
    return method;
  }

  /**
   * Get params.
   * 
   * @return
   */
  @JsonProperty
  @JsonInclude(Include.NON_NULL)
  public Object getParams() {
    return params;
  }

  /**
   * This returns true if id is null.
   * 
   * @return
   */
  public boolean isNotification() {
    return getId() == null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((method == null) ? 0 : method.hashCode());
    result = prime * result + ((params == null) ? 0 : params.hashCode());
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
    Request other = (Request) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (method == null) {
      if (other.method != null)
        return false;
    } else if (!method.equals(other.method))
      return false;
    if (params == null) {
      if (other.params != null)
        return false;
    } else if (!params.equals(other.params))
      return false;
    return true;
  }

}
