package eu.addicted2random.a2rclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.json.JSONException;

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

import eu.addicted2random.a2rclient.grid.IdMap;
import eu.addicted2random.a2rclient.grid.TabListener;
import eu.addicted2random.a2rclient.grid.models.InvalidLayoutException;
import eu.addicted2random.a2rclient.grid.models.Layout;
import eu.addicted2random.a2rclient.grid.models.Section;
import eu.addicted2random.a2rclient.services.AbstractConnection;
import eu.addicted2random.a2rclient.services.ConnectionService;

public class ControlGridActivity extends SherlockFragmentActivity implements ServiceConnection {
  
  /**
   * Task to load layout in background.
   */
  private class LoadLayoutTask extends AsyncTask<String, Integer, Layout> {

    /**
     * Open und parse layout
     */
    protected Layout doInBackground(String... resource) {
      try {
        InputStream stream = getAssets().open(resource[0]);
        Layout layout = Layout.fromJSON(stream);
        stream.close();
        return layout;
      } catch (IOException e) {
        onError(e);
      } catch (JSONException e) {
        onError(e);
      } catch (InvalidLayoutException e) {
        onError(e);
      }
      return null;
    }
    
    /**
     * Notify user about error.
     */
    private void onError(Exception e) {
      e.printStackTrace();
      Looper.prepare();
      Toast.makeText(ControlGridActivity.this, R.string.layout_error, Toast.LENGTH_SHORT).show();
      Looper.loop();
    }

    @Override
    protected void onPostExecute(Layout layout) {
      super.onPostExecute(layout);
      ControlGridActivity.this.onLayoutLoaded(layout);
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

  private IdMap mIdMap;

  private Layout mLayout;

  private ConnectionService mService;

  private int mCurrentSelectedTab = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState == null) {
      mIdMap = new IdMap();
    } else {
      mIdMap = (IdMap) savedInstanceState.getSerializable("idMap");
      mLayout = (Layout) savedInstanceState.getSerializable("layout");
      mCurrentSelectedTab = savedInstanceState.getInt("currentSelectedTab");
    }
    
    openConnection();
  }

  @Override
  protected void onStop() {
    super.onStop();
    // Unbind service if bound
    if(mService != null) {
      unbindService(this);
      mService = null;
    }
  }

  private void openConnection() {
    // Bind to LocalService
    Intent intent = new Intent(this, ConnectionService.class);
    URI uri = (URI) getIntent().getSerializableExtra("uri");
    intent.putExtra("uri", uri);
    bindService(intent, this, Context.BIND_AUTO_CREATE);
  }

  private synchronized void loadLayout() {
    if(mLayout == null) {
      new LoadLayoutTask().execute("grid-layouts/grid_layout.json");
    } else {
      renderLayout();
    }
  }
  
  private void onLayoutLoaded(Layout layout) {
    mLayout = layout;
    renderLayout();
  }
  
  private synchronized void renderLayout() {
    if(mLayout == null)
      return;
    
    ActionBar actionBar = getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    actionBar.setDisplayShowTitleEnabled(true);

    if (mLayout.getTitle() != null)
      actionBar.setTitle(mLayout.getTitle());

    for (Section section : mLayout.getSections()) {
      Tab tab = actionBar.newTab().setText(section.getTitle() == null ? section.getName() : section.getTitle())
          .setTabListener(new TabListener(this, section, mIdMap));
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
    outState.putSerializable("layout", mLayout);
    outState.putSerializable("idMap", mIdMap);
  }

  @Override
  public void onServiceConnected(ComponentName name, IBinder service) {
    ConnectionService.Binder binder = (ConnectionService.Binder) service;
    mService = binder.getService();
    loadLayout();
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
    mService = null;
    // close activity if service disconnects
    finish();
  }
  
  public AbstractConnection getConnection() {
    if(mService == null) return null;
    return mService.getConnection();
  }

}
