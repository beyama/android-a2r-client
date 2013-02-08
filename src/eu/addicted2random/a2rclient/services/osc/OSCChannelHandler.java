package eu.addicted2random.a2rclient.services.osc;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.illposed.osc.OSCPacket;
import com.illposed.osc.utility.OSCByteArrayToJavaConverter;


public class OSCChannelHandler extends SimpleChannelHandler {

  private final OSCPacketListener mListener;
  
  OSCByteArrayToJavaConverter mConverter = new OSCByteArrayToJavaConverter();
  
  public OSCChannelHandler(OSCPacketListener listener) {
    mListener = listener;
  }
  
  @Override
  public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    OSCPacket packet = (OSCPacket)e.getMessage();
    ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(packet.getByteArray());
    Channels.write(ctx, e.getFuture(), buffer);
  }

  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    if(!(e.getMessage() instanceof ChannelBuffer)) {
      super.messageReceived(ctx, e);
      return;
    }
    
    ChannelBuffer buffer = (ChannelBuffer)e.getMessage();
    
    if(!buffer.readable()) return;
    
    byte[] bytes = new byte[buffer.capacity()];
    buffer.getBytes(0, bytes);
    
    OSCPacket packet = mConverter.convert(bytes, bytes.length);
    mListener.onOSCPacket(packet);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    e.getCause().printStackTrace();
  }

}
