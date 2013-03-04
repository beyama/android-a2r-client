package eu.addicted2random.a2rclient.jsonrpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a JSON-RPC 2 result.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class Result extends Response {

  private final Object result;

  /**
   * Private constructor for Jackson.
   * 
   * @param id
   * @param result
   */
  @JsonCreator
  private Result(@JsonProperty(value = "id", required = true) Object id, @JsonProperty("result") JsonNode result) {
    this(id, (Object) result);
  }

  /**
   * Construct a new instance of {@link Result}.
   * 
   * @param id
   * @param result
   */
  public Result(Object id, Object result) {
    super(id);
    this.result = result;
  }

  /**
   * Construct a new instance of {@link Result} without result data.
   * 
   * @param id
   */
  public Result(Object id) {
    this(id, null);
  }

  /**
   * An alias for {@link Result#getPayload()}.
   * 
   * @return
   */
  @JsonProperty
  @JsonInclude(Include.NON_NULL)
  public Object getResult() {
    return result;
  }

}
