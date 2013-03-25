package eu.addicted2random.a2rclient.net;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;

import android.util.Log;

import com.illposed.osc.OSCPacket;

import eu.addicted2random.a2rclient.osc.Hub;
import eu.addicted2random.a2rclient.osc.OSCPacketListener;
import eu.addicted2random.a2rclient.utils.Promise;

public class UdpOscConnection extends Connection implements OSCPacketListener {
  
  final static String TAG = "UdpOscConnection";
  
  @SuppressWarnings("unused")
  private static void v(String message) {
    Log.v(TAG, message);
  }
  
  @SuppressWarnings("unused")
  private static void v(String message, Object ...args) {
    Log.v(TAG, String.format(message, args));
  }

  private int mPort;
  
  private Channel mChannel = null;
  
  private Channel mServerChannel = null;
  
  private ConnectionlessBootstrap mBootstrap = null;
  
  public UdpOscConnection(URI uri) {
    super(uri);
    
    mPort = getURI().getPort();
    
    // set default port if no port is given
    if(mPort == -1)
      mPort = 5001;
  }

  @Override
  protected void doClose(final Promise<Connection> promise) {
    ChannelFuture future;
    // TODO: make close async
    try {
      if(mChannel != null) {
        future = mChannel.close();
        future.awaitUninterruptibly();
        mChannel = null;
      }
      
      if(mServerChannel != null) {
        future = mServerChannel.close();
        future.awaitUninterruptibly();
        mServerChannel = null;
      }
      promise.success(this);
    } catch (Throwable t) {
      promise.failure(t);
    } finally {
      if(mBootstrap != null)
        mBootstrap.shutdown();
    }
    
  }

  @Override
  protected void doOpen(final Promise<Connection> promise) {
    Executor workerPool = Executors.newCachedThreadPool();
    
    ChannelFactory channelFactory = new NioDatagramChannelFactory(workerPool);
    
    ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
      public ChannelPipeline getPipeline() throws Exception {
        return Channels.pipeline(
            new OSCChannelHandler(UdpOscConnection.this)
         );
       }
    };
    
    final InetSocketAddress remoteAddress = new InetSocketAddress(getURI().getHost(), mPort);
    final InetSocketAddress localAddress = new InetSocketAddress(7750);
    
    mBootstrap = new ConnectionlessBootstrap(channelFactory);
    
    mBootstrap.setPipelineFactory(pipelineFactory);
    
    releaseOnClose(mBootstrap);
    
    // connect to remote
    ChannelFuture cf = mBootstrap.connect(remoteAddress);
    
    cf.addListener(new ChannelFutureListener() {
      
      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        if(future.setSuccess()) {
          mChannel = future.getChannel();
       
          // bind on local address
          mServerChannel = mBootstrap.bind(localAddress);
          promise.success(UdpOscConnection.this);
        } else {
          promise.failure(future.getCause());
        }
        
      }
    });
  }
  
  @Override
  public ChannelFuture write(Object object) {
    if(mChannel != null)
      return mChannel.write(object);
    return null;
  }

  @Override
  public void onOSCPacket(OSCPacket packet) {
    Hub hub = getHub();
    if(hub != null)
      hub.onOSCPacket(packet);
  }

}
