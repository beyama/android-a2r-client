package eu.addicted2random.a2rclient.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.jboss.netty.channel.ChannelFuture;
import org.json.JSONException;

import com.illposed.osc.OSCPacket;

import eu.addicted2random.a2rclient.grid.IdMap;
import eu.addicted2random.a2rclient.models.layout.InvalidLayoutException;
import eu.addicted2random.a2rclient.models.layout.Layout;
import eu.addicted2random.a2rclient.services.osc.Hub;
import android.content.Context;
import android.os.Binder;

public class ConnectionServiceBinding extends Binder {

  private final Context context;
  private final AbstractConnection connection;
  private final Hub hub;
  private final IdMap idMap;
  private Layout layout;
  
  public ConnectionServiceBinding(Context context, AbstractConnection connection) {
    this.context = context;
    this.connection = connection;
    this.hub = new Hub(this.connection);
    this.idMap = new IdMap();
  }

  public AbstractConnection getConnection() {
    return connection;
  }

  public void close() throws InterruptedException {
    synchronized (connection) {
      hub.dispose();
      if(connection.isOpen())
        connection.close();
    }
  }

  public AbstractConnection open() {
    synchronized (connection) {
      if(connection.isOpen())
        return connection;
      connection.open();
      return connection;
    }
  }

  public boolean isOpen() {
    return connection.isOpen();
  }

  public URI getURI() {
    return connection.getURI();
  }

  public ChannelFuture sendOSC(OSCPacket packet) {
    return connection.sendOSC(packet);
  }

  public ChannelFuture sendOSC(String address, Object[] args) {
    return connection.sendOSC(address, args);
  }
  
  public Layout loadLayout(String resource) throws IOException, JSONException, InvalidLayoutException {
    InputStream stream = context.getAssets().open(resource);
    layout = Layout.fromJSON(stream);
    stream.close();
    layout.connect(hub);
    return layout;
  }

  public Layout getLayout() {
    return layout;
  }

  public Hub getHub() {
    return hub;
  }

  public IdMap getIdMap() {
    return idMap;
  }
  
}
