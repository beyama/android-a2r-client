package eu.addicted2random.a2rclient.test.jsonrpc;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.addicted2random.a2rclient.jsonrpc.Message;
import eu.addicted2random.a2rclient.jsonrpc.MessageCallback;
import eu.addicted2random.a2rclient.jsonrpc.RPCClient;
import eu.addicted2random.a2rclient.jsonrpc.RPCServer;

public class JSONRPCMockServer {

  private BlockingQueue<String> inChannel = new LinkedBlockingQueue<String>();
  
  private BlockingQueue<String> outChannel = new LinkedBlockingQueue<String>();
  
  private ExecutorService executorService = Executors.newSingleThreadExecutor();
  
  private RPCServer server = new RPCServer(executorService);
  
  private RPCClient serverClient = new RPCClient(server);
  
  private RPCClient client = new RPCClient();
  
  private Thread serverThread;
  
  private Thread clientThread;
  
  public JSONRPCMockServer() {
    
    serverClient.setMessageCallback(new MessageCallback() {
      @Override
      public void onMessage(Message message) {
        try {
          outChannel.add(Message.toJsonString(message));
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }
    });
    
    client.setMessageCallback(new MessageCallback() {
      @Override
      public void onMessage(Message message) {
        try {
          inChannel.add(Message.toJsonString(message));
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }
    });
  
  }
  
  public synchronized void start() {
    if(serverThread != null) return;
    
    if(executorService == null)
      throw new IllegalStateException("Server is stoped");
    
    serverThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        String json = null;
        
        try {
          while((json = inChannel.take()) != null) {
            try {
              serverClient.handle(json);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        } catch (InterruptedException e) {
        }
      }
    });
    
    serverThread.start();
    
    clientThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        String json = null;
        
        try {
          while((json = outChannel.take()) != null) {
            try {
              client.handle(json);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        } catch (InterruptedException e) {
        }
      }
      
    });
    
    clientThread.start();
    
  }
  
  public synchronized void stop() {
    if(serverThread != null) {
      serverThread.interrupt();
      serverThread = null;
    
      clientThread.interrupt();
      clientThread = null;
    }
    
    if(executorService != null) {
      executorService.shutdown();
      executorService = null;
    }
  }

  public RPCServer getServer() {
    return server;
  }

  public RPCClient getClient() {
    return client;
  }
  
}
