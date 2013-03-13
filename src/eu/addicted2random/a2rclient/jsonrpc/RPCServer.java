package eu.addicted2random.a2rclient.jsonrpc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class RPCServer {

  private ExecutorService executorService;

  private final Map<String, RPCEndpoint> methods = new HashMap<String, RPCEndpoint>();

  private final Map<Object, Map<String, RPCEndpoint>> exposedServices = new HashMap<Object, Map<String, RPCEndpoint>>();

  public RPCServer(ExecutorService executorService) {
    this.executorService = executorService;
  }

  /**
   * Expose a service object.
   * 
   * Each method annotated with {@link JSONRPCEndpoint} will be wrapped in a
   * {@link MethodInvokingRPCEndpoint} and registered as service endpoint on
   * this server.
   * 
   * @param module
   *          A name prefix. Each endpoint will be prefixed with this name and a
   *          dot.
   * @param service
   *          The service object to expose.
   */
  public synchronized void expose(String module, Object service) {
    if (module == null || service == null)
      throw new NullPointerException();

    if (module.length() == 0)
      throw new IllegalArgumentException("module can't be empty");
    
    if (exposedServices.containsKey(service))
      throw new IllegalArgumentException("service is already exposed");

    Method[] methods = service.getClass().getMethods();

    Map<String, RPCEndpoint> toExpose = new HashMap<String, RPCEndpoint>();

    for (int i = 0; i < methods.length; i++) {
      Method method = methods[i];

      JSONRPCEndpoint annotation = method.getAnnotation(JSONRPCEndpoint.class);

      if (annotation != null) {
        String name = annotation.value();
        if (name.length() == 0)
          name = method.getName();

        toExpose.put(module + "." + name, new MethodInvokingRPCEndpoint(service, method));
      }
    }

    if (toExpose.size() != 0) {
      try {
        // register endpoints
        for (Map.Entry<String, RPCEndpoint> entry : toExpose.entrySet())
          registerEndpoint(entry.getKey(), entry.getValue());
      } catch (Exception e) {
        // cleanup
        for (Map.Entry<String, RPCEndpoint> entry : toExpose.entrySet()) {
          RPCEndpoint endpoint = this.methods.get(entry.getKey());

          if (endpoint != null && endpoint == entry.getValue())
            this.methods.remove(entry.getKey());
        }
        throw new RuntimeException(e);
      }
      this.exposedServices.put(service, toExpose);
    }
  }

  /**
   * Expose a service object.
   * 
   * The module name will be the value of the {@link JSONRPCService} annotation
   * or if not present, the simple name of the service class.
   * 
   * @see RPCServer#expose(String, Object)
   * 
   * @param service
   */
  public synchronized void expose(Object service) {
    JSONRPCService annotation = service.getClass().getAnnotation(JSONRPCService.class);

    String name = null;

    if (annotation != null)
      name = annotation.value();
    if (name == null || name.length() == 0)
      name = service.getClass().getSimpleName();
    expose(name, service);
  }

  public synchronized boolean unexpose(Object service) {
    Map<String, RPCEndpoint> exposed = this.exposedServices.remove(service);

    if (exposed != null) {
      for (String name : exposed.keySet())
        unregisterEndpoint(name);
      return true;
    }
    return false;
  }

  /**
   * Register an endpoint.
   * 
   * @param name
   * @param endpoint
   */
  public void registerEndpoint(String name, RPCEndpoint endpoint) {
    synchronized (this) {
      if (this.methods.containsKey(name))
        throw new IllegalArgumentException("Endpoint with name `" + name + "` already exists");
      this.methods.put(name, endpoint);
    }
  }

  /**
   * Unregister an endpoint.
   * 
   * @param name
   * @return
   */
  public boolean unregisterEndpoint(String name) {
    synchronized (this) {
      return this.methods.remove(name) != null;
    }
  }

  /**
   * Test if method exist.
   * 
   * @param name
   *          Name of method.
   * @return
   */
  public boolean hasMethod(String name) {
    return methods.containsKey(name);
  }

  /**
   * Call a {@link RPCEndpoint}. Don't access this method directly, only with a
   * {@link RPCClient}.
   * 
   * @param request
   * @param callback
   * @return
   * @throws RPCError
   */
  public Future<Response> call(final Request request, final ResponseCallback callback) throws RPCError {
    synchronized (this) {
      final RPCEndpoint endpoint = methods.get(request.getMethod());
      
      if (endpoint == null) {
        if(request.isNotification())
          return null;
        
        RPCError error = new RPCError(RPCError.METHOD_NOT_FOUND_CODE, RPCError.METHOD_NOT_FOUND_MESSAGE,
            request.getMethod());
        if (callback != null)
          callback.onResponse(new Error(request.getId(), error));
        throw error;
      }
      
      

      return executorService.submit(new Callable<Response>() {

        @Override
        public Response call() throws Exception {
          Response response;
          try {
            response = endpoint.call(request);
            
            if(request.isNotification())
              return null;
            
            if (callback != null)
              callback.onResponse(response);
            return response;
          } catch (RPCError e) {
            if(request.isNotification())
              return null;
            
            response = new Error(request.getId(), e);
            if (callback != null)
              callback.onResponse(response);
            return response;
          } catch (Throwable e) {
            if(request.isNotification())
              return null;
            
            response = new Error(request.getId(), new RPCError(RPCError.SERVER_ERROR_CODE,
                RPCError.SERVER_ERROR_MESSAGE, e));
            if (callback != null)
              callback.onResponse(response);
            
            return response;
          }
        }

      });

    }
  }

}
