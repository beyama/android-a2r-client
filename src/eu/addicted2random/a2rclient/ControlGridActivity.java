package eu.addicted2random.a2rclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.json.JSONException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

import eu.addicted2random.a2rclient.grid.TabListener;
import eu.addicted2random.a2rclient.models.layout.InvalidLayoutException;
import eu.addicted2random.a2rclient.models.layout.Layout;
import eu.addicted2random.a2rclient.models.layout.Section;
import eu.addicted2random.a2rclient.services.AbstractConnection;
import eu.addicted2random.a2rclient.services.AbstractConnection.ConnectionListener;
import eu.addicted2random.a2rclient.services.ConnectionService;
import eu.addicted2random.a2rclient.services.ConnectionServiceBinding;
import eu.addicted2random.a2rclient.services.osc.Hub;

public class ControlGridActivity extends SherlockFragmentActivity implements ServiceConnection, ConnectionListener {
  
  /**
   * Task to load layout in background.
   */
  private class LoadLayoutTask extends AsyncTask<String, Integer, Layout> {
    
    private Exception error = null;

    /**
     * Open and parse layout
     */
    protected Layout doInBackground(String... resource) {
      try {
        if(mConnection.getLayout() != null)
          return mConnection.getLayout();
        
        Hub hub = mConnection.getHub();
        
        if(hub == null) {
          hub = new Hub();
          mConnection.setHub(hub);
        }
        
        Context context = ControlGridActivity.this.getApplicationContext();
        InputStream stream = context.getAssets().open(resource[0]);
        Layout layout = Layout.fromJSON(context, stream);
        stream.close();
        
        layout.connect(hub);
        
        return layout;
      } catch (Exception e) {
        error = e;
      }
      return null;
    }

    @Override
    protected void onPostExecute(Layout layout) {
      super.onPostExecute(layout);
      
      if(error != null) {
        error.printStackTrace();
        Toast.makeText(ControlGridActivity.this, R.string.layout_error, Toast.LENGTH_SHORT).show();
      } else {
        mConnection.setLayout(layout);
        ControlGridActivity.this.renderLayout();
      }
    }
  }
  
  private class OpenConnectionTask extends AsyncTask<URI, Void, AbstractConnection> {

    private Exception error = null;
    
    @Override
    protected AbstractConnection doInBackground(URI... params) {
      try {
        AbstractConnection connection = mConnectionBinding.createConnection(params[0]);
        connection.open();
        connection.addConnectionListener(ControlGridActivity.this);
        return connection;
      } catch (Exception e) {
        error = e;
        return null;
      }
    }

    @Override
    protected void onPostExecute(AbstractConnection connection) {
      super.onPostExecute(connection);
      
      ControlGridActivity activity = ControlGridActivity.this;
      
      if(error != null) {
        Toast.makeText(activity, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        activity.finish();
      } else {
        ControlGridActivity.this.mConnection = connection;
        loadLayout();
      }
    }
    
  }
  
  private final static String TAG = "ControlGridActivity";

  @SuppressWarnings("unused")
  private static void v(String message) {
    Log.v(TAG, message);
  }

  @SuppressWarnings("unused")
  private static void v(String message, Object... args) {
    Log.v(TAG, String.format(message, args));
  }
  
  private ConnectionServiceBinding mConnectionBinding = null;
  
  private AbstractConnection mConnection = null;

  private int mCurrentSelectedTab = 0;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState != null) {
      mCurrentSelectedTab = savedInstanceState.getInt("currentSelectedTab");
    }
    
    openConnection();
  }

  @Override
  protected void onStop() {
    super.onStop();
    
    if(mConnection != null)
      mConnection.removeConnectionListener(this);
    
    // Unbind service if bound
    if(mConnectionBinding != null) {
      unbindService(this);
      mConnectionBinding = null;
    }
  }
  
  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  private void openConnection() {
    // Bind to LocalService
    v("openConnection");
    Intent intent = new Intent(this, ConnectionService.class);
    bindService(intent, this, Context.BIND_AUTO_CREATE);
  }

  private synchronized void loadLayout() {
    if(mConnection.getLayout() == null) {
      new LoadLayoutTask().execute("grid-layouts/mima.json");
    } else {
      renderLayout();
    }
  }
  
  private synchronized void renderLayout() {
    Layout layout = getLayout();
    
    if(layout == null) return;
    
    ActionBar actionBar = getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    actionBar.setDisplayShowTitleEnabled(true);
    
    if (layout.getTitle() != null)
      actionBar.setTitle(layout.getTitle());

    for (Section section : layout.getSections()) {
      Tab tab = actionBar.newTab().setText(section.getTitle() == null ? section.getName() : section.getTitle())
          .setTabListener(new TabListener(this, section.getId()));
      actionBar.addTab(tab);
    }

    actionBar.setSelectedNavigationItem(mCurrentSelectedTab);
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getSupportMenuInflater().inflate(R.menu.activity_control_grid, menu);
    return true;
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt("currentSelectedTab", getSupportActionBar().getSelectedNavigationIndex());
  }

  @Override
  public void onServiceConnected(ComponentName name, IBinder service) {
    mConnectionBinding = (ConnectionServiceBinding) service;
    URI uri = (URI)getIntent().getSerializableExtra("uri");
    new OpenConnectionTask().execute(uri);
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
    mConnectionBinding = null;
    // close activity if service disconnects
    finish();
  }
  
  public Layout getLayout() {
    if(mConnection == null) return null;
    return mConnection.getLayout();
  }

  @Override
  public void onConnectionOpened() {
  }

  @Override
  public void onConnectionClosed() {
    final Activity activity = this;
    
    this.runOnUiThread(new Runnable() {
      
      @Override
      public void run() {
        Toast.makeText(activity, R.string.connection_closed, Toast.LENGTH_SHORT).show();
        activity.finish();
      }
    });
  }

  @Override
  public void onConnectionError(final Throwable e) {
  }

}