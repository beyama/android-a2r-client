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
import eu.addicted2random.a2rclient.fragments.BookmarkListFragment;
import eu.addicted2random.a2rclient.fragments.BookmarkListFragment.OnBookmarkClickListener;
import eu.addicted2random.a2rclient.fragments.JamListFragment;
import eu.addicted2random.a2rclient.fragments.JamListFragment.OnJamClickListener;
import eu.addicted2random.a2rclient.grid.Layout;
import eu.addicted2random.a2rclient.jam.Jam;
import eu.addicted2random.a2rclient.models.Bookmark;
import eu.addicted2random.a2rclient.net.AbstractConnection;
import eu.addicted2random.a2rclient.net.ConnectionService;
import eu.addicted2random.a2rclient.net.ConnectionServiceBinding;
import eu.addicted2random.a2rclient.net.WebSocketConnection;
import eu.addicted2random.a2rclient.utils.Promise;

public class MainActivity extends SherlockFragmentActivity implements ServiceConnection, OnBookmarkClickListener,
    OnJamClickListener {

  static public class MainActivityStateFragment extends SherlockFragment {

    private Bookmark mSelectedConnection;

    private AbstractConnection mCurrentClientConnection;

    private List<Jam> mJams;

    private MainActivity mActivity;

    public MainActivityStateFragment() {
      super();
    }

    @Override
    public void onAttach(Activity activity) {
      super.onAttach(activity);

      mActivity = (MainActivity) activity;

      // mConnection.getOpenPromise().addActivityListener(this,
      // "onConnectionFulfilled");
    }

    @Override
    public void onDetach() {
      super.onDetach();
      unregisterConnectionPromiseListener();
      mActivity = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setRetainInstance(true);
    }

    public Bookmark getSelectedConnection() {
      return mSelectedConnection;
    }

    public void setSelectedConnection(Bookmark selectedConnection) {
      mSelectedConnection = selectedConnection;
    }

    public AbstractConnection getCurrentClientConnection() {
      return mCurrentClientConnection;
    }

    public void setCurrentClientConnection(AbstractConnection currentClientConnection) {
      // unregister activity from previous connection and close previous
      // connection
      if (mCurrentClientConnection != null && mCurrentClientConnection != currentClientConnection) {
        unregisterConnectionPromiseListener();
        try {
          mCurrentClientConnection.close();
        } catch (Exception e) {
        }
      }
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
      mSelectedConnection = null;
      mJams = null;
    }

    private void unregisterConnectionPromiseListener() {
      if (mCurrentClientConnection != null && mActivity != null) {
        mCurrentClientConnection.getOpenPromise().removeActivityListener(mActivity);
        mCurrentClientConnection.getClosePromise().removeActivityListener(mActivity);
      }
    }

  }

  final static String TAG = "MainActivity";

  final static String CONNECTION_LIST_TAG = "connectionList";

  final static String STATE_TAG = "stateFragment";

  private boolean mDualPane = false;

  private MainActivityStateFragment mState;

  private BookmarkListFragment mConnectionListFragment;

  private JamListFragment mJamListFragment;

  private ConnectionServiceBinding mConnectionBinding = null;

  private ProgressDialog mProgressDialog = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    setContentView(R.layout.activity_main);

    // dual pane?
    mDualPane = getResources().getBoolean(R.bool.dualPane);

    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft;

    // get fragments from fragment manager
    mConnectionListFragment = (BookmarkListFragment) fm.findFragmentById(R.id.bookmarkListFragment);
    mJamListFragment = (JamListFragment) fm.findFragmentById(R.id.jamListFragment);
    mState = (MainActivityStateFragment) fm.findFragmentByTag(STATE_TAG);

    // create and add state fragment if not exist
    if (mState == null) {
      mState = new MainActivityStateFragment();
      ft = fm.beginTransaction();
      ft.add(mState, STATE_TAG);
      ft.commit();
    }

    mConnectionListFragment.setSelectedConnection(mState.getSelectedConnection());

    // handle fragment visibility
    if (mState.getJams() != null) {
      mJamListFragment.setJams(mState.getJams());

      ft = fm.beginTransaction();
      ft.show(mJamListFragment);

      if (!mDualPane) {
        ft.hide(mConnectionListFragment);
      } else {
        ft.show(mConnectionListFragment);
      }
      ft.commit();
    } else {
      ft = fm.beginTransaction();
      ft.show(mConnectionListFragment);
      ft.hide(mJamListFragment);
      ft.commit();
    }

    // start ConnectionService
    Intent serviceIntent = new Intent(this, ConnectionService.class);
    startService(serviceIntent);

    bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    // unbinde connection service
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
    case R.id.menu_add_bookmark:
      intent = new Intent(this, BookmarkEditActivity.class);
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

    // reload connection list after connection edit
    if (resultCode == Activity.RESULT_OK)
      mConnectionListFragment.reload();
  }

  @Override
  public void onConnectionClick(int position, Bookmark connection) {
    mState.setSelectedConnection(connection);

    String message = getResources().getString(R.string.dialog_opening_connection, connection.getUri().getHost());
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setTitle(R.string.dialog_title);
    mProgressDialog.setMessage(message);
    mProgressDialog.show();

    AbstractConnection c;

    try {
      c = mConnectionBinding.createConnection(connection.getUri());
      mState.setCurrentClientConnection(c);
      c.open().addActivityListener(this, "onConnectionFulfilled");
    } catch (ProtocolNotSupportedException e) {
      mState.setCurrentClientConnection(null);

      mProgressDialog.dismiss();
      mProgressDialog = null;

      Toast.makeText(this, e.getLocalizedMessage(this), Toast.LENGTH_LONG).show();
      return;
    }
  }

  @Override
  public boolean onConnectionLongClick(int position, Bookmark connection) {
    Intent intent = new Intent(this, BookmarkEditActivity.class);
    intent.putExtra("id", connection.getId());
    startActivityForResult(intent, 0);
    return true;
  }

  @Override
  public void onJamClick(Jam jam) {
    if (mProgressDialog != null)
      mProgressDialog.dismiss();

    String message = getResources().getString(R.string.dialog_loading_layouts_message);
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setTitle(R.string.dialog_title);
    mProgressDialog.setMessage(message);
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

    if (promise.isSuccess())
      showSelectLayoutDialog(promise.getResult());
    else
      Toast.makeText(this, promise.getCause().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
  }

  private void showSelectLayoutDialog(List<Layout> layouts) {
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
  }

  /**
   * Load selected layout
   * 
   * @param layout
   */
  private void loadLayout(Layout layout) {
    if (mProgressDialog != null)
      mProgressDialog.dismiss();

    String message = getResources().getString(R.string.dialog_loading_selected_layout_message, layout.getTitle());
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setTitle(R.string.dialog_title);
    mProgressDialog.setMessage(message);
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
    if (promise.isSuccess()) {
      AbstractConnection c = promise.getResult();
      c.getClosePromise().addActivityListener(this, "onConnectionCloseFulfilled");

      loadJams();
    } else {
      mState.setSelectedConnection(null);
      mState.setCurrentClientConnection(null);

      if (mProgressDialog != null) {
        mProgressDialog.dismiss();
        mProgressDialog = null;
      }
      Toast.makeText(this, promise.getCause().getLocalizedMessage(), Toast.LENGTH_LONG).show();
    }
  }

  private void loadJams() {
    if (mProgressDialog != null) {
      String message = getResources().getString(R.string.dialog_loading_jam_sessions_message);
      mProgressDialog.setMessage(message);
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
    mState.setJams(jams);
    mJamListFragment.setJams(jams);

    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft = fm.beginTransaction();

    if (mDualPane) {
      ft.show(mJamListFragment);
    } else {
      ft.hide(mConnectionListFragment);
      ft.show(mJamListFragment);
    }
    ft.commit();
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
    mConnectionBinding = null;
  }

  @Override
  public void onBackPressed() {
    // set selected connection to null
    if (!mDualPane && mState.getJams() != null) {
      mState.reset();
      mConnectionListFragment.setSelectedConnection(null);

      FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
      ft.hide(mJamListFragment);
      ft.show(mConnectionListFragment);
      ft.commit();
    } else {
      super.onBackPressed();
    }
  }
}