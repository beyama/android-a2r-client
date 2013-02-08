package eu.addicted2random.a2rclient.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.androidquery.AQuery;

import eu.addicted2random.a2rclient.MainActivity;
import eu.addicted2random.a2rclient.adapter.ConnectionAdapter;
import eu.addicted2random.a2rclient.models.Connection;

public class ConnectionListFragment extends SherlockListFragment {
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    AQuery aq = new AQuery(getActivity());
    
    ConnectionAdapter adapter = new ConnectionAdapter(getActivity());
    
    ProgressDialog dialog = new ProgressDialog(getActivity());

    dialog.setIndeterminate(true);
    dialog.setCancelable(true);
    dialog.setInverseBackgroundForced(false);
    dialog.setCanceledOnTouchOutside(true);
    dialog.setTitle("Loading...");
    adapter.fromAjax(aq.progress(dialog), "http://192.168.1.100:8080/sessions.json");
    
    setListAdapter(adapter);
    setRetainInstance(true);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    Connection connection = (Connection)getListAdapter().getItem(position);
    ((MainActivity)getActivity()).onConnectionSelected(connection);
  }

  
}
