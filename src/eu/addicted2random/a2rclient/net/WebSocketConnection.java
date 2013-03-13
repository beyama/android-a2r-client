package eu.addicted2random.a2rclient.net;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.illposed.osc.OSCPacket;

import eu.addicted2random.a2rclient.A2R;
import eu.addicted2random.a2rclient.jam.JamService;
import eu.addicted2random.a2rclient.jsonrpc.Message;
import eu.addicted2random.a2rclient.jsonrpc.MessageCallback;
import eu.addicted2random.a2rclient.jsonrpc.RPCClient;
import eu.addicted2random.a2rclient.osc.Hub;
import eu.addicted2random.a2rclient.osc.OSCPacketListener;
import eu.addicted2random.a2rclient.utils.Promise;

/**
 * Addicted²Random WebSocket connection.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public class WebSocketConnection extends AbstractConnection implements OSCPacketListener {

  @SuppressWarnings("unused")
  private final String TAG = "WebSocketClient";

  /* client channel */
  private Channel mChannel;

  private ClientBootstrap mBootstrap;
  
  /* JSON-RPC client */
  private RPCClient mRPCClient = new RPCClient();
  
  private JamService mJamService;

  public WebSocketConnection(URI uri) {
    super(uri);
    
    // message callback to write requests/response to the WebSocket channel
    mRPCClient.setMessageCallback(new MessageCallback() {
      @Override
      public void onMessage(Message message) {
        try {
          String json = Message.toJsonString(message);
          TextWebSocketFrame textFrame = new TextWebSocketFrame(json);
          WebSocketConnection.this.write(textFrame);
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }
      }
    });
    
    mJamService = new JamService(mRPCClient);
  }

  @Override
  protected void doClose(final Promise<AbstractConnection> promise) {
    ChannelFuture future;

    if (mChannel != null) {
      future = mChannel.close();
      
      future.addListener(new ChannelFutureListener() {
        
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
          if(future.isSuccess())
            promise.success(WebSocketConnection.this);
          else
            promise.failure(future.getCause());
          
          if (mBootstrap != null)
            mBootstrap.shutdown();
        }
        
      });
    }
  }

  @Override
  protected void doOpen(final Promise<AbstractConnection> promise) {

    ExecutorService bossExecuter = Executors.newCachedThreadPool();
    ExecutorService workerExecuter = Executors.newCachedThreadPool();

    NioClientSocketChannelFactory factory = new NioClientSocketChannelFactory(bossExecuter, workerExecuter);

    mBootstrap = new ClientBootstrap(factory);
    
    releaseOnClose(mBootstrap);

    URI uri = getURI();

    try {
      String protocol = uri.getScheme();
      if (!"ws".equals(protocol)) {
        throw new IllegalArgumentException("Unsupported protocol: " + protocol);
      }

      HashMap<String, String> customHeaders = new HashMap<String, String>();
      customHeaders.put("User-Agent", A2R.USER_AGENT);

      // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or
      // V00.
      // If you change it to V00, ping is not supported and remember to change
      // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
      final WebSocketClientHandshaker handshaker = new WebSocketClientHandshakerFactory().newHandshaker(uri,
          WebSocketVersion.V13, null, false, customHeaders);

      mBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
        public ChannelPipeline getPipeline() throws Exception {
          ChannelPipeline pipeline = Channels.pipeline();

          pipeline.addLast("decoder", new HttpResponseDecoder());
          pipeline.addLast("encoder", new HttpRequestEncoder());
          pipeline.addLast("ws-handler", new WebSocketClientHandler(handshaker, WebSocketConnection.this));
          return pipeline;
        }
      });

      // Connect
      ChannelFuture future = mBootstrap.connect(new InetSocketAddress(uri.getHost(), uri.getPort()));
      
      final ChannelFutureListener closeListener = new ChannelFutureListener() {
        @Override
        public void operationComplete(final ChannelFuture future) throws Exception {
          new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                WebSocketConnection.this.close();
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }).start();
        }
      };
      
      ChannelFutureListener openListener = new ChannelFutureListener() {
        @Override
        public void operationComplete(final ChannelFuture future) throws Exception {
          if(future.isSuccess()) {
            mChannel = future.getChannel();
            handshaker.handshake(mChannel).syncUninterruptibly();

            // register channel close listener
            ChannelFuture closeFuture = mChannel.getCloseFuture();
            closeFuture.addListener(closeListener);
            
            promise.success(WebSocketConnection.this);
          } else {
            promise.failure(future.getCause());
          }
        }
      };
      
      future.addListener(openListener);
    } catch (Exception e) {
      promise.failure(e);
    }
  }

  @Override
  public ChannelFuture write(Object object) {
    if (mChannel != null)
      return mChannel.write(object);
    return null;
  }

  @Override
  public ChannelFuture sendOSC(OSCPacket packet) {
    if (mChannel == null)
      return null;

    ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(packet.getByteArray());
    return mChannel.write(new BinaryWebSocketFrame(buffer));
  }

  @Override
  public void onOSCPacket(OSCPacket packet) {
    Hub hub = getHub();
    if (hub != null)
      hub.onOSCPacket(packet);
  }
  
  public RPCClient getRPCClient() {
    return mRPCClient;
  }
  
  public JamService getJamService() {
    return mJamService;
  }

}
