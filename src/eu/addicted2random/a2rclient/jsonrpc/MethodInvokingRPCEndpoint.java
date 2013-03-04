package eu.addicted2random.a2rclient.jsonrpc;

import java.lang.reflect.Method;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * 
 * Makes a receiver object and a method to a {@link RPCEndpoint}.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class MethodInvokingRPCEndpoint implements RPCEndpoint {

  private final Class<?>[] parameters;

  private final Object receiver;
  
  private final Method method;

  public MethodInvokingRPCEndpoint(Object receiver, Method method) {
    super();

    if (receiver == null)
      throw new NullPointerException();
    if (method == null)
      throw new NullPointerException();

    this.receiver = receiver;
    this.method = method;

    parameters = method.getParameterTypes();
  }

  @Override
  public Response call(Request request) {
    Object response = null;
    Object params = request.getParams();
    
    try {
      if(parameters.length == 0) {
        response = method.invoke(receiver, (Object[])null);
      } else {
        if(parameters.length == 1 && parameters[0].isAssignableFrom(Request.class)) {
          response = method.invoke(receiver, request);
        } else if(params == null) {
          response = method.invoke(receiver, (Object)null);
        } else if(parameters.length == 1 && parameters[0].isAssignableFrom(params.getClass())) {
          response = method.invoke(receiver, params);
        } else if(params instanceof ArrayNode) {
          ArrayNode array = (ArrayNode)params;
          
          Object[] args = new Object[parameters.length];
          
          for(int i = 0; i < parameters.length; i++) {
            args[i] = Message.getMapper().treeToValue(array.get(i), parameters[i]);
          }
          
          response = method.invoke(receiver, args);
        } else {
          Object arg0 = Message.getMapper().treeToValue((TreeNode) params, parameters[0]);
          response = method.invoke(receiver, arg0);
        }
      }
    } catch (RPCError e) {
      if(request.isNotification())
        return null;
      
      return new Error(request.getId(), e);
    } catch (Throwable e) {
      if(request.isNotification())
        return null;
      
      RPCError rpcError = new RPCError(RPCError.SERVER_ERROR_CODE, RPCError.SERVER_ERROR_MESSAGE, e);
      return new Error(request.getId(), rpcError);
    }
    
    if(request.isNotification())
      return null;

    if (response == null)
      return new Result(request.getId());
    if (response instanceof Response)
      return (Response) response;

    return new Result(request.getId(), response);
  }

}
