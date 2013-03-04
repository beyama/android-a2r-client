package eu.addicted2random.a2rclient.net;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import eu.addicted2random.a2rclient.grid.Layout;
import eu.addicted2random.a2rclient.utils.Promise;

public class LayoutService {

  private final AbstractConnection connection;

  private Promise<Layout> layoutPromise;

  public LayoutService(AbstractConnection connection) {
    super();
    if (connection == null)
      throw new NullPointerException();

    this.connection = connection;
  }

  public synchronized Promise<Layout> loadLayout(final Context context, final String resource) {
    if (layoutPromise != null)
      return layoutPromise;

    layoutPromise = new Promise<Layout>();

    new Thread(new Runnable() {

      @Override
      public void run() {
        InputStream stream = null;
        try {
          stream = context.getAssets().open(resource);
          Layout layout = Layout.fromJSON(context, stream);
          layout.connect(connection.getHub());
          layoutPromise.success(layout);
        } catch (Exception e) {
          layoutPromise.failure(e);
        } finally {
          if (stream != null) {
            try {
              stream.close();
            } catch (IOException e) {
            }
          }
        }
      }

    }).start();

    return layoutPromise;
  }

  public synchronized void close() {
    if (layoutPromise == null)
      return;

    if (layoutPromise.isDone()) {
      if (layoutPromise.isSuccess())
        layoutPromise.getResult().dispose();
    } else {
      try {
        layoutPromise.get().dispose();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

}
