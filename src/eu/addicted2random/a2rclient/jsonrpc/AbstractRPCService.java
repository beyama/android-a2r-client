package eu.addicted2random.a2rclient.jsonrpc;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import eu.addicted2random.a2rclient.utils.Promise;
import eu.addicted2random.a2rclient.utils.PromiseListener;

/**
 * Abstract base class for RPC services.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public abstract class AbstractRPCService {

  /**
   * Get an instance of {@link ObjectMapper} for a value type.
   * 
   * @param valueType
   * @return
   */
  abstract protected ObjectMapper getObjectMapper(Class<?> valueType);

  /**
   * Get an instance of {@link RPCClient} to perform RPCs.
   * 
   * @return
   */
  abstract protected RPCClient getClient();

  /**
   * Map nodes from an {@link ArrayNode} to instances of valueType and return a
   * {@link List} of mapped values.
   * 
   * @param array
   *          The array node.
   * @param valueType
   *          The target class.
   * @return
   * @throws JsonProcessingException
   */
  private <T> List<T> jsonArrayToList(ArrayNode array, Class<T> valueType) throws JsonProcessingException {
    List<T> list = new ArrayList<T>(array.size());

    ObjectMapper mapper = getObjectMapper(valueType);

    for (JsonNode node : array) {
      list.add(mapper.treeToValue(node, valueType));
    }

    return list;
  }

  /**
   * Fulfill a result promise from a RPC response promise.
   * 
   * @param promise The result promise.
   * @param responsePromise The fulfilled response promise from a RPC.
   * @param valueType
   */
  protected <T> void fulfillListResult(Promise<List<T>> promise, Promise<Response> responsePromise, Class<T> valueType) {
    if (responsePromise.isSuccess()) {
      Response response = responsePromise.getResult();

      if (response.isError()) {
        promise.failure(response.asError().getError());
      } else {
        try {
          ArrayNode array = (ArrayNode) response.asResult().getResult();
          List<T> list = jsonArrayToList(array, valueType);
          promise.success(list);
        } catch (Throwable t) {
          promise.failure(t);
        }
      }
    } else {
      promise.failure(responsePromise.getCause());
    }
  }

  /**
   * Fulfill a result promise from a RPC response promise.
   * 
   * @param promise The result promise.
   * @param responsePromise The fulfilled response promise from a RPC.
   * @param valueType
   */
  protected <T> void fulfillResult(Promise<T> promise, Promise<Response> responsePromise, Class<T> valueType) {
    if (responsePromise.isSuccess()) {
      Response response = responsePromise.getResult();

      if (response.isError()) {
        promise.failure(response.asError().getError());
      } else {
        try {
          JsonNode node = (JsonNode) response.asResult().getResult();
          T value = getObjectMapper(valueType).treeToValue(node, valueType);
          promise.success(value);
        } catch (Throwable t) {
          promise.failure(t);
        }
      }
    } else {
      promise.failure(responsePromise.getCause());
    }
  }

  /**
   * Get a result list from the RPC server.
   * 
   * @param method The RPC method.
   * @param params The RPC arguments.
   * @param timeout The request timeout in ms.
   * @param valueType The expected item result type.
   * @return
   */
  protected <T> Promise<List<T>> getList(String method, Object params, int timeout, final Class<T> valueType) {
    Promise<Response> responsePromise = getClient().call(method, params, timeout);

    final Promise<List<T>> resultPromise = new Promise<List<T>>();

    responsePromise.addListener(new PromiseListener<Response>() {

      @Override
      public void opperationComplete(Promise<Response> promise) {
        fulfillListResult(resultPromise, promise, valueType);
      }

    });

    return resultPromise;
  }
  
  /**
   * Get a result list from the RPC server.
   * 
   * @param method The RPC method.
   * @param params The RPC arguments.
   * @param valueType The expected item result type.
   * @return
   */
  protected <T> Promise<List<T>> getList(String method, Object params, final Class<T> valueType) {
    return getList(method, params, 0, valueType);
  }

  /**
   * Get a result list from the RPC server.
   * 
   * @param method The RPC method.
   * @param timeout The request timeout in ms.
   * @param valueType The expected item result type.
   * @return
   */
  protected <T> Promise<List<T>> getList(String method, int timeout, final Class<T> valueType) {
    return getList(method, null, timeout, valueType);
  }
  
  /**
   * Get a result list from the RPC server.
   * 
   * @param method The RPC method.
   * @param valueType The expected item result type.
   * @return
   */
  protected <T> Promise<List<T>> getList(String method, final Class<T> valueType) {
    return getList(method, null, 0, valueType);
  }

  /**
   * Get a result from the RPC server.
   * 
   * @param method The RPC method.
   * @param params The RPC arguments.
   * @param timeout The request timeout in ms.
   * @param valueType The expected result type.
   * @return
   */
  protected <T> Promise<T> get(String method, Object params, int timeout, final Class<T> valueType) {
    Promise<Response> responsePromise = getClient().call(method, params, timeout);

    final Promise<T> resultPromise = new Promise<T>();

    responsePromise.addListener(new PromiseListener<Response>() {

      @Override
      public void opperationComplete(Promise<Response> promise) {
        fulfillResult(resultPromise, promise, valueType);
      }

    });

    return resultPromise;
  }
  
  /**
   * Get a result from the RPC server.
   * 
   * @param method The RPC method.
   * @param params The RPC arguments.
   * @param valueType The expected result type.
   * @return
   */
  protected <T> Promise<T> get(String method, Object params, final Class<T> valueType) {
    return get(method, params, 0, valueType);
  }
  
  /**
   * Get a result from the RPC server.
   * 
   * @param method The RPC method.
   * @param timeout The request timeout in ms.
   * @param valueType The expected result type.
   * @return
   */
  protected <T> Promise<T> get(String method, int timeout, final Class<T> valueType) {
    return get(method, null, timeout, valueType);
  }

  /**
   * Get a result from the RPC server.
   * 
   * @param method The RPC method.
   * @param valueType The expected result type.
   * @return
   */
  protected <T> Promise<T> get(String method, final Class<T> valueType) {
    return get(method, null, 0, valueType);
  }

}
