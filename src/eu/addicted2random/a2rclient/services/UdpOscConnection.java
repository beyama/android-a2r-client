package eu.addicted2random.a2rclient.services;

import java.net.InetSocketAddress;
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

import android.content.Intent;
import android.util.Log;

public class UdpOscConnection extends AbstractConnection {
  
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
  
  private OnOSCMessageListener mMessageListener = null;
  
  public UdpOscConnection(Intent intent, OnOSCMessageListener listener) {
    super(intent);
    
    mPort = getURI().getPort();
    
    // set default port if no port is given
    if(mPort == -1)
      mPort = 5001;
    
    mMessageListener = listener;
  }

  @Override
  public void close() throws InterruptedException {
    ChannelFuture future;
    
    if(mChannel != null) {
      future = mChannel.close();
      future.awaitUninterruptibly();
    }
    
    if(mServerChannel != null) {
      future = mServerChannel.close();
      future.awaitUninterruptibly();
    }
    mChannel = null;
    mServerChannel = null;
  }

  @Override
  public void open() {
    Executor workerPool = Executors.newCachedThreadPool();
    
    ChannelFactory channelFactory = new NioDatagramChannelFactory(workerPool);
    
    ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
      public ChannelPipeline getPipeline() throws Exception {
        return Channels.pipeline(
            new OSCChannelHandler(mMessageListener)
         );
       }
    };
    
    InetSocketAddress remoteAddress = new InetSocketAddress(getURI().getHost(), mPort);
    InetSocketAddress localAddress = new InetSocketAddress(7750);
    
    ConnectionlessBootstrap bootstrap = new ConnectionlessBootstrap(channelFactory);
    
    bootstrap.setPipelineFactory(pipelineFactory);
    
    ChannelFuture cf = bootstrap.connect(remoteAddress);

    cf.addListener(new ChannelFutureListener() {

      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        if(future.isSuccess()) { 
          mChannel = future.getChannel();
        } else {
          future.getCause().printStackTrace();
        }
      }

    });
    
    mServerChannel = bootstrap.bind(localAddress);
  }

  @Override
  public boolean isOpen() {
    return mChannel != null || mServerChannel != null;
  }
  
  @Override
  public ChannelFuture write(Object object) {
    if(mChannel != null)
      return mChannel.write(object);
    return null;
  }

}
