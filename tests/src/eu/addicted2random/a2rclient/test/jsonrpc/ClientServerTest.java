package eu.addicted2random.a2rclient.test.jsonrpc;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.TestCase;
import eu.addicted2random.a2rclient.jsonrpc.Error;
import eu.addicted2random.a2rclient.jsonrpc.JSONRPCEndpoint;
import eu.addicted2random.a2rclient.jsonrpc.Message;
import eu.addicted2random.a2rclient.jsonrpc.RPCClient;
import eu.addicted2random.a2rclient.jsonrpc.RPCEndpoint;
import eu.addicted2random.a2rclient.jsonrpc.RPCError;
import eu.addicted2random.a2rclient.jsonrpc.RPCServer;
import eu.addicted2random.a2rclient.jsonrpc.Request;
import eu.addicted2random.a2rclient.jsonrpc.Response;
import eu.addicted2random.a2rclient.jsonrpc.Result;
import eu.addicted2random.a2rclient.utils.Promise;
import eu.addicted2random.a2rclient.utils.PromiseListener;

public class ClientServerTest extends TestCase {

  private class MockClient extends RPCClient {
    
    public MockClient(RPCServer server) {
      super(server);
    }

    @Override
    public void onMessage(Message message) {
      handle(message);
    }

  }

  public void testClientServer() throws InterruptedException, ExecutionException {
    RPCServer server = new RPCServer(Executors.newCachedThreadPool());
    MockClient client = new MockClient(server);

    // returns a result with "bar" as payload.
    server.registerEndpoint("foo", new RPCEndpoint() {
      @Override
      public Response call(Request request) {
        return new Result(request.getId(), "bar");
      }
    });

    // returns an error with "an error" as payload and 5 as error code
    server.registerEndpoint("error", new RPCEndpoint() {
      @Override
      public Response call(Request request) {
        return new Error(request.getId(), 5, "an error");
      }
    });

    // throws a NullPointerException
    server.registerEndpoint("throws", new RPCEndpoint() {
      @Override
      public Response call(Request request) {
        throw new NullPointerException();
      }
    });
    
    // runs 1000ms
    server.registerEndpoint("longRunning", new RPCEndpoint() {
      @Override
      public Response call(Request request) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        return new Result(request.getId());
      }
    });

    // call foo
    Promise<Response> promise = client.call("foo");
 
    Response response = promise.get();
    Result result = (Result) response;

    assertEquals("bar", result.getResult());

    // call error
    promise = client.call("error");

    Error error = (Error) promise.get();

    assertEquals(5, error.getCode());
    assertEquals("an error", error.getMessage());

    // test exception throwing endpoint
    promise = client.call("throws");

    error = (Error) promise.get();

    assertEquals(-32000, error.getCode());
    assertEquals("Server error", error.getMessage());
    assertTrue(error.getError().getCause() instanceof NullPointerException);

    // test timeout
    promise = client.call("longRunning", 2);
    try {
      response = promise.get();
      fail("No exception caught");
    } catch (Exception e) {
      assertTrue(e instanceof ExecutionException);
      RPCError rpcError = (RPCError)e.getCause();
      assertEquals("timeout", rpcError.getMessage());
      assertEquals(2, ((Number) rpcError.getData()).intValue());
    }
    
    // future callback
    final Response[] var = new Response[1];
    promise = client.call("foo");
    promise.addListener(new PromiseListener<Response>() {
      
      @Override
      public void opperationComplete(Promise<Response> result) {
        var[0] = result.getResult();
      }
      
    });
    
    Thread.sleep(10);
    assertEquals("bar", var[0].getPayload());
  }

  public void testExpose() throws InterruptedException, ExecutionException {
    RPCServer server = new RPCServer(Executors.newCachedThreadPool());
    MockClient client = new MockClient(server);

    Object service = new Object() {

      @JSONRPCEndpoint("helloWorld")
      public Response method1(Request req) {
        return new Result(req.getId(), "method1 called");
      }

      @JSONRPCEndpoint
      public Response method2(Request req) {
        return new Result(req.getId(), "method2 called");
      }

    };

    server.expose("myService", service);

    assertTrue(server.hasMethod("myService.helloWorld"));
    assertTrue(server.hasMethod("myService.method2"));

    Future<Response> future = client.handle(new Request(9, "myService.helloWorld"));

    Result result = (Result) future.get();

    assertEquals(9L, result.getId());
    assertEquals("method1 called", result.getPayload());

    future = client.handle(new Request(10, "myService.method2"));

    result = (Result) future.get();

    assertEquals(10L, result.getId());
    assertEquals("method2 called", result.getPayload());

    try {
      // should cleanup if processing failed
      server.expose("myService2", new Object() {

        @JSONRPCEndpoint
        public Response method1(Request req) {
          return new Result(req.getId(), "method1 called");
        }

        // not allowed
        @JSONRPCEndpoint
        public Response method2(int i) {
          return null;
        }

      });
      fail("No exception caught");
    } catch (Exception e) {
      // okay
    }
    assertFalse(server.hasMethod("myService2.method1"));
    assertFalse(server.hasMethod("myService2.method2"));

    // should not register a service twice
    try {
      server.expose("myService", service);
      fail("No exception caught");
    } catch (Exception e) {
      // okay
    }
    assertTrue(server.hasMethod("myService.helloWorld"));
    assertTrue(server.hasMethod("myService.method2"));

    // unexpose service
    server.unexpose(service);
    assertFalse(server.hasMethod("myService.helloWorld"));
    assertFalse(server.hasMethod("myService.method2"));
  }

}
