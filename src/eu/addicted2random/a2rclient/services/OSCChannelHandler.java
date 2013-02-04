package eu.addicted2random.a2rclient.services;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import android.util.Log;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;
import com.illposed.osc.utility.OSCByteArrayToJavaConverter;

public class OSCChannelHandler extends SimpleChannelHandler {

  OnOSCMessageListener mListener;
  
  OSCByteArrayToJavaConverter mConverter = new OSCByteArrayToJavaConverter();
  
  public OSCChannelHandler(OnOSCMessageListener listener) {
    mListener = listener;
    Log.v("OSCChannelHandler", String.format("constructed (has listener? %b)", mListener != null));
  }
  
  @Override
  public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    OSCPacket packet = (OSCPacket)e.getMessage();
    ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(packet.getByteArray());
    Channels.write(ctx, e.getFuture(), buffer);
  }

  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    Log.v("OSCChannelHandler", "messageReceived");
    if(!(e.getMessage() instanceof ChannelBuffer)) {
      super.messageReceived(ctx, e);
      return;
    }
    
    ChannelBuffer buffer = (ChannelBuffer)e.getMessage();
    
    if(!buffer.readable()) return;
    
    byte[] bytes = new byte[buffer.capacity()];
    buffer.getBytes(0, bytes);
    
    OSCPacket packet = mConverter.convert(bytes, bytes.length);
    if(packet instanceof OSCBundle) {
      mListener.onOSCBundle((OSCBundle)packet);
    } else {
      mListener.onOSCMessage((OSCMessage)packet);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    e.getCause().printStackTrace();
  }

}
