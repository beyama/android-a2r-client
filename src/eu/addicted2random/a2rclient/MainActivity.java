package eu.addicted2random.a2rclient;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import eu.addicted2random.a2rclient.dialogs.LayoutChooserDialog;
import eu.addicted2random.a2rclient.dialogs.LayoutChooserDialog.OnLayoutSelectListener;
import eu.addicted2random.a2rclient.exceptions.A2RException;
import eu.addicted2random.a2rclient.exceptions.ProtocolNotSupportedException;
import eu.addicted2random.a2rclient.fragments.BookmarkListFragment;
import eu.addicted2random.a2rclient.fragments.BookmarkListFragment.OnBookmarkClickListener;
import eu.addicted2random.a2rclient.fragments.JamListFragment;
import eu.addicted2random.a2rclient.fragments.JamListFragment.OnJamClickListener;
import eu.addicted2random.a2rclient.grid.Layout;
import eu.addicted2random.a2rclient.jam.Jam;
import eu.addicted2random.a2rclient.models.Bookmark;
import eu.addicted2random.a2rclient.net.ConnectionHandler;
import eu.addicted2random.a2rclient.net.ConnectionService;
import eu.addicted2random.a2rclient.net.ConnectionServiceBinder;
import eu.addicted2random.a2rclient.utils.Promise;

