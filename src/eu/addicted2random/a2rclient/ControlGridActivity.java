package eu.addicted2random.a2rclient;

import java.io.InputStream;
import java.net.URI;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

import eu.addicted2random.a2rclient.grid.Layout;
import eu.addicted2random.a2rclient.grid.Section;
import eu.addicted2random.a2rclient.grid.TabListener;
import eu.addicted2random.a2rclient.net.AbstractConnection;
import eu.addicted2random.a2rclient.net.ConnectionService;
import eu.addicted2random.a2rclient.net.ConnectionServiceBinding;
import eu.addicted2random.a2rclient.osc.Hub;
import eu.addicted2random.a2rclient.utils.Promise;

public class ControlGridActivity extends SherlockFragmentActivity implements ServiceConnection {

  /**
   * Task to load layout in background.
   */
  private class LoadLayoutTask extends AsyncTask<String, Integer, Layout> {

    private Exception error = null;

    /**
     * Open and parse layout
     */
    protected Layout doInBackground(String... resource) {

      if (mInProgress)
        mProgressDialog.setMessage("Open connection");

      try {
        if (mConnection.getLayout() != null)
          return mConnection.getLayout();

        Hub hub = mConnection.getHub();

        if (hub == null) {
          hub = new Hub();
          mConnection.setHub(hub);
        }

        Context context = getApplicationContext();
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

      if (mInProgress)
        mProgressDialog.setProgress(2);

      if (error != null) {
        error.printStackTrace();
        Toast.makeText(ControlGridActivity.this, R.string.layout_error, Toast.LENGTH_SHORT).show();
        ControlGridActivity.this.finish();
      } else {
        mConnection.setLayout(layout);
        ControlGridActivity.this.renderLayout();
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

  private boolean mInProgress = true;

  private ProgressDialog mProgressDialog = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState != null) {
      mInProgress = savedInstanceState.getBoolean("inProgress");
      mCurrentSelectedTab = savedInstanceState.getInt("currentSelectedTab");
    }

    if (mInProgress) {
      mProgressDialog = new ProgressDialog(this);
      mProgressDialog.setMax(2);
      mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      mProgressDialog.show();
    }

    openConnection();
  }

  @Override
  protected void onStop() {
    super.onStop();
    
    if(mProgressDialog != null)
      mProgressDialog.dismiss();

    // Unbind service if bound
    if (mConnectionBinding != null) {
      unbindService(this);
      mConnectionBinding = null;
    }
  }

  private void openConnection() {
    // Bind to LocalService
    Intent intent = new Intent(this, ConnectionService.class);
    bindService(intent, this, Context.BIND_AUTO_CREATE);
  }

  private synchronized void loadLayout() {
    if (mConnection.getLayout() == null) {
      new LoadLayoutTask().execute("grid-layouts/mima.json");
    } else {
      renderLayout();
    }
  }

  private synchronized void renderLayout() {
    Layout layout = getLayout();

    if (layout == null)
      return;

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

    if (mInProgress) {
      mProgressDialog.dismiss();
      mProgressDialog = null;
      mInProgress = false;
    }

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
    outState.putBoolean("inProgress", mInProgress);
  }
  
  @Override
  public void onServiceConnected(ComponentName name, IBinder service) {
    mConnectionBinding = (ConnectionServiceBinding) service;
    
    URI uri = (URI) getIntent().getSerializableExtra("uri");
    
    AbstractConnection connection = mConnectionBinding.createConnection(uri);

    connection.open().addActivityListener(this, "onConnectionFulfilled");
  }

  protected void onConnectionFulfilled(Promise<AbstractConnection> promise) {
    if (mInProgress)
      mProgressDialog.setProgress(1);

    if (promise.isSuccess()) {
      mConnection = promise.getResult();
      mConnection.getClosePromise().addActivityListener(this, "onConnectionCloseFulfilled");
      loadLayout();
    } else {
      Toast.makeText(this, promise.getCause().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
      finish();
    }
  }
  
  protected void onConnectionCloseFulfilled(Promise<AbstractConnection> promise) {
    mConnection = null;
    Toast.makeText(this, R.string.connection_closed, Toast.LENGTH_SHORT).show();
    finish();
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
    mConnectionBinding = null;
    // close activity if service disconnects
    finish();
  }

  public Layout getLayout() {
    if (mConnection == null)
      return null;
    return mConnection.getLayout();
  }

  public void setConnection(AbstractConnection connection) {
    mConnection = connection;
  }

  public AbstractConnection getConnection() {
    return mConnection;
  }

}
