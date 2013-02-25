package eu.addicted2random.a2rclient.test.jsonrpc;

import java.lang.reflect.Method;

import junit.framework.TestCase;
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
      return new Result(request.getId(), request.getPayload());
    }
    
    // return type result without taking the request isn't allowed
    @SuppressWarnings("unused")
    public Result illegal(int x) {
      return null;
    }

  }

  public void testEndpoint() throws NoSuchMethodException {
    RPCService service = new RPCService();

    // void method(void)
    Method method = RPCService.class.getMethod("getNothingAndTakeNothing", new Class<?>[] {});

    RPCEndpoint endpoint = new MethodInvokingRPCEndpoint(service, method);

    Response response = endpoint.call(new Request(1, "foo"));
    assertTrue(response instanceof Result);
    assertEquals(1L, response.getId());
    assertNull(response.getPayload());

    // int method(void)
    method = RPCService.class.getMethod("getAnIntAndTakeNothing", new Class<?>[] {});

    endpoint = new MethodInvokingRPCEndpoint(service, method);

    response = endpoint.call(new Request(2, "foo"));
    assertTrue(response instanceof Result);
    assertEquals(2L, response.getId());
    assertEquals(5, response.getPayload());

    // int method(int)
    method = RPCService.class.getMethod("powerOfTwo", new Class<?>[] { Integer.TYPE });

    endpoint = new MethodInvokingRPCEndpoint(service, method);

    response = endpoint.call(new Request(3, "foo", new Object[] { 4 }));
    
    assertTrue(response instanceof Result);
    assertEquals(3L, response.getId());
    assertEquals(16, response.getPayload());

    // double method(double, double)
    method = RPCService.class.getMethod("pow", new Class<?>[] { Double.TYPE, Double.TYPE });

    endpoint = new MethodInvokingRPCEndpoint(service, method);

    response = endpoint.call(new Request(4, "foo", new Object[] { 8d, 8d }));
    
    assertTrue(response instanceof Result);
    assertEquals(4L, response.getId());
    assertEquals(Math.pow(8, 8), response.getPayload());
    
    // Result method(Request)
    method = RPCService.class.getMethod("echo", new Class<?>[] { Request.class });

    endpoint = new MethodInvokingRPCEndpoint(service, method);

    response = endpoint.call(new Request(5, "foo", new Object[] { "bar" }));
    
    assertTrue(response instanceof Result);
    assertEquals(5L, response.getId());
    assertEquals("bar", response.getList().get(0));
    
    // Result method(int)
    method = RPCService.class.getMethod("illegal", new Class<?>[] { Integer.TYPE });

    try {
      new MethodInvokingRPCEndpoint(service, method);
      fail("No exception caught");
    } catch(IllegalArgumentException e) {
      // okay
    }
  }

}
