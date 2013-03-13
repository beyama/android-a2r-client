package eu.addicted2random.a2rclient;

import java.net.URI;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

import eu.addicted2random.a2rclient.fragments.GridFragment;
import eu.addicted2random.a2rclient.grid.Layout;
import eu.addicted2random.a2rclient.grid.Section;
import eu.addicted2random.a2rclient.grid.TabListener;
import eu.addicted2random.a2rclient.net.AbstractConnection;
import eu.addicted2random.a2rclient.net.ConnectionService;
import eu.addicted2random.a2rclient.net.ConnectionServiceBinding;
import eu.addicted2random.a2rclient.utils.Promise;

public class ControlGridActivity extends SherlockFragmentActivity implements ServiceConnection {
  
  @SuppressWarnings("unused")
  private final static String TAG = "ControlGridActivity";

  private ConnectionServiceBinding mConnectionBinding = null;

  private AbstractConnection mConnection = null;

  private int mCurrentSelectedTab = 0;
  
  private Layout mLayout = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState != null) {
      mCurrentSelectedTab = savedInstanceState.getInt("currentSelectedTab");
    }

    Intent intent = new Intent(this, ConnectionService.class);
    bindService(intent, this, Context.BIND_AUTO_CREATE);
  }

  @Override
  protected void onStop() {
    super.onStop();
    
    if(mConnection != null) {
      mConnection.getClosePromise().removeActivityListener(this);
    }

    // Unbind service if bound
    if (mConnectionBinding != null) {
      unbindService(this);
      mConnectionBinding = null;
    }
  }

  private synchronized void renderLayout() {
    Layout layout = getLayout();

    if (layout == null)
      return;

    if(layout.getSections().size() == 1) {
      FragmentManager fm = getSupportFragmentManager();
      FragmentTransaction ft = fm.beginTransaction();
      
      Section section = layout.getSections().get(0);
      
      GridFragment gridFragment = (GridFragment)fm.findFragmentByTag(section.getId());
      
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
    
    URI uri = (URI) getIntent().getSerializableExtra("uri");
    
    mConnection = mConnectionBinding.getConnection(uri);
    
    mConnection.getClosePromise().addActivityListener(this, "onConnectionCloseFulfilled");
    
    mLayout = mConnection.getCurrentSelectedLayout();
    
    renderLayout();
  }
  
  protected void onConnectionCloseFulfilled(Promise<AbstractConnection> promise) {
    mConnection = null;
    Toast.makeText(this, R.string.connection_closed, Toast.LENGTH_LONG).show();
    finish();
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
    mConnectionBinding = null;
    // close activity if service disconnects
    finish();
  }

  public Layout getLayout() {
    return mLayout;
  }

  public AbstractConnection getConnection() {
    return mConnection;
  }

}
