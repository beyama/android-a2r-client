package eu.addicted2random.a2rclient.test.jsonrpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.addicted2random.a2rclient.jsonrpc.Payload;
import junit.framework.TestCase;

public class PayloadTest extends TestCase {

  public void testConstructor() throws JSONException {
    // default constructor
    Payload load = new Payload();
    assertNull(load.getPayload());
    assertNull(load.getList());
    assertNull(load.getMap());
    assertFalse(load.hasPayload());
    assertFalse(load.isList());
    assertFalse(load.isMap());
    
    assertEquals(JSONObject.NULL, load.toJSON());
    
    // with payload
    Object payload = new Object();
    load = new Payload(payload);
    assertEquals(payload, load.getPayload());
    assertNull(load.getList());
    assertNull(load.getMap());
    assertTrue(load.hasPayload());
    assertFalse(load.isList());
    assertFalse(load.isMap());
    
    assertEquals(JSONObject.NULL, load.toJSON());
  }
  
  public void testListPayload() throws JSONException {
    List<Object> payload = new ArrayList<Object>();
    String foo = "foo";
    payload.add(foo);
    
    Payload load = new Payload(payload);
    assertEquals(payload, load.getPayload());
    assertEquals(payload, load.getList());
    assertNull(load.getMap());
    assertTrue(load.hasPayload());
    assertTrue(load.isList());
    assertFalse(load.isMap());
    
    JSONArray json = (JSONArray)load.toJSON();
    assertEquals(foo, json.get(0));
    assertEquals(1, json.length());
  }
  
  public void testMapPayload() throws JSONException {
    Map<String, Object> payload = new HashMap<String, Object>();
    payload.put("key", Integer.valueOf(5));
    
    Payload load = new Payload(payload);
    assertEquals(payload, load.getPayload());
    assertEquals(payload, load.getMap());
    assertNull(load.getList());
    assertTrue(load.hasPayload());
    assertTrue(load.isMap());
    assertFalse(load.isList());
    
    JSONObject json = (JSONObject)load.toJSON();
    assertEquals(Integer.valueOf(5), Integer.valueOf(json.getInt("key")));
    assertEquals(1, json.length());
  }
  
}
