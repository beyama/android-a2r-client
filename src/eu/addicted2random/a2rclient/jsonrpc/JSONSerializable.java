package eu.addicted2random.a2rclient.jsonrpc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Base interface for all JSON-RPC 2 objects which are able
 * to serialize its content to JSON.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public interface JSONSerializable {

  /**
   * Must return a {@link JSONObject} or a {@link JSONArray}.
   * 
   * @return
   */
  Object toJSON() throws JSONException;
  
  /**
   * Must return a {@link String} with JSON data.
   * 
   * @return
   * @throws JSONException
   */
  String toJSONString() throws JSONException;
  
}
