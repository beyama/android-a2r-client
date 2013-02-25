package eu.addicted2random.a2rclient.test.jsonrpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.addicted2random.a2rclient.jsonrpc.Error;
import eu.addicted2random.a2rclient.jsonrpc.Message;
import eu.addicted2random.a2rclient.jsonrpc.RPCError;
import eu.addicted2random.a2rclient.jsonrpc.Request;
import eu.addicted2random.a2rclient.jsonrpc.Result;
import junit.framework.TestCase;

public class MessageTest extends TestCase {
  
  private Map<String, Object> getMapPayload() {
    Map<String, Object> map = new HashMap<String, Object>(3);
    List<Object> list = new ArrayList<Object>(2);
    
    map.put("one", 1);
    map.put("two", "two");
    map.put("three", list);
    
    list.add(2);
    list.add("bar");
    
    return map;
  }
  
  private void testMapPayload(Map<String, Object> map, JSONObject object) throws JSONException {
    assertEquals(map.size(), object.length());
    assertEquals(map.get("one"), object.getInt("one"));
    assertEquals(map.get("two"), object.getString("two"));
    
    @SuppressWarnings("unchecked")
    List<Object> list = (List<Object>)map.get("three");
    
    JSONArray array = object.getJSONArray("three");
    assertEquals(list.get(0), array.getInt(0));
    assertEquals(list.get(1), array.getString(1));
  }
  
  private List<Object> getListPayload() {
    List<Object> payload = new ArrayList<Object>(3);
    List<Object> nestedList = new ArrayList<Object>(2);
    
    payload.add(1);
    payload.add("foo");
    payload.add(nestedList);
    
    nestedList.add(2);
    nestedList.add("bar");
    return payload;
  }
  
  private void testListPayload(List<Object> payload, JSONArray array) throws JSONException {
    assertEquals(payload.size(), array.length());
    
    @SuppressWarnings("unchecked")
    List<Object> nestedList = (List<Object>)payload.get(2);
    
    assertEquals(payload.get(0), array.getInt(0));
    assertEquals(payload.get(1), array.getString(1));
    
    array = array.getJSONArray(2);
    assertEquals(nestedList.get(0), array.getInt(0));
    assertEquals(nestedList.get(1), array.getString(1));
  }
  
  public void testRequest() throws JSONException {
    // only method
    Request req = new Request("foo");
    assertNull(req.getId());
    assertNull(req.getPayload());
    assertEquals("foo", req.getMethod());
    
    JSONObject json = (JSONObject)req.toJSON();
    assertEquals("2.0", json.getString("jsonrpc"));
    assertFalse(json.has("id"));
    assertEquals(req.getMethod(), json.getString("method"));
    assertFalse(json.has("params"));
    
    // id and method
    Long id = 5l;
    req = new Request(id, "foo");
    assertEquals(req.getId(), id);
    assertNull(req.getPayload());
    assertEquals("foo", req.getMethod());
    
    json = (JSONObject)req.toJSON();
    assertEquals("2.0", json.getString("jsonrpc"));
    assertEquals(req.getId(), json.getLong("id"));
    assertEquals(req.getMethod(), json.getString("method"));
    assertFalse(json.has("params"));
    
    // id, method and list payload
    List<Object> payload = getListPayload();
    
    req = new Request("the uuid", "rpcMethod", payload);
    assertEquals(req.getId(), "the uuid");
    assertEquals(payload, req.getList());
    assertEquals("rpcMethod", req.getMethod());
    
    json = (JSONObject)req.toJSON();
    assertEquals("2.0", json.getString("jsonrpc"));
    assertEquals(req.getId(), json.getString("id"));
    assertEquals(req.getMethod(), json.getString("method"));
    assertTrue(json.has("params"));
    
    JSONArray array = (JSONArray)json.getJSONArray("params");
    testListPayload(payload, array);
    
    // id, method and map payload
    Map<String, Object> mapPayload = getMapPayload();
    
    req = new Request("the uuid", "rpcMethod", mapPayload);
    assertEquals(req.getId(), "the uuid");
    assertEquals(mapPayload, req.getMap());
    assertEquals("rpcMethod", req.getMethod());
    
    json = (JSONObject)req.toJSON();
    assertEquals("2.0", json.getString("jsonrpc"));
    assertEquals(req.getId(), json.getString("id"));
    assertEquals(req.getMethod(), json.getString("method"));
    assertTrue(json.has("params"));
    
    
    JSONObject object = json.getJSONObject("params");
    testMapPayload(mapPayload, object);
    
    // test illegal arguments
    try {
      req = new Request(null);
      fail("No exception caught");
    } catch (IllegalArgumentException e) {
      // Okay
    }
    
    try {
      req = new Request("");
      fail("No exception caught");
    } catch (IllegalArgumentException e) {
      // Okay
    }
    
    try {
      req = new Request(new Object(), "foo");
      fail("No exception caught");
    } catch (IllegalArgumentException e) {
      // Okay
    }
    
    // convert numerical ids
    req = new Request(5f, "foo");
    assertEquals(req.getId(), 5l);
  }
  
  public void testResult() throws JSONException {
    Result res = new Result(5);
    assertNull(res.getResult());
    assertEquals(5l, res.getId());
    
    JSONObject json = (JSONObject)res.toJSON();
    assertEquals("2.0", json.getString("jsonrpc"));
    assertEquals(5l, json.getLong("id"));
    assertFalse(json.has("result"));
    
    // with list payload
    List<Object> listPayload = getListPayload();
    res = new Result(5, listPayload);
    assertEquals(5l, res.getId());
    assertEquals(listPayload, res.getResult());
    
    json = (JSONObject)res.toJSON();
    assertEquals("2.0", json.getString("jsonrpc"));
    assertEquals(5l, json.getLong("id"));
    testListPayload(listPayload, json.getJSONArray("result"));
    
    // with map payload
    Map<String, Object> mapPayload = getMapPayload();
    res = new Result(5, mapPayload);
    assertEquals(5l, res.getId());
    assertEquals(mapPayload, res.getResult());
    
    json = (JSONObject)res.toJSON();
    assertEquals("2.0", json.getString("jsonrpc"));
    assertEquals(5l, json.getLong("id"));
    testMapPayload(mapPayload, json.getJSONObject("result"));
  }
  
  public void testError() throws JSONException {
    Error err = new Error(5l, 1, "timeout", 5000l);
    
    assertEquals(5l, err.getId());
    
    RPCError rpcError = err.getError();
    assertEquals(1, rpcError.getCode());
    assertEquals("timeout", rpcError.getMessage());
  }
  
  public void testParseBack() throws JSONException {
    Request request = new Request(33l, "foo", getListPayload());
    assertTrue(Message.fromJSON(request.toJSON()).equals(request));
    
    Result result = new Result(8l, getMapPayload());
    assertTrue(Message.fromJSON(result.toJSON()).equals(result));
    
    Error error = new Error(9l, 2, "unexpected ...", 8);
    assertTrue(Message.fromJSON(error.toJSON()).equals(error));
  }

}
