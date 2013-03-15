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
import eu.addicted2random.a2rclient.dao.ConnectionDAO;
import eu.addicted2random.a2rclient.models.Connection;

public class ConnectionAdapter extends ArrayAdapter<Connection> {
  
  private Connection mSelectedConnection;
  
  private AQuery mAq;
  
  public ConnectionAdapter(Context context) {
    super(context, R.layout.connection_list_item);
  }
  
  public void fromDB(ConnectionDAO dao) {
    for(Connection c : dao.getAll())
      add(c);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    RelativeLayout layout = null;
    
    Connection connection = this.getItem(position);
    
    if(convertView != null) {
      layout = (RelativeLayout)convertView;
    } else {
      LayoutInflater li = LayoutInflater.from(getContext());
      layout = (RelativeLayout)li.inflate(R.layout.connection_list_item, parent, false);
    }
    
    if(mAq == null)
      mAq = new AQuery(layout);
    else
      mAq.recycle(layout);
    
    if(mSelectedConnection == connection)
      layout.setBackgroundResource(R.drawable.selected_connection_item);
    else
      layout.setBackgroundResource(Color.TRANSPARENT);
    
    mAq.id(R.id.connectionTitle).text(connection.getTitle());
    
    mAq.id(R.id.connectionDescription).text(connection.getDescription());
    
    return layout;
  }
  
  public void setSelectedConnection(Connection connection) {
    mSelectedConnection = connection;
  }
  
  public Connection getSelectedConnection() {
    return mSelectedConnection;
  }
}