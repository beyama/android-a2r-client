package eu.addicted2random.a2rclient.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.actionbarsherlock.app.SherlockListFragment;

import eu.addicted2random.a2rclient.adapter.ConnectionAdapter;
import eu.addicted2random.a2rclient.dao.ConnectionDAO;
import eu.addicted2random.a2rclient.models.Connection;

public class ConnectionListFragment extends SherlockListFragment implements OnItemClickListener, OnItemLongClickListener {
  
  public interface OnConnectionClickListener {
    void onConnectionClick(Connection connection);
    boolean onConnectionLongClick(Connection connection);
  }
  
  private OnConnectionClickListener mListener;
  private ConnectionDAO mDAO;
  
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mListener = (OnConnectionClickListener)activity;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    mDAO = new ConnectionDAO(getActivity());
    
    // AQuery aq = new AQuery(getActivity());
    
    ConnectionAdapter adapter = new ConnectionAdapter(getActivity());
    
    ProgressDialog dialog = new ProgressDialog(getActivity());

    dialog.setIndeterminate(true);
    dialog.setCancelable(true);
    dialog.setInverseBackgroundForced(false);
    dialog.setCanceledOnTouchOutside(true);
    dialog.setTitle("Loading...");
    
    adapter.fromDB(mDAO);
    // adapter.fromAjax(aq.progress(dialog), "http://192.168.1.100:8080/sessions.json");
    
    setListAdapter(adapter);
    setRetainInstance(true);
  }
  
  public void reload() {
    ConnectionAdapter adapter = (ConnectionAdapter)getListAdapter();
    adapter.clear();
    adapter.fromDB(mDAO);
  }
  

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    
    getListView().setOnItemClickListener(this);
    getListView().setOnItemLongClickListener(this);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Connection connection = (Connection)getListAdapter().getItem(position);
    mListener.onConnectionClick(connection);
  }
  
  @Override
  public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
    Connection connection = (Connection)getListAdapter().getItem(position);
    return mListener.onConnectionLongClick(connection);
  }

  
}
