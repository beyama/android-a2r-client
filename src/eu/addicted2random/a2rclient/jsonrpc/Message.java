package eu.addicted2random.a2rclient.jsonrpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Abstract base class of all JSON-RPC 2 messages.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public abstract class Message extends Payload {

  private static final String DATA = "data";

  private static final String PARAMS = "params";

  private static final String MESSAGE = "message";

  private static final String METHOD = "method";

  private static final String ERROR = "error";

  private static final String RESULT = "result";

  private static final String CODE = "code";
  
  private static final String ID = "id";

  private static final String JSONRPC = "jsonrpc";
  
  /**
   * Get a JSON Object for a {@link Map}.
   * 
   * @param map
   * @return
   * @throws JSONException
   */
  @SuppressWarnings("unchecked")
  static public JSONObject wrap(Map<String, Object> map) throws JSONException {
    JSONObject object = new JSONObject();
    
    for(Map.Entry<String, Object> entry : map.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      
      if(value == null)
        object.put(key, JSONObject.NULL);
      else if(value instanceof Map)
        object.put(key, wrap((Map<String, Object>)value));
      else if(value instanceof List)
        object.put(key, wrap((List<Object>)value));
      else
        object.put(key, value);
    }
    return object;
  }
  
  /**
   * Get a {@link JSONArray} for a {@link List}.
   * 
   * @param list
   * @return
   * @throws JSONException
   */
  @SuppressWarnings("unchecked")
  static public JSONArray wrap(List<Object> list) throws JSONException {
    JSONArray array = new JSONArray();
    
    for(Object object : list) {
      if(object == null)
        array.put(JSONObject.NULL);
      else if(object instanceof Map)
        array.put(wrap((Map<String, Object>)object));
      else if(object instanceof List)
        array.put(wrap((List<Object>)object));
      else
        array.put(object);
    }
    return array;
  }

  /**
   * Parse a JSON string and return the first value.
   * 
   * @param jsonString The string with JSON data.
   * @return The first object from JSON string.
   * @throws RPCError A parse error if something goes wrong.
   */
  static public Object parseJSON(String jsonString) throws RPCError {
    try {
      return new JSONTokener(jsonString).nextValue();
    } catch (JSONException e) {
      throw new RPCError(RPCError.PARSE_ERROR_CODE, RPCError.PARSE_ERROR_MESSAGE, e);
    }
  }
  
  /**
   * Unwrap a JSON object.
   * 
   * @param object The object to unwrap.
   * @return 
   *    Returns a {@link List} of {@link Object}s if the object is a {@link JSONArray},
   *    a {@link Map} of {@link String} {@link Object} pairs if the object is
   *    a {@link JSONObject}, null if the object is {@link JSONObject#NULL} otherwise
   *    the object itself.
   * 
   * @throws JSONException
   */
  static public Object unwrap(Object object) throws JSONException {
    if(object instanceof JSONObject)
      return unwrap((JSONObject)object);
    else if(object instanceof JSONArray)
      return unwrap((JSONArray)object);
    else if(object == JSONObject.NULL)
      return null;
    return object;
  }
  
  /**
   * Returns a {@link Map} of {@link String} {@link Object} pairs with the content of
   * the {@link JSONObject}.
   * 
   * @param object The {@link JSONObject} to unwrap.
   * @return The map with the unwrapped content of the {@link JSONObject}.
   * @throws JSONException
   */
  static public Map<String, Object> unwrap(JSONObject object) throws JSONException {
    Map<String, Object> map = new HashMap<String, Object>(object.length());
    
    @SuppressWarnings("unchecked")
    Iterator<String> keys = (Iterator<String>)object.keys();
    
    while(keys.hasNext()) {
      String key = keys.next();
      Object value = object.get(key);
      map.put(key, unwrap(value));
    }
    
    return map;
  }
  
  /**
   * Returns a {@link List} of {@link Object}s with the content of
   * the {@link JSONArray}.
   * 
   * @param array The {@link JSONArray} to unwrap.
   * @return The list with the unwrapped content of the {@link JSONArray}.
   * @throws JSONException
   */
  static public List<Object> unwrap(JSONArray array) throws JSONException {
    int length = array.length();
    
    List<Object> list = new ArrayList<Object>(length);
    
    for(int i = 0; i < length; i++) {
      list.add(unwrap(array.get(i)));
    }
    
    return list;
  }

  /**
   * Construct a {@link Message} from a JSON object.
   * 
   * @param json The JSON data.
   * @return The newly constructed message.
   * @throws RPCError
   */
  @SuppressWarnings("unchecked")
  static public Message fromJSON(JSONObject json) throws RPCError {
    try {
      Map<String, Object> msg = unwrap(json);
      
      boolean hasResult = msg.containsKey(RESULT);
      boolean hasError = msg.containsKey(ERROR);
      
      if(hasResult || hasError) {
        if(hasResult && hasError)
          throw new RPCError(RPCError.PARSE_ERROR_CODE, RPCError.PARSE_ERROR_MESSAGE, "Either result or error must be given (but not both).");
        
        Object id = msg.get(ID);
        
        if(hasResult) {
          Object result = msg.get(RESULT);
          
          if(result instanceof List)
            return new Result(id, (List<Object>)result);
          else if(result instanceof Map)
            return new Result(id, (Map<String, Object>)result);
          else
            return new Result(id);
        // error
        } else {
          Map<String, Object> error = (Map<String, Object>)msg.get(ERROR);
          
          int code = ((Number)error.get(CODE)).intValue();
          String message = (String)error.get(MESSAGE);
          Object data = error.get(DATA);
          
          return new Error(id, code, message, data);
        }
        
      } else if(msg.containsKey(METHOD)) {
        Object id = msg.get(ID);
        String method = (String)msg.get(METHOD);
        Object params = msg.get(PARAMS);
        
        return new Request(id, method, params);
      } else {
        throw new RPCError(RPCError.PARSE_ERROR_CODE, RPCError.PARSE_ERROR_MESSAGE, "Invalid message.");
      }
    } catch (RPCError e) {
      throw e;
    } catch (Throwable e) {
      throw new RPCError(RPCError.PARSE_ERROR_CODE, RPCError.PARSE_ERROR_MESSAGE, e);
    }
  }

  /**
   * Build a {@link Message} from JSON if JSON data is {@link JSONObject} or a {@link List} of
   * {@link Request} messages if JSON data is {@link JSONArray}.
   * 
   * @param json The {@link JSONObject} or {@link JSONArray} with messages.
   * @return A list of {@link Request} objects or a {@link Message} object.
   * 
   * @throws RPCError
   */
  static public Object fromJSON(Object json) throws RPCError {
    if (json instanceof JSONObject) {
      return fromJSON((JSONObject)json);
    } else if (json instanceof JSONArray) {
      JSONArray array = (JSONArray) json;
      int length = array.length();
      
      List<Request> requests = new ArrayList<Request>(length);

      try {
        for (int i = 0; i < length; i++)
          requests.add((Request)fromJSON((JSONObject)array.get(i)));
      } catch (ClassCastException e) {
        throw new RPCError(RPCError.PARSE_ERROR_CODE, RPCError.PARSE_ERROR_MESSAGE, e);
      } catch (JSONException e) {
        throw new RPCError(RPCError.PARSE_ERROR_CODE, RPCError.PARSE_ERROR_MESSAGE, e);
      }
    }
    throw new RPCError(RPCError.PARSE_ERROR_CODE, RPCError.PARSE_ERROR_MESSAGE);
  }
  
  /**
   * Parse JSON data and pass the result to {@link Message#fromJSON(Object)} and return
   * the result.
   * 
   * @see Message#fromJSON(Object).
   * 
   * @param jsonString The JSON data string.
   * @return The result from {@link Message#fromJSON(Object)}.
   * @throws RPCError
   */
  static public Object fromJSON(String jsonString) throws RPCError {
    Object object = parseJSON(jsonString);
    return fromJSON(object);
  }

  private final Object id;

  /**
   * Construct a new {@link Message} with id and payload data.
   * 
   * @param id An ID of type null, {@link String} or {@link Number}.
   *    An ID of type {@link Number} will be converted to {@link Long}
   * @param payload
   */
  public Message(Object id, Object payload) {
    super(payload);
    
    if(id != null && id != JSONObject.NULL && !(id instanceof Number) && !(id instanceof String))
      throw new IllegalArgumentException("id must be null, a number of a string");
    
    if(id != null && id instanceof Number)
      this.id = ((Number)id).longValue();
    else
      this.id = id;
  }

  /**
   * Construct a new {@link Message} with id and no payload data.
   * 
   * @param id
   */
  public Message(Object id) {
    this(id, null);
  }

  /**
   * Get id of message.
   * 
   * @return
   */
  public Object getId() {
    return id;
  }

  /*
   * (non-Javadoc)
   * @see eu.addicted2random.a2rclient.jsonrpc.Payload#toJSON()
   */
  @Override
  public Object toJSON() throws JSONException {
    JSONObject object = new JSONObject();
    object.put(JSONRPC, "2.0");

    if (getId() != null)
      object.put(ID, getId());

    return object;
  }

  /*
   * (non-Javadoc)
   * @see eu.addicted2random.a2rclient.jsonrpc.Payload#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * @see eu.addicted2random.a2rclient.jsonrpc.Payload#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    Message other = (Message) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

}
