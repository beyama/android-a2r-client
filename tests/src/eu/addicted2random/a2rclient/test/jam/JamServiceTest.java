package eu.addicted2random.a2rclient.test.jam;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.util.Log;

import eu.addicted2random.a2rclient.jam.Jam;
import eu.addicted2random.a2rclient.jam.JamService;
import eu.addicted2random.a2rclient.jsonrpc.RPCEndpoint;
import eu.addicted2random.a2rclient.jsonrpc.Request;
import eu.addicted2random.a2rclient.jsonrpc.Response;
import eu.addicted2random.a2rclient.jsonrpc.Result;
import eu.addicted2random.a2rclient.test.jsonrpc.JSONRPCMockServer;
import eu.addicted2random.a2rclient.utils.Promise;
import junit.framework.TestCase;

public class JamServiceTest extends TestCase {

  private JamService jams;
  
  private JSONRPCMockServer server;
  
  protected void setUp() throws Exception {
    super.setUp();
    
    server = new JSONRPCMockServer();
    jams = new JamService(server.getClient());
    
    server.start();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    
    server.stop();
  }
  
  public void testGetAll() throws InterruptedException, ExecutionException {
    server.getServer().registerEndpoint("jams.getAll", new RPCEndpoint() {
      @Override
      public Response call(Request request) {
        List<Jam> jams = new ArrayList<Jam>(2);
        
        jams.add(new Jam("mima", "Mima", "Dubstep"));
        jams.add(new Jam("cuts", "Clicks & Cuts", null));
        
        return new Result(request.getId(), jams);
      }
    });
    
    Promise<List<Jam>> promise = jams.getAll();
    
    List<Jam> jamList = promise.get();
    
    for(Jam jam : jamList) {
      Log.v("JamServiceTest", jam.getTitle());
    }
  }

}
