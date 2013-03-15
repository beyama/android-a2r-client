package eu.addicted2random.a2rclient.fragments;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockListFragment;

import eu.addicted2random.a2rclient.R;
import eu.addicted2random.a2rclient.adapter.JamAdapter;
import eu.addicted2random.a2rclient.jam.Jam;

public class JamListFragment extends SherlockListFragment implements OnItemClickListener {
  
  public interface OnJamClickListener {
    void onJamClick(Jam jam);
  }
  
  private OnJamClickListener mListener;
  
  private List<Jam> mJams;
  
  public JamListFragment() {
    super();
    // TODO Auto-generated constructor stub
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mListener = (OnJamClickListener)activity;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_jam_list, container, false);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    JamAdapter adapter = new JamAdapter(getActivity());
    if(mJams != null)
      adapter.addAll(mJams);
    
    setListAdapter(adapter);
    // setRetainInstance(true);
  }
  
  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    
    getListView().setOnItemClickListener(this);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Jam jam = (Jam)getListAdapter().getItem(position);
    mListener.onJamClick(jam);
  }
  
  public void setJams(List<Jam> jams) {
    mJams = jams;
    
    JamAdapter adapter = getJamAdapter();
    if(adapter != null) {
      adapter.clear();
      adapter.addAll(jams);
    }
  }
    
  public JamAdapter getJamAdapter() {
    return (JamAdapter)getListAdapter();
  }

}
