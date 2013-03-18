package eu.addicted2random.a2rclient.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

import eu.addicted2random.a2rclient.A2R;
import eu.addicted2random.a2rclient.R;
import eu.addicted2random.a2rclient.adapter.BookmarkAdapter;
import eu.addicted2random.a2rclient.models.Bookmark;

public class BookmarkListFragment extends SherlockListFragment implements OnItemClickListener,
    OnItemLongClickListener {

  public interface OnBookmarkClickListener {
    void onConnectionClick(int index, Bookmark bookmark);

    boolean onConnectionLongClick(int index, Bookmark connection);
  }
  
  private A2R a2r;
  private OnBookmarkClickListener mListener;
  private BookmarkAdapter mAdapter;
  private Bookmark mSelectedConnection;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    
    a2r = A2R.getInstance(activity);
    mListener = (OnBookmarkClickListener) activity;
    
    if(mAdapter == null) {
      mAdapter = new BookmarkAdapter(activity);
      mAdapter.setSelectedBookmark(mSelectedConnection);
      reload();
    }
    
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);


    setListAdapter(mAdapter);
    setRetainInstance(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_bookmark_list, container, false);
  }

  public void reload() {
    mAdapter.clear();
    for (Bookmark b : a2r.getAllBookmarks())
      mAdapter.add(b);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    ListView lv = getListView();
    lv.setOnItemClickListener(this);
    lv.setOnItemLongClickListener(this);
  }

  /**
   * Set currently selected connection.
   * 
   * @param connection
   */
  public void setSelectedConnection(Bookmark connection) {
    mSelectedConnection = connection;
    
    if(mAdapter != null) {
      mAdapter.setSelectedBookmark(connection);
      mAdapter.notifyDataSetChanged();
    }
  }

  /**
   * Get currently selected connection.
   * 
   * @return
   */
  public Bookmark getSelectedConnection() {
    return mSelectedConnection;
  }

  /*
   * (non-Javadoc)
   * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
   */
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Bookmark bookmark = (Bookmark) getListAdapter().getItem(position);
    setSelectedConnection(bookmark);
    mListener.onConnectionClick(position, bookmark);
  }

  /*
   * (non-Javadoc)
   * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView, android.view.View, int, long)
   */
  @Override
  public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
    Bookmark bookmark = (Bookmark) getListAdapter().getItem(position);
    return mListener.onConnectionLongClick(position, bookmark);
  }

}
