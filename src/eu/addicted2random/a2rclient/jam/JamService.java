package eu.addicted2random.a2rclient.jam;

import java.util.List;

import android.content.Context;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.addicted2random.a2rclient.grid.Layout;
import eu.addicted2random.a2rclient.jsonrpc.AbstractRPCService;
import eu.addicted2random.a2rclient.jsonrpc.RPCClient;
import eu.addicted2random.a2rclient.jsonrpc.Response;
import eu.addicted2random.a2rclient.utils.Promise;
import eu.addicted2random.a2rclient.utils.PromiseListener;

/**
 * JSON-RPC 2 Jam service to retrieve informations about available Jams from A2R
 * Hub.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class JamService extends AbstractRPCService {

  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.NONE);
  }

  private final RPCClient client;

  public JamService(RPCClient client) {
    super();
    this.client = client;
  }

  /**
   * Get a list of all available jams from A2R Hub.
   * 
   * @return
   */
  public Promise<List<Jam>> getAll() {
    return getList("jams.getAll", Jam.class);
  }

  /**
   * Fetch a list of all available layouts for a jam session from the Hub.
   * 
   * The layouts in the list will only contain the ids and names of the layouts.
   * 
   * @return
   */
  public Promise<List<Layout>> getLayouts(String jamId) {
    return getList("jams.getLayouts", new String[] { jamId }, Layout.class);
  }

  /**
   * Fetch a layout from server.
   * 
   * @param jamId
   * @param layoutId
   * @return
   */
  public Promise<Layout> getLayout(final Context context, String jamId, String layoutId) {
    Promise<Response> responsePromise = client.call("jams.getLayout", new String[] { jamId, layoutId });

    final Promise<Layout> layoutPromise = new Promise<Layout>();

    responsePromise.addListener(new PromiseListener<Response>() {

      @Override
      public void opperationComplete(Promise<Response> promise) {
        if (promise.isSuccess()) {
          Response response = promise.getResult();

          if (response.isError()) {
            layoutPromise.failure(response.asError().getError());
          } else {
            try {
              Layout layout = Layout.fromJSON(context, (TreeNode) response.asResult().getResult());
              layoutPromise.success(layout);
            } catch (Throwable t) {
              layoutPromise.failure(t);
            }
          }
        } else {
          layoutPromise.failure(promise.getCause());
        }
      }
    });

    return layoutPromise;
  }

  /**
   * Join a jam session.
   * 
   * @param jamId Jam id
   * @param layoutId Layout id
   */
  public Promise<Boolean> join(String jamId, String layoutId) {
    return get("jams.join", new Object[] { jamId, layoutId }, Boolean.class);
  }

  /**
   * Leave a jam session.
   * 
   * @param jamId Jam id
   * @param layoutId Layout id
   */
  public void leave(String jamId, String layoutId) {
    client.notify("jams.leave", new Object[] { jamId, layoutId });
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.addicted2random.a2rclient.jsonrpc.AbstractRPCService#getObjectMapper
   * (java.lang.Class)
   */
  @Override
  protected ObjectMapper getObjectMapper(Class<?> valueType) {
    return mapper;
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.addicted2random.a2rclient.jsonrpc.AbstractRPCService#getClient()
   */
  @Override
  protected RPCClient getClient() {
    return client;
  }

}
