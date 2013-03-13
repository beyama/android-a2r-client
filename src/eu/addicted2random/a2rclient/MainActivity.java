package eu.addicted2random.a2rclient;

import java.net.URI;
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
import android.widget.ArrayAdapter;
import android.widget.Toast;

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
  final static String TAG = "MainActivity";

  final static String CONNECTION_LIST_TAG = "connectionList";

  final static String JAM_LIST_TAG = "jamList";

  private URI mUri;

  private boolean mDualPane = false;

  private ConnectionServiceBinding mConnectionBinding = null;

  private AbstractConnection mConnection = null;

  private ProgressDialog mProgressDialog = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (findViewById(R.id.rightPane) != null)
      mDualPane = true;

    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft;

    ConnectionListFragment connectionListFragment = (ConnectionListFragment) fm.findFragmentByTag(CONNECTION_LIST_TAG);

    if (mDualPane) {
      if (connectionListFragment == null) {
        ft = fm.beginTransaction();
        ft.add(R.id.leftPane, new ConnectionListFragment(), CONNECTION_LIST_TAG);
        ft.commit();
      } else {
        ft = fm.beginTransaction();
        ft.replace(R.id.leftPane, connectionListFragment, CONNECTION_LIST_TAG);
        ft.commit();
      }
    } else {
      if (connectionListFragment == null) {
        // add connection list fragment
        ft = fm.beginTransaction();
        ft.add(R.id.leftPane, new ConnectionListFragment(), CONNECTION_LIST_TAG);
        ft.commit();
      }
    }

    Intent serviceIntent = new Intent(this, ConnectionService.class);
    bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
  }

//  @Override
//  protected void onResume() {
//    super.onResume();
//
//    if (mUri != null)
//      stopService(new Intent(this, ConnectionService.class));
//  }

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
  public void onConnectionClick(Connection connection) {
    // TODO: close previous opened connections
    try {
      mConnection = mConnectionBinding.createConnection(connection.getUri());
    } catch (ProtocolNotSupportedException e) {
      Toast.makeText(this, e.getLocalizedMessage(this), Toast.LENGTH_LONG).show();
      return;
    }

    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setTitle(R.string.dialog_title);
    mProgressDialog.setMessage(getResources().getString(R.string.dialog_opening_connection,
        connection.getUri().getHost()));
    mProgressDialog.show();

    mConnection.open().addActivityListener(this, "onConnectionFulfilled");
  }

  @Override
  public boolean onConnectionLongClick(Connection connection) {
    Intent intent = new Intent(this, ConnectionEditActivity.class);
    intent.putExtra("connectionId", connection.getId());
    startActivityForResult(intent, 0);
    return true;
  }

  @Override
  public void onJamClick(Jam jam) {
    if(mProgressDialog != null) mProgressDialog.dismiss();
    
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setTitle(R.string.dialog_title);
    mProgressDialog.setMessage(getResources().getString(R.string.dialog_loading_layouts_message));
    mProgressDialog.show();
    
    WebSocketConnection ws = (WebSocketConnection) mConnection;
    ws.getJamService().getLayouts(jam).addActivityListener(this, "onLayoutsLoaded");
  }

  /**
   * Show a dialog to select a layout.
   * 
   * @param promise
   */
  protected void onLayoutsLoaded(Promise<List<Layout>> promise) {
    if(mProgressDialog != null) {
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
   * @param layout
   */
  private void loadLayout(Layout layout) {
    if(mProgressDialog != null) mProgressDialog.dismiss();
    
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setTitle(R.string.dialog_title);
    mProgressDialog.setMessage(getResources().getString(R.string.dialog_loading_selected_layout_message, layout.getTitle()));
    mProgressDialog.show();
    
    WebSocketConnection ws = (WebSocketConnection) mConnection;
    ws.getJamService().getLayout(getApplicationContext(), layout.getJam(), layout.getId()).addActivityListener(this, "onLayoutLoaded");
  }
  
  protected void onLayoutLoaded(Promise<Layout> promise) {
    if(mProgressDialog != null) {
      mProgressDialog.dismiss();
      mProgressDialog = null;
    }

    if(promise.isSuccess()) {
      mConnection.setCurrentSelectedLayout(promise.getResult());
      Intent intent = new Intent(this, ControlGridActivity.class);
      intent.putExtra("uri", mConnection.getURI());
      startActivity(intent);
    } else {
      Toast.makeText(this, promise.getCause().getLocalizedMessage(), Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public void onServiceConnected(ComponentName name, IBinder service) {
    mConnectionBinding = (ConnectionServiceBinding) service;
  }

  protected void onConnectionFulfilled(Promise<AbstractConnection> promise) {

    if (promise.isSuccess()) {
      mConnection = promise.getResult();
      mConnection.getClosePromise().addActivityListener(this, "onConnectionCloseFulfilled");

      if (mProgressDialog != null) {
        mProgressDialog.setMessage(getResources().getString(R.string.dialog_loading_jam_sessions_message));
      }

      WebSocketConnection ws = (WebSocketConnection) mConnection;
      ws.getJamService().getAll().addActivityListener(this, "onJamsLoaded");
    } else {
      if (mProgressDialog != null) {
        mProgressDialog.dismiss();
        mProgressDialog = null;
      }
      Toast.makeText(this, promise.getCause().getLocalizedMessage(), Toast.LENGTH_LONG).show();
    }
  }

  protected void onJamsLoaded(Promise<List<Jam>> promise) {
    if (mProgressDialog != null) {
      mProgressDialog.dismiss();
      mProgressDialog = null;
    }

    showJams(promise.getResult());
  }

  private void showJams(List<Jam> jams) {
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
    mConnection = null;
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
  }

}
