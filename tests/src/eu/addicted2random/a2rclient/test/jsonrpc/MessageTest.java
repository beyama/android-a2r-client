package eu.addicted2random.a2rclient.test.jsonrpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;

import eu.addicted2random.a2rclient.jsonrpc.Message;
import eu.addicted2random.a2rclient.jsonrpc.RPCError;
import eu.addicted2random.a2rclient.jsonrpc.Request;
import eu.addicted2random.a2rclient.jsonrpc.Result;
import eu.addicted2random.a2rclient.jsonrpc.Error;

public class MessageTest extends TestCase {
  
  static class Address {
    @JsonProperty
    String name;
  
    @JsonCreator
    public Address(@JsonProperty("name") String name) {
      super();
      this.name = name;
    }
    
  }
  
  public void testRequest() throws IOException {
    String json;
    JsonNode root;
    
    
    // only method
    Request req = new Request("foo");
    assertNull(req.getId());
    assertNull(req.getParams());
    assertEquals("foo", req.getMethod());
    
    json = Message.toJsonString(req);
    root = Message.getMapper().readTree(json);

    assertEquals("2.0", root.get("jsonrpc").asText());
    assertFalse(root.has("id"));
    assertEquals(req.getMethod(), root.get("method").asText());
    assertFalse(root.has("params"));
    
    // id and method
    Long id = 5l;
    req = new Request(id, "foo");
    assertEquals(req.getId(), id);
    assertNull(req.getParams());
    assertEquals("foo", req.getMethod());
    
    json = Message.toJsonString(req);
    root = Message.getMapper().readTree(json);
    
    assertEquals("2.0", root.get("jsonrpc").asText());
    assertEquals(req.getId(), root.get("id").asLong());
    assertEquals(req.getMethod(), root.get("method").asText());
    assertFalse(root.has("params"));
    
    // id, method and list payload
    List<Address> addresses = new ArrayList<MessageTest.Address>();
    addresses.add(new Address("John Doe"));
    
    req = new Request("the uuid", "rpcMethod", addresses);
    assertEquals(req.getId(), "the uuid");
    assertEquals(addresses, req.getParams());
    assertEquals("rpcMethod", req.getMethod());
    
    json = Message.toJsonString(req);
    root = Message.getMapper().readTree(json);
    
    assertEquals("2.0", root.get("jsonrpc").asText());
    assertEquals(req.getId(), root.get("id").asText());
    assertEquals(req.getMethod(), root.get("method").asText());
    assertTrue(root.has("params"));
    
    // parse back
    req = Message.fromJson(json).asRequest();
    
    assertEquals(req.getId(), "the uuid");
    assertTrue(req.getParams() instanceof ArrayNode);
    assertEquals("rpcMethod", req.getMethod());
    
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
  
  public void testResult() throws IOException {
    String json;
    JsonNode root;
    
    Result res = new Result(5);
    assertNull(res.getResult());
    assertEquals(5l, res.getId());
    
    json = Message.toJsonString(res);
    root = Message.getMapper().readTree(json);
    
    assertEquals("2.0", root.get("jsonrpc").asText());
    assertEquals(5l, root.get("id").asLong());
    assertFalse(root.has("result"));
    
    // with list payload
    List<Address> addresses = new ArrayList<MessageTest.Address>();
    addresses.add(new Address("John Doe"));
    
    res = new Result(5, addresses);
    assertEquals(5l, res.getId());
    assertEquals(addresses, res.getResult());
    
    json = Message.toJsonString(res);
    root = Message.getMapper().readTree(json);
    
    assertEquals("2.0", root.get("jsonrpc").asText());
    assertEquals(res.getId(), root.get("id").asLong());
    assertTrue(root.has("result"));
    
    // parse back
    res = Message.fromJson(json).asResult();
    
    assertEquals(5L, res.getId());
    assertTrue(res.getResult() instanceof ArrayNode);
  }
  
  public void testError() throws JsonProcessingException, IOException {
    String json;
    JsonNode root;
    
    Error err = new Error(5, 1, "timeout", 5000l);
    
    assertEquals(5l, err.getId());
    
    RPCError rpcError = err.getError();
    assertEquals(1, rpcError.getCode());
    assertEquals("timeout", rpcError.getMessage());
    assertEquals(5000L, rpcError.getData());
    
    json = Message.toJsonString(err);
    root = (JsonNode)Message.getMapper().readTree(json);
    
    assertEquals("2.0", root.get("jsonrpc").asText());
    assertEquals(err.getId(), root.get("id").asLong());
    
    JsonNode error = root.get("error");
    
    assertEquals(rpcError.getCode(), error.get("code").asInt());
    assertEquals(rpcError.getMessage(), error.get("message").asText());
    assertEquals(rpcError.getData(), error.get("data").asLong());
    
    // parse back
    err = Message.fromJson(json).asError();
    assertEquals(5L, err.getId());
    assertEquals(1, err.getCode());
    assertEquals("timeout", err.getMessage());
    assertEquals(5000L, ((IntNode)err.getData()).asLong());
  }

}
