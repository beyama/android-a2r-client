package eu.addicted2random.a2rclient.jam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  
  private static class RequestedLayout {
    final String jamId;
    final String layoutId;
    final Promise<Layout> promise;
    
    public RequestedLayout(String jamId, String layoutId, Promise<Layout> promise) {
      super();
      this.jamId = jamId;
      this.layoutId = layoutId;
      this.promise = promise;
    }
  }

  private final RPCClient client;

  private Promise<List<Jam>> jams;

  private Map<String, Promise<List<Layout>>> layouts = new HashMap<String, Promise<List<Layout>>>();
  
  private RequestedLayout requestedLayout;

  public JamService(RPCClient client) {
    super();
    this.client = client;
  }

  /**
   * Get a list of all available jams from A2R Hub.
   * 
   * @return
   */
  public synchronized Promise<List<Jam>> getAll() {
    if (jams != null) return jams;
    jams = getList("jams.getAll", Jam.class);
    return jams;
  }

  /**
   * Fetch a list of all available layouts for a jam session from the Hub.
   * 
   * The layouts in the list will only contain the ids and names of the layouts.
   * 
   * @return
   */
  public synchronized Promise<List<Layout>> getLayouts(final Jam jam) {
    if (jam == null)
      throw new NullPointerException();

    if (layouts.containsKey(jam.getId()))
      return layouts.get(jam.getId());

    Promise<List<Layout>> layoutsPromise = getList("jams.getLayouts", new String[] { jam.getId() }, Layout.class);
    
    layoutsPromise.addListener(new PromiseListener<List<Layout>>() {

      @Override
      public void opperationComplete(Promise<List<Layout>> promise) {
        if(promise.isSuccess()) {
          List<Layout> layouts = promise.getResult();
          jam.setLayouts(layouts);
          
          for(Layout layout : layouts)
            layout.setJam(jam);
        }
      }
      
    });
    
    layouts.put(jam.getId(), layoutsPromise);
    return layoutsPromise;
  }
  
  /**
   * Fetch a layout from server.
   * 
   * @param jamId
   * @param layoutId
   * @return
   */
  public synchronized Promise<Layout> getLayout(final Context context, final Jam jam, String layoutId) {
    String jamId = jam.getId();
    
    if(requestedLayout != null && requestedLayout.jamId.equals(jamId) && requestedLayout.layoutId.equals(layoutId))
      return requestedLayout.promise;
    
    Promise<Response> responsePromise = client.call("jams.getLayout", new String[] { jamId, layoutId });
    
    final Promise<Layout> layoutPromise = new Promise<Layout>();
    
    responsePromise.addListener(new PromiseListener<Response>() {
      
      @Override
      public void opperationComplete(Promise<Response> promise) {
        if(promise.isSuccess()) {
          Response response = promise.getResult();
          
          if(response.isError()) {
            layoutPromise.failure(response.asError().getError());
          } else {
            try {
              Layout layout = Layout.fromJSON(context, (TreeNode)response.asResult().getResult());
              jam.setCurrentSelectedLayout(layout);
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
    
    
    requestedLayout = new RequestedLayout(jamId, layoutId, layoutPromise);
    
    return layoutPromise;
  }

  /*
   * (non-Javadoc)
   * @see eu.addicted2random.a2rclient.jsonrpc.AbstractRPCService#getObjectMapper(java.lang.Class)
   */
  @Override
  protected ObjectMapper getObjectMapper(Class<?> valueType) {
    return mapper;
  }

  /*
   * (non-Javadoc)
   * @see eu.addicted2random.a2rclient.jsonrpc.AbstractRPCService#getClient()
   */
  @Override
  protected RPCClient getClient() {
    return client;
  }

}
