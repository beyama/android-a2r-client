package eu.addicted2random.a2rclient;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

import eu.addicted2random.a2rclient.fragments.GridFragment;
import eu.addicted2random.a2rclient.grid.Layout;
import eu.addicted2random.a2rclient.grid.Section;
import eu.addicted2random.a2rclient.grid.TabListener;
import eu.addicted2random.a2rclient.net.AbstractConnectionHandler;
import eu.addicted2random.a2rclient.net.ConnectionHandler;
import eu.addicted2random.a2rclient.net.ConnectionService;
import eu.addicted2random.a2rclient.net.ConnectionServiceBinder;
import eu.addicted2random.a2rclient.utils.Promise;

public class ControlGridActivity extends SherlockFragmentActivity implements ServiceConnection {

  @SuppressWarnings("unused")
  private final static String TAG = "ControlGridActivity";

  private ConnectionService mConnectionService;

  private ConnectionHandler mConnectionHandler = null;

  private int mCurrentSelectedTab = 0;

  private Layout mLayout = null;

  /*
   * (non-Javadoc)
   * 
   * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState != null) {
      mCurrentSelectedTab = savedInstanceState.getInt("currentSelectedTab");
    }

    bindService(new Intent(this, ConnectionService.class), this, Service.START_STICKY);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.actionbarsherlock.app.SherlockFragmentActivity#onDestroy()
   */
  @Override
  protected void onDestroy() {
    super.onDestroy();
    
    if (mConnectionHandler != null)
      mConnectionHandler.getClosePromise().removeActivityListener(this);
    
    if (mConnectionService != null)
      unbindService(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.actionbarsherlock.app.SherlockFragmentActivity#onCreateOptionsMenu(
   * android.view.Menu)
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getSupportMenuInflater().inflate(R.menu.activity_control_grid, menu);
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.actionbarsherlock.app.SherlockFragmentActivity#onSaveInstanceState(
   * android.os.Bundle)
   */
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt("currentSelectedTab", getSupportActionBar().getSelectedNavigationIndex());
  }

  /**
   * Render layout.
   */
  private void renderLayout() {
    ActionBar actionBar = getSupportActionBar();
  
    actionBar.setDisplayShowTitleEnabled(true);
  
    if (mLayout.getTitle() != null)
      actionBar.setTitle(mLayout.getTitle());
  
    if (mLayout.getSections().size() == 1) {
      FragmentManager fm = getSupportFragmentManager();
      FragmentTransaction ft = fm.beginTransaction();
  
      Section section = mLayout.getSections().get(0);
  
      GridFragment gridFragment = (GridFragment) fm.findFragmentByTag(section.getId());
  
      // Check if the fragment is already initialized
      if (gridFragment == null) {
        // If not, instantiate and add it to the activity
        gridFragment = (GridFragment) SherlockFragment.instantiate(this, GridFragment.class.getName());
        gridFragment.setSectionId(section.getId());
  
        ft.add(android.R.id.content, gridFragment, section.getId());
      } else {
        // If it exists, simply attach it in order to show it
        ft.attach(gridFragment);
      }
      ft.commit();
    } else {
      actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
  
      for (Section section : mLayout.getSections()) {
        Tab tab = actionBar.newTab().setText(section.getTitle() == null ? section.getName() : section.getTitle())
            .setTabListener(new TabListener(this, section.getId()));
        actionBar.addTab(tab);
      }
  
      actionBar.setSelectedNavigationItem(mCurrentSelectedTab);
    }
  }

  /**
   * {@link AbstractConnectionHandler} close callback to close this activity if 
   * handler closed.
   * 
   * @param promise
   */
  protected void onConnectionHandlerCloseFulfilled(Promise<ConnectionHandler> promise) {
    finish();
  }

  public Layout getLayout() {
    return mLayout;
  }

  public ConnectionHandler getConnectionHandler() {
    return mConnectionHandler;
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.content.ServiceConnection#onServiceConnected(android.content.
   * ComponentName, android.os.IBinder)
   */
  @Override
  public void onServiceConnected(ComponentName name, IBinder service) {
    mConnectionService = ((ConnectionServiceBinder) service).getService();

    
    mConnectionHandler = mConnectionService.getHandler();

    if (mConnectionHandler == null) {
      finish();
      return;
    }
    
    mConnectionHandler.getClosePromise().addActivityListener(this, "onConnectionHandlerCloseFulfilled");
    
    mLayout = mConnectionHandler.getLayout().getResult();
  
    if (mLayout == null) {
      finish();
      return;
    }
    
    renderLayout();
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
  }

}