/**
 * Main activity shows list of bookmarks and corresponding jams and handle
 * connections and selection of layouts.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class MainActivity extends SherlockFragmentActivity implements ServiceConnection, OnBookmarkClickListener,
    OnJamClickListener, OnLayoutSelectListener {

  @SuppressWarnings("unused")
  private final static String TAG = "MainActivity";

  private final static String STATE_TAG = "stateFragment";

  private final static String LAYOUT_CHOOSER_DIALOG = "dialogChooser";
  
  private static final String ON_LAYOUT_LOADED = "onLayoutLoaded";

  private static final String ON_LAYOUTS_LOADED = "onLayoutsLoaded";

  private static final String ON_CONNECTION_HANDLER_CLOSE_FULFILLED = "onConnectionHandlerCloseFulfilled";

  private static final String ON_CONNECTION_HANDLER_FULFILLED = "onConnectionHandlerFulfilled";

  /**
   * Holds the state of the main activity.
   */
  static public class MainActivityStateFragment extends SherlockFragment {

    /* currently selected bookmark */
    private Bookmark mSelectedBookmark;

    public MainActivityStateFragment() {
      super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setRetainInstance(true);
    }

    /**
     * Get selected bookmark.
     * 
     * @return
     */
    public Bookmark getSelectedBookmark() {
      return mSelectedBookmark;
    }

    /**
     * Set selected bookmark.
     * 
     * @param bookmark
     */
    public void setSelectedBookmark(Bookmark bookmark) {
      mSelectedBookmark = bookmark;
    }

  }

  private ConnectionService mConnectionService;

  /* dual pane layout? */
  private boolean mDualPane = false;

  private MainActivityStateFragment mState;

  private ConnectionHandler mHandler;

  private BookmarkListFragment mBookmarkListFragment;

  private JamListFragment mJamListFragment;

  private ProgressDialog mProgressDialog = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // initialize A2R
    A2R.getInstance(this);

    setContentView(R.layout.activity_main);

    // dual pane?
    mDualPane = getResources().getBoolean(R.bool.dualPane);

    FragmentManager fm = getSupportFragmentManager();

    // get fragments from fragment manager
    mBookmarkListFragment = (BookmarkListFragment) fm.findFragmentById(R.id.bookmarkListFragment);
    mJamListFragment = (JamListFragment) fm.findFragmentById(R.id.jamListFragment);
    mState = (MainActivityStateFragment) fm.findFragmentByTag(STATE_TAG);

    // create and add state fragment if not exist
    if (mState == null) {
      mState = new MainActivityStateFragment();
      fm.beginTransaction().add(mState, STATE_TAG).commit();
    }

    // start connection service
    Intent intent = new Intent(this, ConnectionService.class);
    startService(intent);

    bindService(intent, this, Service.START_STICKY);

    // set current selected bookmark from state fragment
    setSelectedBookmark(mState.getSelectedBookmark());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.actionbarsherlock.app.SherlockFragmentActivity#onPause()
   */
  @Override
  protected void onPause() {
    super.onPause();
    unregisterConnectionHandlerListeners();
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.support.v4.app.FragmentActivity#onResume()
   */
  @Override
  protected void onResume() {
    super.onResume();

    if (mHandler != null) {
      if (mHandler.isClosed())
        onConnectionHandlerCloseFulfilled(mHandler.getClosePromise());
      else if(mHandler.getOpenPromise().isDone())
        onConnectionHandlerFulfilled(mHandler.getOpenPromise());
      else
        mHandler.getOpenPromise().addActivityListener(this, ON_CONNECTION_HANDLER_FULFILLED);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.actionbarsherlock.app.SherlockFragmentActivity#onDestroy()
   */
  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (mConnectionService != null)
      unbindService(this);
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

    // reload connection list after bookmark edit/create
    if (resultCode == Activity.RESULT_OK)
      mBookmarkListFragment.reload();
  }

  /**
   * Set selected bookmark to clicked bookmark.
   */
  @Override
  public void onBookmarkClick(int position, Bookmark bookmark) {
    setSelectedBookmark(bookmark);
  }

  /**
   * Start {@link BookmarkEditActivity} for long clicked bookmark.
   */
  @Override
  public boolean onBookmarkLongClick(int position, Bookmark connection) {
    Intent intent = new Intent(this, BookmarkEditActivity.class);
    intent.putExtra("id", connection.getId());
    startActivityForResult(intent, 0);
    return true;
  }

  /**
   * Load and show list of available layouts for clicked jam.
   */
  @Override
  public void onJamClick(Jam jam) {
    mHandler.setSelectedJam(jam);
    loadLayouts();
  }

  /**
   * Load selected layout.
   * 
   * @param layout
   */
  @Override
  public void onLayoutSelect(int index, Layout layout) {
    mHandler.setSelectedLayout(layout);
    loadLayout();
  }

  /**
   * Open a handler an load jams.
   */
  protected void openHandler() {
    if (mHandler.isOpen()) {
      onConnectionHandlerFulfilled(mHandler.getOpenPromise());
    } else {
      String message = getResources().getString(R.string.dialog_opening_connection, mHandler.getUri().getHost());
      showProgressDialog(R.string.dialog_title, message);
      mHandler.getOpenPromise().addActivityListener(this, ON_CONNECTION_HANDLER_FULFILLED);
    }
  }

  /**
   * Connection handler open callback.
   * 
   * @param promise
   */
  protected void onConnectionHandlerFulfilled(Promise<ConnectionHandler> promise) {
    if (promise.isSuccess()) {
      // register close listener
      promise.getResult().getClosePromise().addActivityListener(this, ON_CONNECTION_HANDLER_CLOSE_FULFILLED);
      loadJams();
    } else {
      setSelectedBookmark(null);

      dismissProgressDialog();

      Throwable throwable = promise.getCause();
      throwable.printStackTrace();
      String message;

      if (throwable instanceof A2RException)
        message = ((A2RException) throwable).getLocalizedMessage(this);
      else
        message = throwable.getLocalizedMessage();

      Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
  }

  /**
   * Connection handler close callback.
   * 
   * @param promise
   */
  protected void onConnectionHandlerCloseFulfilled(Promise<ConnectionHandler> promise) {
    dismissProgressDialog();
    setSelectedBookmark(null);

    // remove layout chooser dialog
    FragmentManager fm = getSupportFragmentManager();
    LayoutChooserDialog dialog = (LayoutChooserDialog) fm.findFragmentByTag(LAYOUT_CHOOSER_DIALOG);
    if (dialog != null)
      fm.beginTransaction().remove(dialog).commit();

    Toast.makeText(this, R.string.connection_closed, Toast.LENGTH_LONG).show();
  }

  /**
   * Load a list of available jams from the server.
   */
  private void loadJams() {
    Promise<List<Jam>> jamsPromise = mHandler.getJams();

    if (jamsPromise.isSuccess()) {
      dismissProgressDialog();
      setJams(jamsPromise.getResult());
    } else {
      String message = getResources().getString(R.string.dialog_loading_jam_sessions_message);
      showProgressDialog(R.string.dialog_title, message);

      mHandler.getJams().addActivityListener(this, "onJamsLoaded");
    }
  }

  /**
   * Jams loaded callback.
   * 
   * @param promise
   */
  protected void onJamsLoaded(Promise<List<Jam>> promise) {
    dismissProgressDialog();

    if (promise.isSuccess()) {
      setJams(promise.getResult());
    } else {
      Throwable throwable = promise.getCause();
      String message;

      if (throwable instanceof A2RException)
        message = ((A2RException) throwable).getLocalizedMessage(this);
      else
        message = throwable.getLocalizedMessage();

      setSelectedBookmark(null);

      Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
  }

  /**
   * Load layouts and show a layout select dialog.
   */
  private void loadLayouts() {
    Promise<List<Layout>> layoutsPromise = mHandler.getLayouts();

    if (layoutsPromise.isSuccess()) {
      showSelectLayoutDialog(layoutsPromise.getResult());
    } else {
      String message = getResources().getString(R.string.dialog_loading_layouts_message);
      showProgressDialog(R.string.dialog_title, message);

      layoutsPromise.addActivityListener(this, ON_LAYOUTS_LOADED);
    }

  }

  /**
   * Layout list loaded callback.
   * 
   * @param promise
   */
  protected void onLayoutsLoaded(Promise<List<Layout>> promise) {
    dismissProgressDialog();

    if (promise.isSuccess()) {
      showSelectLayoutDialog(promise.getResult());
    } else {
      Toast.makeText(this, promise.getCause().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Show a dialog to select a layout.
   * 
   * @param promise
   */
  private void showSelectLayoutDialog(List<Layout> layouts) {
    LayoutChooserDialog.newInstance(layouts).show(getSupportFragmentManager(), LAYOUT_CHOOSER_DIALOG);
  }

  /**
   * Load and open selected layout.
   */
  private void loadLayout() {
    Promise<Layout> layoutPromise = mHandler.getLayout();

    if (layoutPromise.isSuccess()) {
      openLayout(layoutPromise.getResult());
    } else {
      String message = getResources().getString(R.string.dialog_loading_selected_layout_message,
          mHandler.getSelectedLayout().getTitle());
      showProgressDialog(R.string.dialog_title, message);

      layoutPromise.addActivityListener(this, ON_LAYOUT_LOADED);
    }
  }

  /**
   * Layout loaded callback.
   * 
   * This will start the {@link ControlGridActivity}.
   * 
   * @param promise
   */
  protected void onLayoutLoaded(Promise<Layout> promise) {
    if (promise.isSuccess()) {
      openLayout(promise.getResult());
    } else {
      dismissProgressDialog();
      promise.getCause().printStackTrace();
      Toast.makeText(this, promise.getCause().getLocalizedMessage(), Toast.LENGTH_LONG).show();
    }
  }

  protected void openLayout(Layout layout) {
    Promise<Boolean> joinPromise = mHandler.join();

    if (joinPromise.isSuccess()) {
      onJamJoined(joinPromise);
    } else {
      String message = getString(R.string.dialog_joining_session, layout.getJam().getTitle());
      showProgressDialog(R.string.dialog_title, message);

      joinPromise.addActivityListener(this, "onJamJoined");
    }
  }

  protected void onJamJoined(Promise<Boolean> promise) {
    dismissProgressDialog();

    if (promise.isSuccess()) {
      Intent intent = new Intent(this, ControlGridActivity.class);
      startActivity(intent);
    } else {
      promise.getCause().printStackTrace();
      Toast.makeText(this, promise.getCause().getLocalizedMessage(), Toast.LENGTH_LONG).show();
    }
  }

  private void unregisterConnectionHandlerListeners() {
    if (mHandler != null) {
      mHandler.getOpenPromise().removeActivityListener(this);
      mHandler.getClosePromise().removeActivityListener(this);
    }
  }

  private void setSelectedBookmark(Bookmark bookmark) {
    Bookmark previousBookmark = mState.getSelectedBookmark();

    mState.setSelectedBookmark(bookmark);
    mBookmarkListFragment.setSelectedBookmark(bookmark);

    if (bookmark == null) {
      setJams(null);

      if (mHandler != null) {
        unregisterConnectionHandlerListeners();
        mConnectionService.closeHandler();
        mHandler = null;
      }
    } else {
      if (previousBookmark == null || !previousBookmark.equals(bookmark)) {
        setJams(null);

        String message = getResources().getString(R.string.dialog_opening_connection, bookmark.getUri().getHost());
        showProgressDialog(R.string.dialog_title, message);

        unregisterConnectionHandlerListeners();

        mConnectionService.closeHandler();
        mHandler = null;

        try {
          mHandler = mConnectionService.open(bookmark.getUri());
          openHandler();
        } catch (ProtocolNotSupportedException e) {
          setSelectedBookmark(null);
          dismissProgressDialog();

          Toast.makeText(this, e.getLocalizedMessage(this), Toast.LENGTH_LONG).show();
          return;
        }
      }

      mJamListFragment.setTitle(bookmark.getTitle());
    }
  }

  private void setJams(List<Jam> jams) {
    mJamListFragment.setJams(jams);

    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft;

    // handle fragment visibility
    if (jams != null) {
      ft = fm.beginTransaction();
      ft.show(mJamListFragment);

      if (!mDualPane) {
        ft.hide(mBookmarkListFragment);
      } else {
        ft.show(mBookmarkListFragment);
      }
      ft.commit();
    } else {
      ft = fm.beginTransaction();
      ft.show(mBookmarkListFragment);
      ft.hide(mJamListFragment);
      ft.commit();
    }
  }

  private ProgressDialog showProgressDialog(int titleId, String message) {
    if (mProgressDialog == null) {
      mProgressDialog = new ProgressDialog(this);
      mProgressDialog.show();
    }
    mProgressDialog.setTitle(titleId);
    mProgressDialog.setMessage(message);

    return mProgressDialog;
  }

  private void dismissProgressDialog() {
    if (mProgressDialog == null)
      return;

    mProgressDialog.dismiss();
    mProgressDialog = null;
  }

  @Override
  public void onBackPressed() {
    // set selected connection to null
    if (!mDualPane && mJamListFragment.isVisible()) {
      setSelectedBookmark(null);
    } else {
      mConnectionService.closeHandler();
      super.onBackPressed();
    }
  }

  @Override
  public void onServiceConnected(ComponentName name, IBinder service) {
    mConnectionService = ((ConnectionServiceBinder) service).getService();
    mHandler = mConnectionService.getHandler();

    if (mHandler != null)
      openHandler();
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
  }

}