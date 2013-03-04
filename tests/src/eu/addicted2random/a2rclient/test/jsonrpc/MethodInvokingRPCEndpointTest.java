package eu.addicted2random.a2rclient.test.jsonrpc;

import java.io.IOException;
import java.lang.reflect.Method;

import junit.framework.TestCase;

import com.fasterxml.jackson.databind.JsonNode;

import eu.addicted2random.a2rclient.jsonrpc.Message;
import eu.addicted2random.a2rclient.jsonrpc.MethodInvokingRPCEndpoint;
import eu.addicted2random.a2rclient.jsonrpc.RPCEndpoint;
import eu.addicted2random.a2rclient.jsonrpc.Request;
import eu.addicted2random.a2rclient.jsonrpc.Response;
import eu.addicted2random.a2rclient.jsonrpc.Result;

public class MethodInvokingRPCEndpointTest extends TestCase {

  private class RPCService {

    @SuppressWarnings("unused")
    public void getNothingAndTakeNothing() {
    }

    @SuppressWarnings("unused")
    public int getAnIntAndTakeNothing() {
      return 5;
    }

    @SuppressWarnings("unused")
    public int powerOfTwo(int i) {
      return Double.valueOf(Math.pow(Integer.valueOf(i), 2d)).intValue();
    }

    @SuppressWarnings("unused")
    public double pow(double x, double y) {
      return Math.pow(x, y);
    }
    
    @SuppressWarnings("unused")
    public Result echo(Request request) {
      return new Result(request.getId(), request.getParams());
    }
    
    // return type result without taking the request isn't allowed
    @SuppressWarnings("unused")
    public Result illegal(int x) {
      return null;
    }

  }
  
  public Response call(RPCEndpoint endpoint, Request request) throws IOException {
    String json = Message.toJsonString(request);
    return endpoint.call(Message.fromJson(json).asRequest());
  }

  public void testEndpoint() throws NoSuchMethodException, IOException {
    RPCService service = new RPCService();

    // void method(void)
    Method method = RPCService.class.getMethod("getNothingAndTakeNothing", new Class<?>[] {});

    RPCEndpoint endpoint = new MethodInvokingRPCEndpoint(service, method);

    Response response = endpoint.call(new Request(1, "foo"));
    assertTrue(response.isResult());
    assertEquals(1L, response.getId());
    assertNull(response.asResult().getResult());

    // int method(void)
    method = RPCService.class.getMethod("getAnIntAndTakeNothing", new Class<?>[] {});

    endpoint = new MethodInvokingRPCEndpoint(service, method);

    response = call(endpoint, new Request(2, "foo"));
    assertTrue(response.isResult());
    assertEquals(2L, response.getId());
    assertEquals(5, response.asResult().getResult());

    // int method(int)
    method = RPCService.class.getMethod("powerOfTwo", new Class<?>[] { Integer.TYPE });

    endpoint = new MethodInvokingRPCEndpoint(service, method);

    response = call(endpoint, new Request(3, "foo", new Object[] { 4 }));
    
    assertTrue(response.isResult());
    assertEquals(3L, response.getId());
    assertEquals(16, response.asResult().getResult());

    // double method(double, double)
    method = RPCService.class.getMethod("pow", new Class<?>[] { Double.TYPE, Double.TYPE });

    endpoint = new MethodInvokingRPCEndpoint(service, method);

    response = call(endpoint, new Request(4, "foo", new Object[] { 8d, 8d }));
    
    assertTrue(response.isResult());
    assertEquals(4L, response.getId());
    assertEquals(Math.pow(8, 8), response.asResult().getResult());
    
    // Result method(Request)
    method = RPCService.class.getMethod("echo", new Class<?>[] { Request.class });

    endpoint = new MethodInvokingRPCEndpoint(service, method);

    response = call(endpoint, new Request(5, "foo", "bar"));
       
    assertTrue(response.isResult());
    assertEquals(5L, response.getId());
    System.out.println(response.asResult().getResult().getClass().getName());
    assertEquals("bar", ((JsonNode)response.asResult().getResult()).asText());
  }

}
