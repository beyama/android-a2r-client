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
    void onBookmarkClick(int index, Bookmark bookmark);
    boolean onBookmarkLongClick(int index, Bookmark connection);
  }
  
  private A2R a2r;
  private OnBookmarkClickListener mListener;
  private BookmarkAdapter mAdapter;
  private Bookmark mSelectedBookmark;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    
    a2r = A2R.getInstance(activity);
    mListener = (OnBookmarkClickListener) activity;
    
    if(mAdapter == null) {
      mAdapter = new BookmarkAdapter(activity);
      mAdapter.setSelectedBookmark(mSelectedBookmark);
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
   * Set currently selected bookmark.
   * 
   * @param bookmark
   */
  public void setSelectedBookmark(Bookmark bookmark) {
    mSelectedBookmark = bookmark;
    
    if(mAdapter != null) {
      mAdapter.setSelectedBookmark(bookmark);
      mAdapter.notifyDataSetChanged();
    }
  }

  /**
   * Get currently selected bookmark.
   * 
   * @return
   */
  public Bookmark getSelectedBookmark() {
    return mSelectedBookmark;
  }

  /*
   * (non-Javadoc)
   * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
   */
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Bookmark bookmark = (Bookmark) getListAdapter().getItem(position);
    setSelectedBookmark(bookmark);
    mListener.onBookmarkClick(position, bookmark);
  }

  /*
   * (non-Javadoc)
   * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView, android.view.View, int, long)
   */
  @Override
  public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
    Bookmark bookmark = (Bookmark) getListAdapter().getItem(position);
    return mListener.onBookmarkLongClick(position, bookmark);
  }

}
