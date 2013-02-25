package eu.addicted2random.a2rclient.jsonrpc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * JSON-RPC client. This class is the glue layer between your network connection
 * and the local {@link RPCServer}. If you like to extend this class you should
 * override {@link RPCClient#onMessage(Message)} to send the messages to your
 * remote endpoint. The received JSON data must be passed to one of the 'handle'
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

  private class RequestEntry {
    final TimerTask task;
    final ResponseCallback callback;

    public RequestEntry(TimerTask task, ResponseCallback callback) {
      super();
      this.task = task;
      this.callback = callback;
    }
  }

  private final RPCServer server;

  private volatile MessageCallback messageCallback;

  private final Map<Object, RequestEntry> requests = new HashMap<Object, RequestEntry>();

  /* to check for timeouts */
  private final Timer timer = new Timer();

  private volatile long id = 1l;

  /**
   * Construct a new instance of {@link RPCClient}.
   * 
   * @param server
   *          The local {@link RPCServer}.
   */
  public RPCClient(RPCServer server) {
    this.server = server;
  }

  /**
   * Handle a {@link Request}. This passes the {@link Request} to the
   * {@link RPCServer} and registers itself as {@link ResponseCallback}. The
   * received {@link Response} will be passed to
   * {@link RPCClient#onMessage(Message)}.
   * 
   * @param request
   *          The request object.
   * @return
   */
  public Future<Response> handle(Request request) {
    return this.server.call(request, this);
  }

  /**
   * Handle a response from the remote server.
   * 
   * @param response
   */
  public synchronized void handle(Response response) {
    RequestEntry entry = requests.remove(response.getId());

    if (entry != null) {
      if (entry.task.cancel()) {
        entry.callback.onResponse(response);
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
   * @param callback
   *          The response callback.
   */
  public void call(String method, Object params, int timeout, final ResponseCallback callback) {
    call(new Request(id++, method, params), timeout, callback);
  }

  /**
   * Call a remote method without parameters.
   * 
   * @param method
   *          The method name.
   * @param timeout
   *          The request timeout in ms.
   * @param callback
   *          The response callback.
   */
  public void call(String method, int timeout, final ResponseCallback callback) {
    call(new Request(id++, method, null), timeout, callback);
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
  public void call(String method, Object params, final ResponseCallback callback) {
    call(new Request(id++, method, params), 0, callback);
  }

  /**
   * Call a remote method without parameters and with default timeout time
   * (6000ms).
   * 
   * @param method
   *          The method name.
   * @param callback
   *          The response callback.
   */
  public void call(String method, final ResponseCallback callback) {
    call(new Request(id++, method, null), 0, callback);
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
    call(new Request(method, params), 0, null);
  }

  /**
   * Call a remote method without waiting for a response.
   * 
   * @param method
   *          The method name.
   */
  public void notify(String method) {
    call(new Request(method), 0, null);
  }

  /**
   * Send a request to the remote host.
   * 
   * @param request
   *          The request object.
   * @param timeout
   *          The timeout in ms. Any value lower or equal zero will be set to
   *          default timeout of 6000ms.
   * @param callback
   *          The response callback or null.
   * 
   */
  public synchronized void call(final Request request, int timeout, final ResponseCallback callback) {
    if (request.isNotification() || callback == null) {
      onMessage(request);
    } else {
      final int finalTimeout = timeout <= 0 ? 6000 : timeout;

      // handle timeout
      TimerTask task = new TimerTask() {

        @Override
        public void run() {
          synchronized (RPCClient.this) {
            RequestEntry entry = requests.remove(request.getId());
            if (entry != null) {
              Error timeoutError = new Error(request.getId(), 1, "timeout", finalTimeout);
              entry.callback.onResponse(timeoutError);
            }
          }
        }

      };

      this.requests.put(request.getId(), new RequestEntry(task, callback));
      this.timer.schedule(task, finalTimeout);

      onMessage(request);
    }
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
