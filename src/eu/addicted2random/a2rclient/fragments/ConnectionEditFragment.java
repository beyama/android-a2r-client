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

import eu.addicted2random.a2rclient.R;
import eu.addicted2random.a2rclient.dao.ConnectionDAO;
import eu.addicted2random.a2rclient.models.Connection;

public class ConnectionEditFragment extends SherlockFragment {
  
  public interface OnConnectionEditListener {
    void onConnectionUpdated(Connection connection);
    void onConnectionCreated(Connection connection);
    void onConnectionDestroyed(Connection connection);
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
      ConnectionEditFragment.this.afterTextChanged(id, s);
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
  
  private OnConnectionEditListener mListener;
  
  private ConnectionDAO mDAO;
  
  private Connection mConnection;
  
  private MenuItem mSaveMenuItem;
  
  private MenuItem mDeleteMenuItem;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity); 
    mListener = (OnConnectionEditListener)activity;
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    mDAO = new ConnectionDAO(getActivity().getApplicationContext());
    
    long id = getActivity().getIntent().getLongExtra("connectionId", -1);
    
    if(id != -1)
      mConnection = mDAO.get(id);

    if(mConnection == null)
      mConnection = new Connection();
    
    setRetainInstance(true);
    setHasOptionsMenu(true);
  }
  
  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    
    AQuery aq = new AQuery(view);
    
    EditText title = aq.id(R.id.connectionTitleEdit)
        .text(mConnection.getTitle())
        .getEditText();
    
    EditText description = aq.id(R.id.connectionDescriptionEdit)
        .text(mConnection.getDescription())
        .getEditText();
    
    EditText uri = aq.id(R.id.connectionUriEdit).getEditText();
    
    if(mConnection.getUri() != null)
        uri.setText(mConnection.getUri().toString());
      
    title.addTextChangedListener(new InputTextWatcher(title.getId()));
    description.addTextChangedListener(new InputTextWatcher(description.getId()));
    uri.addTextChangedListener(new InputTextWatcher(uri.getId()));
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_connection_edit, container, false);
  }
  
  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    
    inflater.inflate(R.menu.fragment_connection_edit, menu);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    
    mSaveMenuItem = menu.findItem(R.id.menu_connection_save);
    mDeleteMenuItem = menu.findItem(R.id.menu_connection_delete);
    
    if(!mConnection.isValid())
      setEnabled(mSaveMenuItem, false);
    else
      setEnabled(mSaveMenuItem, true);
    
    if(mConnection.getId() == null)
      setEnabled(mDeleteMenuItem, false);
    else
      setEnabled(mDeleteMenuItem, true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.menu_connection_save:
      if(mConnection.getId() == null) {
        mDAO.add(mConnection);
        mListener.onConnectionCreated(mConnection);
      } else {
        mDAO.update(mConnection);
        mListener.onConnectionUpdated(mConnection);
      }
      break;
    case R.id.menu_connection_delete:
      mDAO.delete(mConnection);
      mListener.onConnectionDestroyed(mConnection);
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
    case R.id.connectionTitleEdit:
      mConnection.setTitle(s.toString());
      break;
    case R.id.connectionDescriptionEdit:
      mConnection.setDescription(s.toString());
      break;
    case R.id.connectionUriEdit:
      try {
        URI uri = URI.create(s.toString());
        mConnection.setUri(uri);
      } catch(IllegalArgumentException e) {
      }
      break;
    }
    
    if(mConnection.isValid()) {
      setEnabled(mSaveMenuItem, true);
    } else {
      setEnabled(mSaveMenuItem, false);
    }
  }

}
