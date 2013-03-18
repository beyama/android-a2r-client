package eu.addicted2random.a2rclient.fragments;

import java.net.URI;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.androidquery.AQuery;

import eu.addicted2random.a2rclient.A2R;
import eu.addicted2random.a2rclient.R;
import eu.addicted2random.a2rclient.models.Bookmark;

public class BookmarkEditFragment extends SherlockFragment {
  
  public interface OnBookmarkEditListener {
    void onBookmarkUpdated(Bookmark bookmark);
    void onBookmarkCreated(Bookmark bookmark);
    void onBookmarkDestroyed(Bookmark bookmark);
  }
  
  private class InputTextWatcher implements TextWatcher {
    final int id;
    
    public InputTextWatcher(int id) {
      this.id = id;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
      BookmarkEditFragment.this.afterTextChanged(id, s);
    }
  }
  
  private static void setEnabled(MenuItem item, boolean enabled) {
    if(enabled) {
      item.getIcon().setAlpha(255);
      item.setEnabled(true);
    } else {
      item.getIcon().setAlpha(75);
      item.setEnabled(false);
    }
  }
  
  private OnBookmarkEditListener mListener;
 
  private Bookmark mBookmark;
  
  private A2R a2r;
  
  private MenuItem mSaveMenuItem;
  
  private MenuItem mDeleteMenuItem;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity); 
    mListener = (OnBookmarkEditListener)activity;
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    long id = getActivity().getIntent().getLongExtra("id", -1);
    
    if(id != -1)
      mBookmark = a2r.getBookmark(id);

    if(mBookmark == null)
      mBookmark = new Bookmark();
    
    setRetainInstance(true);
    setHasOptionsMenu(true);
  }
  
  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    
    AQuery aq = new AQuery(view);
    
    EditText title = aq.id(R.id.bookmarkTitleEdit)
        .text(mBookmark.getTitle())
        .getEditText();
    
    EditText description = aq.id(R.id.bookmarkDescriptionEdit)
        .text(mBookmark.getDescription())
        .getEditText();
    
    EditText uri = aq.id(R.id.bookmarkUriEdit).getEditText();
    
    if(mBookmark.getUri() != null)
        uri.setText(mBookmark.getUri().toString());
      
    title.addTextChangedListener(new InputTextWatcher(title.getId()));
    description.addTextChangedListener(new InputTextWatcher(description.getId()));
    uri.addTextChangedListener(new InputTextWatcher(uri.getId()));
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_bookmark_edit, container, false);
  }
  
  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    
    inflater.inflate(R.menu.fragment_bookmark_edit, menu);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    
    mSaveMenuItem = menu.findItem(R.id.menu_bookmark_save);
    mDeleteMenuItem = menu.findItem(R.id.menu_bookmark_delete);
    
    if(!mBookmark.isValid())
      setEnabled(mSaveMenuItem, false);
    else
      setEnabled(mSaveMenuItem, true);
    
    if(mBookmark.getId() == null)
      setEnabled(mDeleteMenuItem, false);
    else
      setEnabled(mDeleteMenuItem, true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.menu_bookmark_save:
      if(mBookmark.getId() == null) {
        a2r.addBookmark(mBookmark);
        mListener.onBookmarkCreated(mBookmark);
      } else {
        a2r.updateBookmark(mBookmark);
        mListener.onBookmarkUpdated(mBookmark);
      }
      break;
    case R.id.menu_bookmark_delete:
      a2r.deleteBookmark(mBookmark);
      mListener.onBookmarkDestroyed(mBookmark);
      break;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Set edit text to model and validate.
   * 
   * @param id
   * @param s
   */
  private void afterTextChanged(int id, Editable s) {
    switch(id) {
    case R.id.bookmarkTitleEdit:
      mBookmark.setTitle(s.toString());
      break;
    case R.id.bookmarkDescriptionEdit:
      mBookmark.setDescription(s.toString());
      break;
    case R.id.bookmarkUriEdit:
      try {
        URI uri = URI.create(s.toString());
        mBookmark.setUri(uri);
      } catch(IllegalArgumentException e) {
      }
      break;
    }
    
    if(mBookmark.isValid()) {
      setEnabled(mSaveMenuItem, true);
    } else {
      setEnabled(mSaveMenuItem, false);
    }
  }

}
