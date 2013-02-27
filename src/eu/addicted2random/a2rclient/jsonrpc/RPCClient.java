package eu.addicted2random.a2rclient.jsonrpc;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.addicted2random.a2rclient.utils.Promise;

/**
 * JSON-RPC client. This class can be used to send RPC Requests to a remote
 * JSON-RPC 2 Server. if bound to a local {@link RPCServer} this class will acts
 * as glue layer between your network connection and the local {@link RPCServer}
 * .
 * 
 * If you like to extend this class you should override
 * {@link RPCClient#onMessage(Message)} to send the messages to your remote
 * endpoint. The received JSON data must be passed to one of the 'handle'
 * methods.
 * 
 * If you don't like to subclass this class, you can register a
 * {@link MessageCallback} via
 * {@link RPCClient#setMessageCallback(MessageCallback)} to receive the data
 * which must be sent to the remote endpoint.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class RPCClient implements ResponseCallback {

  /**
   * Request task to handle request timeouts and to store a
   * {@link ResponsePromise} in the requests map.
   */
  private class RequestTask extends TimerTask {
    final Object id;
    final int timeout;
    final Promise<Response> promise;

    public RequestTask(Object id, int timeout, Promise<Response> promise) {
      super();
      this.id = id;
      this.timeout = timeout;
      this.promise = promise;

      timer.schedule(this, timeout);
    }

    @Override
    public void run() {
      Promise<Response> promise = removeRequest(id);
      if (promise != null) {
        if (promise.isDone())
          return;
        RPCError error = new RPCError(1, "timeout", timeout);
        promise.failure(error);
      }
    }
  }

  /* a weak reference to the bound local RPC server */
  private final WeakReference<RPCServer> server;

  /* callback to handle messages that should be sent to the remote. */
  private volatile MessageCallback messageCallback;

  /* request id to RequestTask map */
  private final Map<Object, RequestTask> requests = new HashMap<Object, RequestTask>();

  /* timeout timer */
  private final Timer timer = new Timer();

  /* to generate IDs for requests */
  private final AtomicLong id = new AtomicLong(1);

  /**
   * Construct a new instance of {@link RPCClient} bound to a local
   * {@link RPCServer} to handle requests from a remote.
   * 
   * @param server
   *          The local {@link RPCServer}.
   */
  public RPCClient(RPCServer server) {
    this.server = new WeakReference<RPCServer>(server);
  }

  /**
   * Construct a new instance of {@link RPCClient} without binding to a local
   * {@link RPCServer}.
   */
  public RPCClient() {
    this.server = null;
  }

  /**
   * Handle a {@link Request}. This passes the {@link Request} to the
   * {@link RPCServer} and registers itself as {@link ResponseCallback}. The
   * received {@link Response} will be passed to
   * {@link RPCClient#onMessage(Message)}.
   * 
   * @param request
   *          The request object.
   * @return The response future or null if this client isn't bound to a local
   *         {@link RPCServer}.
   */
  public Future<Response> handle(Request request) {
    if (this.server == null)
      return null;

    RPCServer server = this.server.get();
    if (server == null)
      return null;

    return server.call(request, this);
  }

  /**
   * Handle a response from the remote server.
   * 
   * @param response
   */
  public synchronized void handle(Response response) {
    Promise<Response> promise = removeRequest(response.getId());

    if (promise != null) {
      try {
        promise.success(response);
      } catch (Exception e) {
      }
    }
  }

  /**
   * Handle a list of requests. This handles batch requests.
   * 
   * @param requests
   */
  public synchronized void handle(List<Request> requests) {
    for (Request request : requests)
      handle(request);
  }

  /**
   * Generic handle method. Deals with unparsed JSON strings, {@link JSONObject}
   * s and {@link Message}s.
   * 
   * E.g. If you like to interact with this client from a network connection and
   * your receive JSON strings from your remote, then you can simply pass your
   * string data to this method.
   * 
   * @param object
   *          A JSON {@link String}, {@link JSONObject}, {@link JSONArray},
   *          {@link Request}, {@link Response} or a {@link List} of
   *          {@link Request}s.
   */
  public void handle(Object object) {
    if (object instanceof String)
      object = Message.fromJSON((String) object);
    else if (object instanceof JSONObject || object instanceof JSONArray)
      object = Message.fromJSON(object);

    if (object instanceof Request)
      handle((Request) object);
    else if (object instanceof Response)
      handle((Response) object);
    else if (object instanceof List) {
      @SuppressWarnings("unchecked")
      List<Request> requests = (List<Request>) object;
      handle(requests);
    } else {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Call a remote method.
   * 
   * @param method
   *          The method name.
   * @param params
   *          The request parameters.
   * @param timeout
   *          The request timeout in ms.
   */
  public Promise<Response> call(String method, Object params, int timeout) {
    return call(new Request(id.getAndIncrement(), method, params), timeout);
  }

  /**
   * Call a remote method without parameters.
   * 
   * @param method
   *          The method name.
   * @param timeout
   *          The request timeout in ms.
   */
  public Promise<Response> call(String method, int timeout) {
    return call(new Request(id.getAndIncrement(), method, null), timeout);
  }

  /**
   * Call a remote method with default timeout time (6000ms).
   * 
   * @param method
   *          The method name.
   * @param params
   *          The request parameters.
   * @param callback
   *          The response callback.
   */
  public Promise<Response> call(String method, Object params) {
    return call(new Request(id.getAndIncrement(), method, params), 0);
  }

  /**
   * Call a remote method without parameters and with default timeout time
   * (6000ms).
   * 
   * @param method
   *          The method name.
   */
  public Promise<Response> call(String method) {
    return call(new Request(id.getAndIncrement(), method, null), 0);
  }

  /**
   * Call a remote method without waiting for a response.
   * 
   * @param method
   *          The method name.
   * @param params
   *          The request parameters.
   */
  public void notify(String method, Object params) {
    call(new Request(method, params), 0);
  }

  /**
   * Call a remote method without waiting for a response.
   * 
   * @param method
   *          The method name.
   */
  public void notify(String method) {
    call(new Request(method), 0);
  }

  /**
   * Send a request to the remote host.
   * 
   * @param request
   *          The request object.
   * @param timeout
   *          The timeout in ms. Any value lower or equal zero will be set to
   *          default timeout of 6000ms.
   * 
   */
  public Promise<Response> call(final Request request, int timeout) {
    if (request.isNotification()) {
      onMessage(request);
      return null;
    } else {
      final int finalTimeout = timeout <= 0 ? 6000 : timeout;

      Promise<Response> promise = new Promise<Response>();
      this.requests.put(request.getId(), new RequestTask(request.getId(), finalTimeout, promise));
      onMessage(request);

      return promise;
    }
  }

  protected synchronized Promise<Response> removeRequest(Object id) {
    RequestTask task = this.requests.remove(id);

    if (task != null) {
      task.cancel();
      return task.promise;
    }

    return null;
  }

  /**
   * This is called every time the client receives an {@link Response} from the
   * local RPC server or if the client needs to send an {@link Request} to a
   * remote host.
   * 
   * @param message
   *          A {@link Request} or a {@link Response} to send to the remote
   *          host.
   */
  public synchronized void onMessage(Message message) {
    if (messageCallback != null)
      messageCallback.onMessage(message);
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.jsonrpc.ResponseCallback#onResponse(eu.
   * addicted2random.a2rclient.jsonrpc.Response)
   */
  @Override
  public synchronized void onResponse(Response response) {
    onMessage(response);
  }

  /**
   * Get registered message callback.
   * 
   * @return
   */
  public MessageCallback getMessageCallback() {
    return messageCallback;
  }

  /**
   * Set a message callback.
   * 
   * @param messageCallback
   */
  public synchronized void setMessageCallback(MessageCallback messageCallback) {
    this.messageCallback = messageCallback;
  }

}
