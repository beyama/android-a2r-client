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
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;

import android.util.Log;

import com.illposed.osc.OSCPacket;

import eu.addicted2random.a2rclient.A2R;
import eu.addicted2random.a2rclient.osc.OSCPacketListener;

public class WebSocketConnection extends AbstractConnection implements OSCPacketListener {

  private final String TAG = "WebSocketClient";

  private void v(String message) {
    Log.v(TAG, message);
  }

  @SuppressWarnings("unused")
  private void v(String message, Object... args) {
    v(String.format(message, args));
  }

  private Channel mChannel;

  private ClientBootstrap mBootstrap;

  public WebSocketConnection(URI uri) {
    super(uri);
  }

  @Override
  protected void doClose() throws InterruptedException {
    ChannelFuture future;

    if (mChannel != null) {
      future = mChannel.close();
      future.awaitUninterruptibly();
      mChannel = null;
    }
    
    if (mBootstrap != null)
      mBootstrap.shutdown();
    //  mBootstrap.releaseExternalResources();
  }

  @Override
  protected void doOpen() throws Exception {

    ExecutorService bossExecuter = Executors.newCachedThreadPool();
    ExecutorService workerExecuter = Executors.newCachedThreadPool();

    NioClientSocketChannelFactory factory = new NioClientSocketChannelFactory(bossExecuter, workerExecuter);

    mBootstrap = new ClientBootstrap(factory);

    Channel ch = null;

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
      v("WebSocket Client connecting");
      ChannelFuture future = mBootstrap.connect(new InetSocketAddress(uri.getHost(), uri.getPort()));
      future.syncUninterruptibly();

      ch = future.getChannel();

      handshaker.handshake(ch).syncUninterruptibly();
      mChannel = ch;

      ChannelFuture closeFuture = mChannel.getCloseFuture();

      // Call WebSocketConnection.close() if the channel gets closed.
      closeFuture.addListener(new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
          
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
      });
    } catch (Exception e) {
      mBootstrap.releaseExternalResources();
      throw new RuntimeException(e);
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
    OSCPacketListener listener = getOscPacketListener();
    if (listener != null)
      listener.onOSCPacket(packet);
  }

}
