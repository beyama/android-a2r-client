package eu.addicted2random.a2rclient.jsonrpc;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * 
 * Makes a receiver object and a method to a {@link RPCEndpoint}.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class MethodInvokingRPCEndpoint implements RPCEndpoint {

  private static final Object[] NO_ARGS = new Object[0];

  private enum CallType {
    CALL_WITH_LIST, CALL_WITH_MAP, CALL_WITH_REQUEST, CALL_WITHOUT_ARGS, APPLY_LIST
  }

  private enum ResponseType {
    VOID, RESPONSE, RAW
  }

  private final CallType callType;
  private final ResponseType responseType;

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

    this.parameters = method.getParameterTypes();

    switch (this.parameters.length) {
    case 0:
      this.callType = CallType.CALL_WITHOUT_ARGS;
      break;
    case 1:
      Class<?> parameter = this.parameters[0];

      if (parameter.isAssignableFrom(List.class))
        this.callType = CallType.CALL_WITH_LIST;
      else if (parameter.isAssignableFrom(Map.class))
        this.callType = CallType.CALL_WITH_MAP;
      else if (parameter.isAssignableFrom(Request.class))
        this.callType = CallType.CALL_WITH_REQUEST;
      else
        this.callType = CallType.APPLY_LIST;
      break;
    default:
      this.callType = CallType.APPLY_LIST;
      break;
    }

    Class<?> returnType = this.method.getReturnType();

    if (returnType.equals(Void.TYPE))
      this.responseType = ResponseType.VOID;
    else if (returnType.isAssignableFrom(Response.class) || returnType.isAssignableFrom(Result.class)
        || returnType.isAssignableFrom(Error.class))
      this.responseType = ResponseType.RESPONSE;
    else
      this.responseType = ResponseType.RAW;

    if (this.responseType == ResponseType.RESPONSE && this.callType != CallType.CALL_WITH_REQUEST)
      throw new IllegalArgumentException("Method can't return a response without taking the corresponding request");
  }

  @Override
  public Response call(Request request) {
    Object response = null;

    try {
      switch (this.callType) {
      case CALL_WITH_REQUEST:
        response = this.method.invoke(this.receiver, new Object[] { request });
        break;
      case CALL_WITHOUT_ARGS:
        response = this.method.invoke(this.receiver, NO_ARGS);
        break;
      case CALL_WITH_LIST:
        if (request.isList())
          response = this.method.invoke(this.receiver, new Object[] { request.getList() });
        else
          return new Error(request.getId(), RPCError.INVALID_PARAMS_CODE, RPCError.INVALID_PARAMS_MESSAGE);
        break;
      case CALL_WITH_MAP:
        if (request.isMap())
          response = this.method.invoke(this.receiver, new Object[] { request.getMap() });
        else
          return new Error(request.getId(), RPCError.INVALID_PARAMS_CODE, RPCError.INVALID_PARAMS_MESSAGE);
        break;
      case APPLY_LIST:
        if (request.isList())
          response = this.method.invoke(this.receiver, request.getList().toArray());
        else
          return new Error(request.getId(), RPCError.INVALID_PARAMS_CODE, RPCError.INVALID_PARAMS_MESSAGE);
        break;
      default:
        break;
      }
    } catch (RPCError e) {
      return new Error(request.getId(), e);
    } catch (Throwable e) {
      RPCError rpcError = new RPCError(RPCError.SERVER_ERROR_CODE, RPCError.SERVER_ERROR_MESSAGE, e);
      return new Error(request.getId(), rpcError);
    }

    if (response == null)
      return new Result(request.getId());
    else if (response instanceof Response)
      return (Response) response;

    switch (this.responseType) {
    case VOID:
      return new Result(request.getId());
    case RAW:
      return new Result(request.getId(), response);
    default:
      return new Result(request.getId());
    }
  }

}
