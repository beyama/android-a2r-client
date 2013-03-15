package eu.addicted2random.a2rclient.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.actionbarsherlock.app.SherlockListFragment;

import eu.addicted2random.a2rclient.R;
import eu.addicted2random.a2rclient.adapter.ConnectionAdapter;
import eu.addicted2random.a2rclient.dao.ConnectionDAO;
import eu.addicted2random.a2rclient.models.Connection;

public class ConnectionListFragment extends SherlockListFragment implements OnItemClickListener,
    OnItemLongClickListener {

  public interface OnConnectionClickListener {
    void onConnectionClick(int index, Connection connection);

    boolean onConnectionLongClick(int index, Connection connection);
  }

  private OnConnectionClickListener mListener;
  private ConnectionDAO mDAO;
  private ConnectionAdapter mAdapter;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mListener = (OnConnectionClickListener) activity;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mDAO = new ConnectionDAO(getActivity().getApplicationContext());

    mAdapter = new ConnectionAdapter(getActivity().getApplicationContext());

    mAdapter.fromDB(mDAO);

    setListAdapter(mAdapter);
    setRetainInstance(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_connection_list, container, false);
  }

  public void reload() {
    ConnectionAdapter adapter = (ConnectionAdapter) getListAdapter();
    adapter.clear();
    adapter.fromDB(mDAO);
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
  public void setSelectedConnection(Connection connection) {
    if(connection != mAdapter.getSelectedConnection()) {
      mAdapter.setSelectedConnection(connection);
      mAdapter.notifyDataSetChanged();
    }
  }

  /**
   * Get currently selected connection.
   * 
   * @return
   */
  public Connection getSelectedConnection() {
    return mAdapter.getSelectedConnection();
  }

  /*
   * (non-Javadoc)
   * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
   */
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Connection connection = (Connection) getListAdapter().getItem(position);
    setSelectedConnection(connection);
    mListener.onConnectionClick(position, connection);
  }

  /*
   * (non-Javadoc)
   * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView, android.view.View, int, long)
   */
  @Override
  public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
    Connection connection = (Connection) getListAdapter().getItem(position);
    return mListener.onConnectionLongClick(position, connection);
  }

}
