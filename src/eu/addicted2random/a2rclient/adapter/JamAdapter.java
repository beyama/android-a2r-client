package eu.addicted2random.a2rclient.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import com.androidquery.AQuery;

import eu.addicted2random.a2rclient.R;
import eu.addicted2random.a2rclient.jam.Jam;

public class JamAdapter extends ArrayAdapter<Jam> {

  public JamAdapter(Context context) {
    super(context, R.layout.jam_list_item);
  }
  
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    RelativeLayout layout = null;
    
    Jam jam = this.getItem(position);
    
    if(convertView != null) {
      layout = (RelativeLayout)convertView;
    } else {
      LayoutInflater li = LayoutInflater.from(getContext());
      layout = (RelativeLayout)li.inflate(R.layout.jam_list_item, parent, false);
    }
    
    AQuery aq = new AQuery(layout);

    aq.id(R.id.jamTitle).text(jam.getTitle());
    
    aq.id(R.id.jamDescription).text(jam.getDescription());
    
    return layout;
  }
  
}
