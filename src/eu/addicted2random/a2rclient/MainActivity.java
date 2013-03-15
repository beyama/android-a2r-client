package eu.addicted2random.a2rclient;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import eu.addicted2random.a2rclient.exceptions.ProtocolNotSupportedException;
import eu.addicted2random.a2rclient.fragments.ConnectionListFragment;
import eu.addicted2random.a2rclient.fragments.ConnectionListFragment.OnConnectionClickListener;
import eu.addicted2random.a2rclient.fragments.JamListFragment;
import eu.addicted2random.a2rclient.fragments.JamListFragment.OnJamClickListener;
import eu.addicted2random.a2rclient.grid.Layout;
import eu.addicted2random.a2rclient.jam.Jam;
import eu.addicted2random.a2rclient.models.Connection;
import eu.addicted2random.a2rclient.net.AbstractConnection;
import eu.addicted2random.a2rclient.net.ConnectionService;
import eu.addicted2random.a2rclient.net.ConnectionServiceBinding;
import eu.addicted2random.a2rclient.net.WebSocketConnection;
import eu.addicted2random.a2rclient.utils.Promise;

public class MainActivity extends SherlockFragmentActivity implements ServiceConnection, OnConnectionClickListener,
    OnJamClickListener {
  
  static public class MainActivityStateFragment extends SherlockFragment {

    private Connection mSelectedConnection;
    
    private AbstractConnection mCurrentClientConnection;
    
    private List<Jam> mJams;
    
    private MainActivity mActivity;
    
    public MainActivityStateFragment() {
      super();
    }
    
    @Override
    public void onAttach(Activity activity) {
      super.onAttach(activity);
      
      mActivity = (MainActivity)activity;
      
      // mConnection.getOpenPromise().addActivityListener(this, "onConnectionFulfilled");

      Log.v("MainActivityStateFragment", "attach");
    }

    @Override
    public void onDetach() {
      super.onDetach();
      
      Log.v("MainActivityStateFragment", "detach");
      unregisterConnectionPromiseListener();
      mActivity = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setRetainInstance(true);
    }

    public Connection getSelectedConnection() {
      return mSelectedConnection;
    }

    public void setSelectedConnection(Connection selectedConnection) {
      mSelectedConnection = selectedConnection;
    }

    public AbstractConnection getCurrentClientConnection() {
      return mCurrentClientConnection;
    }

    public void setCurrentClientConnection(AbstractConnection currentClientConnection) {
      mCurrentClientConnection = currentClientConnection;
    }

    public List<Jam> getJams() {
      return mJams;
    }

    public void setJams(List<Jam> jams) {
      mJams = jams;
    }
    
    public void reset() {
      unregisterConnectionPromiseListener();
      
      mCurrentClientConnection = null;
      mJams = null;
    }
    
    private void unregisterConnectionPromiseListener() {
      if(mCurrentClientConnection != null && mActivity != null) {
        mCurrentClientConnection.getOpenPromise().removeActivityListener(mActivity);
        mCurrentClientConnection.getClosePromise().removeActivityListener(mActivity);
      }
    }
    
  }
  
  final static String TAG = "MainActivity";

  final static String CONNECTION_LIST_TAG = "connectionList";
  
  final static String STATE_TAG = "stateFragment";

  final static String JAM_LIST_TAG = "jamList";

  private boolean mDualPane = false;

  private MainActivityStateFragment mState;
  
  private ConnectionServiceBinding mConnectionBinding = null;

  private ProgressDialog mProgressDialog = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (findViewById(R.id.rightPane) != null)
      mDualPane = true;

    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft;
    
    mState = (MainActivityStateFragment) fm.findFragmentByTag(STATE_TAG);
    
    if(mState == null) {
      mState = new MainActivityStateFragment();
      ft = fm.beginTransaction();
      ft.add(mState, STATE_TAG);
      ft.commit();
    }

    ConnectionListFragment connectionListFragment = (ConnectionListFragment) fm.findFragmentByTag(CONNECTION_LIST_TAG);
    
    if (connectionListFragment == null) {
      connectionListFragment = new ConnectionListFragment();
      
      if(mState.getSelectedConnection() != null)
        connectionListFragment.setSelectedConnection(mState.getSelectedConnection());
    }
    
    if(mDualPane) {
      ft = fm.beginTransaction();
      ft.replace(R.id.leftPane, connectionListFragment, CONNECTION_LIST_TAG);
      
      if(mState.getJams() != null) {
        JamListFragment jamListFragment = new JamListFragment();
        jamListFragment.setJams(mState.getJams());
        ft.replace(R.id.rightPane, jamListFragment);
      }
      ft.commit();
    } else {
      ft = fm.beginTransaction();
      
      ft.replace(R.id.leftPane, connectionListFragment, CONNECTION_LIST_TAG);
      ft.addToBackStack(null);
      
      if(mState.getJams() != null) {
        JamListFragment jamListFragment = new JamListFragment();
        jamListFragment.setJams(mState.getJams());
        ft.replace(R.id.leftPane, jamListFragment);
      }
      ft.commit(); 
    }

    Intent serviceIntent = new Intent(this, ConnectionService.class);
    startService(serviceIntent);

    bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
  }

  @Override
  protected void onStop() {
    super.onStop();

    if (mConnectionBinding != null) {
      unbindService(this);
      mConnectionBinding = null;
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getSupportMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Intent intent;

    switch (item.getItemId()) {
    case R.id.menu_add_connection:
      intent = new Intent(this, ConnectionEditActivity.class);
      startActivityForResult(intent, 0);
      break;
    case R.id.menu_settings:
      intent = new Intent(this, SettingsActivity.class);
      startActivity(intent);
      break;
    default:
      return super.onOptionsItemSelected(item);
    }
    return true;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);

    if (resultCode == Activity.RESULT_OK) {
      ConnectionListFragment fragment = (ConnectionListFragment) getSupportFragmentManager().findFragmentByTag(
          CONNECTION_LIST_TAG);
      fragment.reload();
    }
  }

  @Override
  public void onConnectionClick(int position, Connection connection) {
    Connection currentConnection = mState.getSelectedConnection();
    
    if(currentConnection != null && !currentConnection.equals(connection)) {
      try {
        if(mState.getCurrentClientConnection() != null) {
          mState.getCurrentClientConnection().close();
        }
      } catch (Exception e) {
      }
    }
    
    mState.setSelectedConnection(connection);

    AbstractConnection c;
    
    try {
      c = mConnectionBinding.createConnection(connection.getUri());
      mState.setCurrentClientConnection(c);
    } catch (ProtocolNotSupportedException e) {
      Toast.makeText(this, e.getLocalizedMessage(this), Toast.LENGTH_LONG).show();
      return;
    }

    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setTitle(R.string.dialog_title);
    mProgressDialog.setMessage(getResources().getString(R.string.dialog_opening_connection,
        connection.getUri().getHost()));
    mProgressDialog.show();

    c.open().addActivityListener(this, "onConnectionFulfilled");
  }

  @Override
  public boolean onConnectionLongClick(int position, Connection connection) {
    Intent intent = new Intent(this, ConnectionEditActivity.class);
    intent.putExtra("connectionId", connection.getId());
    startActivityForResult(intent, 0);
    return true;
  }

  @Override
  public void onJamClick(Jam jam) {
    if (mProgressDialog != null)
      mProgressDialog.dismiss();

    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setTitle(R.string.dialog_title);
    mProgressDialog.setMessage(getResources().getString(R.string.dialog_loading_layouts_message));
    mProgressDialog.show();

    WebSocketConnection ws = (WebSocketConnection) mState.getCurrentClientConnection();
    ws.getJamService().getLayouts(jam).addActivityListener(this, "onLayoutsLoaded");
  }

  /**
   * Show a dialog to select a layout.
   * 
   * @param promise
   */
  protected void onLayoutsLoaded(Promise<List<Layout>> promise) {
    if (mProgressDialog != null) {
      mProgressDialog.dismiss();
      mProgressDialog = null;
    }

    if (promise.isSuccess()) {
      List<Layout> layouts = promise.getResult();

      final ArrayAdapter<Layout> layoutAdapter = new ArrayAdapter<Layout>(this, R.layout.layout_list_item);

      for (Layout layout : layouts)
        layoutAdapter.add(layout);

      AlertDialog.Builder builder = new AlertDialog.Builder(this);

      builder.setTitle(R.string.dialog_choose_layout);

      builder.setAdapter(layoutAdapter, new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
          Layout layout = layoutAdapter.getItem(which);
          loadLayout(layout);
        }
      });

      builder.create().show();

    } else {
      Toast.makeText(this, promise.getCause().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Load selected layout
   * 
   * @param layout
   */
  private void loadLayout(Layout layout) {
    if (mProgressDialog != null)
      mProgressDialog.dismiss();

    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setTitle(R.string.dialog_title);
    mProgressDialog.setMessage(getResources().getString(R.string.dialog_loading_selected_layout_message,
        layout.getTitle()));
    mProgressDialog.show();

    WebSocketConnection ws = (WebSocketConnection) mState.getCurrentClientConnection();
    ws.getJamService().getLayout(getApplicationContext(), layout.getJam(), layout.getId())
        .addActivityListener(this, "onLayoutLoaded");
  }

  protected void onLayoutLoaded(Promise<Layout> promise) {
    if (mProgressDialog != null) {
      mProgressDialog.dismiss();
      mProgressDialog = null;
    }

    if (promise.isSuccess()) {
      AbstractConnection c = mState.getCurrentClientConnection();
      c.setCurrentSelectedLayout(promise.getResult());
      
      Intent intent = new Intent(this, ControlGridActivity.class);
      intent.putExtra("uri", c.getURI());
      
      startActivity(intent);
    } else {
      Toast.makeText(this, promise.getCause().getLocalizedMessage(), Toast.LENGTH_LONG).show();
    }
  }

  protected void onConnectionFulfilled(Promise<AbstractConnection> promise) {
    Log.v(TAG, "onConnectionFulfilled");
    if (promise.isSuccess()) {
      AbstractConnection c = promise.getResult();
      
      mState.setCurrentClientConnection(c);
      c.getClosePromise().addActivityListener(this, "onConnectionCloseFulfilled");

      loadJams();
    } else {
      if (mProgressDialog != null) {
        mProgressDialog.dismiss();
        mProgressDialog = null;
      }
      Toast.makeText(this, promise.getCause().getLocalizedMessage(), Toast.LENGTH_LONG).show();
    }
  }

  private void loadJams() {
    Log.v(TAG, "loadJams");
    if (mProgressDialog != null) {
      mProgressDialog.setMessage(getResources().getString(R.string.dialog_loading_jam_sessions_message));
    }

    WebSocketConnection ws = (WebSocketConnection) mState.getCurrentClientConnection();
    ws.getJamService().getAll().addActivityListener(this, "onJamsLoaded");
  }

  protected void onJamsLoaded(Promise<List<Jam>> promise) {
    if (mProgressDialog != null) {
      mProgressDialog.dismiss();
      mProgressDialog = null;
    }

    Log.v(TAG, "onJamsLoaded");
    // TODO: handle errors
    showJams(promise.getResult());
  }

  private void showJams(List<Jam> jams) {
    Log.v(TAG, "showJams");
    mState.setJams(jams);
    
    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft;

    JamListFragment jamListFragment;

    if (mDualPane) {
      jamListFragment = (JamListFragment) fm.findFragmentByTag(JAM_LIST_TAG);

      if (jamListFragment == null) {
        jamListFragment = new JamListFragment();
        jamListFragment.setJams(jams);
        ft = fm.beginTransaction();
        ft.replace(R.id.rightPane, jamListFragment, JAM_LIST_TAG);
        ft.commit();
      } else {
        jamListFragment.setJams(jams);
      }
    } else {
      jamListFragment = new JamListFragment();
      jamListFragment.setJams(jams);
      ft = fm.beginTransaction();
      ft.replace(R.id.leftPane, jamListFragment, JAM_LIST_TAG);
      ft.addToBackStack(null);
      ft.commit();
    }
  }

  protected void onConnectionCloseFulfilled(Promise<AbstractConnection> promise) {
    mState.reset();
  }

  @Override
  public void onServiceConnected(ComponentName name, IBinder service) {
    mConnectionBinding = (ConnectionServiceBinding) service;
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
  }

}
