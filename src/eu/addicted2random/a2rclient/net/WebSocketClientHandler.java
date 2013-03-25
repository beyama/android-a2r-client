package eu.addicted2random.a2rclient.net;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.util.CharsetUtil;

import android.util.Log;

import com.illposed.osc.OSCPacket;
import com.illposed.osc.utility.OSCByteArrayToJavaConverter;

/**
 * WebSocket client handler.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public class WebSocketClientHandler extends SimpleChannelUpstreamHandler {

  private final String TAG = "WebSocketClientHandler";
  
  private void v(String message) {
    Log.v(TAG, message);
  }
  
  @SuppressWarnings("unused")
  private void v(String message, Object ... args) {
    v(String.format(message, args));
  }
  
  private final WebSocketClientHandshaker mHandshaker;
  
  private final WebSocketConnection mConnection;
  
  private final OSCByteArrayToJavaConverter mConverter = new OSCByteArrayToJavaConverter();

  public WebSocketClientHandler(WebSocketClientHandshaker handshaker, WebSocketConnection connection) {
    mHandshaker = handshaker;
    mConnection = connection;
  }

  /*
   * (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelClosed(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
   */
  @Override
  public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    v("WebSocket client disconnected");
  }

  /*
   * (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
   */
  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    Channel ch = ctx.getChannel();
    if (!mHandshaker.isHandshakeComplete()) {
      mHandshaker.finishHandshake(ch, (HttpResponse) e.getMessage());
      return;
    }

    if (e.getMessage() instanceof HttpResponse) {
      HttpResponse response = (HttpResponse) e.getMessage();
      throw new Exception("Unexpected HttpResponse (status=" + response.getStatus() + ", content="
          + response.getContent().toString(CharsetUtil.UTF_8) + ')');
    }

    WebSocketFrame frame = (WebSocketFrame) e.getMessage();
    if (frame instanceof TextWebSocketFrame) {
      // handle JSON-RPC request/response
      TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
      mConnection.getRPCClient().handle(textFrame.getText());
      v("WebSocket Client received message: " + textFrame.getText());
    } else if (frame instanceof BinaryWebSocketFrame) {
      // get OSC packet from buffer
      ChannelBuffer buffer = frame.getBinaryData();
      byte[] bytes = new byte[buffer.capacity()];
      buffer.getBytes(0, bytes);
      
      OSCPacket packet = mConverter.convert(bytes, bytes.length);
      mConnection.onOSCPacket(packet);
    } else if (frame instanceof PongWebSocketFrame) {
      v("WebSocket Client received pong");
    } else if (frame instanceof CloseWebSocketFrame) {
      v("WebSocket Client received closing");
      ch.close();
    } else if (frame instanceof PingWebSocketFrame) {
      v("WebSocket Client received ping, response with pong");
      ch.write(new PongWebSocketFrame(frame.getBinaryData()));
    }
  }

  /*
   * (non-Javadoc)
   * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
   */
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    final Throwable t = e.getCause();
    t.printStackTrace();
    e.getChannel().close();
  }
}
