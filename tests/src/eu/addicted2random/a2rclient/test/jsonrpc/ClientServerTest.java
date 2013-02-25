package eu.addicted2random.a2rclient.test.jsonrpc;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.TestCase;
import eu.addicted2random.a2rclient.jsonrpc.Error;
import eu.addicted2random.a2rclient.jsonrpc.JSONRPCEndpoint;
import eu.addicted2random.a2rclient.jsonrpc.Message;
import eu.addicted2random.a2rclient.jsonrpc.RPCClient;
import eu.addicted2random.a2rclient.jsonrpc.RPCEndpoint;
import eu.addicted2random.a2rclient.jsonrpc.RPCServer;
import eu.addicted2random.a2rclient.jsonrpc.Request;
import eu.addicted2random.a2rclient.jsonrpc.Response;
import eu.addicted2random.a2rclient.jsonrpc.ResponseCallback;
import eu.addicted2random.a2rclient.jsonrpc.Result;

public class ClientServerTest extends TestCase {

  private class MockClient extends RPCClient {
    private final Queue<Message> queue = new LinkedList<Message>();

    public MockClient(RPCServer server) {
      super(server);
    }

    @SuppressWarnings("unused")
    public void receive(Object data) {
      this.handle(data);
    }

    @Override
    public void onResponse(Response response) {
      this.queue.add(response);
    }

    @Override
    public void onMessage(Message message) {
      this.queue.add(message);
    }

    public Queue<Message> getQueue() {
      return queue;
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

    // call foo
    Request request = new Request(1, "foo");

    Future<Response> future = client.handle(request);

    Result result = (Result) future.get();

    assertEquals("bar", result.getResult());
    assertEquals(result, client.getQueue().remove());

    // call error
    request = new Request(1, "error");

    future = client.handle(request);

    Error error = (Error) future.get();

    assertEquals(5, error.getCode());
    assertEquals("an error", error.getMessage());
    assertEquals(error, client.getQueue().remove());

    // call throw
    request = new Request(2, "throws");

    future = client.handle(request);

    error = (Error) future.get();

    assertEquals(-32000, error.getCode());
    assertEquals("Server error", error.getMessage());
    assertTrue(error.getError().getCause() instanceof NullPointerException);
    assertEquals(error, client.getQueue().remove());

    final Error[] errorStore = new Error[] { null };

    // test timeout
    client.call("foo", 2, new ResponseCallback() {

      @Override
      public void onResponse(Response response) {
        assertTrue(response instanceof Error);
        Error error = (Error) response;
        assertEquals("timeout", error.getMessage());
        assertEquals(2, ((Number) error.getData()).intValue());
        errorStore[0] = error;
      }

    });

    synchronized (this) {
      wait(50);
      assertNotNull(errorStore[0]);
    }

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
    
    Result result = (Result)future.get();
    
    assertEquals(9L, result.getId());
    assertEquals("method1 called", result.getPayload());
    
    future = client.handle(new Request(10, "myService.method2"));
    
    result = (Result)future.get();
    
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
