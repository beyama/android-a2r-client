package eu.addicted2random.a2rclient.fragments;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.androidquery.AQuery;

import eu.addicted2random.a2rclient.R;
import eu.addicted2random.a2rclient.jam.Jam;

public class JamListFragment extends SherlockListFragment implements OnItemClickListener {
  
  public class JamAdapter extends ArrayAdapter<Jam> {

    private AQuery mAq;
    
    public JamAdapter(Context context) {
      super(context, R.layout.jam_list_item);
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
      RelativeLayout layout = null;
      
      final Jam jam = this.getItem(position);
      
      if(convertView != null) {
        layout = (RelativeLayout)convertView;
      } else {
        LayoutInflater li = LayoutInflater.from(getContext());
        layout = (RelativeLayout)li.inflate(R.layout.jam_list_item, parent, false);
      }
      
      if(mAq == null)
        mAq = new AQuery(layout);
      else
        mAq.recycle(layout);

      mAq.id(R.id.jamTitle).text(jam.getTitle());
      
      mAq.id(R.id.jamDescription).text(jam.getDescription());
      
      return layout;
    }
    
  }

  public interface OnJamClickListener {
    void onJamClick(Jam jam);
  }

  private OnJamClickListener mListener;

  private List<Jam> mJams;

  public JamListFragment() {
    super();
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mListener = (OnJamClickListener) activity;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_jam_list, container, false);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    JamAdapter adapter = new JamAdapter(getActivity());
    if (mJams != null)
      adapter.addAll(mJams);

    setListAdapter(adapter);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    getListView().setOnItemClickListener(this);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Jam jam = (Jam) getListAdapter().getItem(position);
    mListener.onJamClick(jam);
  }

  public void setJams(List<Jam> jams) {
    mJams = jams;

    JamAdapter adapter = getJamAdapter();
    if (adapter != null) {
      adapter.clear();
      if (jams != null)
        adapter.addAll(jams);
    }
  }

  public JamAdapter getJamAdapter() {
    return (JamAdapter) getListAdapter();
  }

  /**
   * Set title of fragment view.
   * 
   * @param title
   */
  public void setTitle(String title) {
    Activity activity = getActivity();
    TextView titleView = (TextView) activity.findViewById(R.id.jamsTitle);
    title = activity.getString(R.string.section_title_jams, title);
    titleView.setText(title);
  }

}
