package eu.addicted2random.a2rclient.jsonrpc;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a JSON-RPC 2 result.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class Result extends Response {

  private final static String RESULT = "result";

  /**
   * Construct a new instance of {@link Result}.
   * 
   * @param id
   * @param payload
   */
  public Result(Object id, Object payload) {
    super(id, payload);
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
  public Object getResult() {
    return getPayload();
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.jsonrpc.Message#toJSON()
   */
  @Override
  public Object toJSON() throws JSONException {
    JSONObject object = (JSONObject) super.toJSON();

    if (hasPayload()) {
      if (isMap())
        object.put(RESULT, Message.wrap(getMap()));
      else if (isList())
        object.put(RESULT, Message.wrap(getList()));
      else
        object.put(RESULT, getPayload());
    }

    return object;
  }

}
