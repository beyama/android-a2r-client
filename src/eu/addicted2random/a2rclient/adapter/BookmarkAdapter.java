package eu.addicted2random.a2rclient.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import com.androidquery.AQuery;

import eu.addicted2random.a2rclient.R;
import eu.addicted2random.a2rclient.models.Bookmark;

public class BookmarkAdapter extends ArrayAdapter<Bookmark> {
  
  private Bookmark mSelectedBookmark;
  
  private AQuery mAq;
  
  public BookmarkAdapter(Context context) {
    super(context, R.layout.bookmark_list_item);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    RelativeLayout layout = null;
    
    Bookmark bookmark = this.getItem(position);
    
    if(convertView != null) {
      layout = (RelativeLayout)convertView;
    } else {
      LayoutInflater li = LayoutInflater.from(getContext());
      layout = (RelativeLayout)li.inflate(R.layout.bookmark_list_item, parent, false);
    }
    
    if(mAq == null)
      mAq = new AQuery(layout);
    else
      mAq.recycle(layout);
    
    if(mSelectedBookmark != null && mSelectedBookmark.equals(bookmark))
      layout.setBackgroundResource(R.drawable.selected_connection_item);
    else
      layout.setBackgroundResource(Color.TRANSPARENT);
    
    mAq.id(R.id.bookmarkTitle).text(bookmark.getTitle());
    
    mAq.id(R.id.bookmarkDescription).text(bookmark.getDescription());
    
    return layout;
  }
  
  public void setSelectedBookmark(Bookmark bookmark) {
    mSelectedBookmark = bookmark;
  }
  
  public Bookmark getSelectedBookmark() {
    return mSelectedBookmark;
  }
}