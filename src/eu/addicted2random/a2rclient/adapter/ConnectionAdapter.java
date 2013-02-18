package eu.addicted2random.a2rclient.adapter;

import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;

import eu.addicted2random.a2rclient.R;
import eu.addicted2random.a2rclient.dao.ConnectionDAO;
import eu.addicted2random.a2rclient.models.Connection;

public class ConnectionAdapter extends ArrayAdapter<Connection> {
  
  public ConnectionAdapter(Context context) {
    super(context, R.layout.connection_list_item);
  }
  
  public void fromDB(ConnectionDAO dao) {
    for(Connection c : dao.getAll())
      add(c);
  }
  
  public void fromAjax(AQuery aq, String address) {
    aq.ajax(address, JSONObject.class, 1000, this, "onConnectionListLoaded");
  }
  
  /**
   * Ajax callback.
   * 
   * @param url
   * @param json
   * @param status
   */
  public void onConnectionListLoaded(String url, JSONObject json, AjaxStatus status) {

    if (json != null) {
      try {
        JSONArray sessions = json.getJSONArray("items");
        if (sessions == null) {
          return;
        }

        for (int i = 0; i < sessions.length(); i++) {
          JSONObject js = sessions.getJSONObject(i);
          Connection connection = Connection.fromJSONObject(js);
          add(connection);
        }
      } catch (JSONException e) {
        Toast.makeText(getContext(), R.string.sessions_format_error, Toast.LENGTH_LONG).show();
      } catch (URISyntaxException e) {
        Toast.makeText(getContext(), R.string.sessions_format_error, Toast.LENGTH_LONG).show();
      }

    } else {
      Toast.makeText(getContext(), R.string.sessions_load_error, Toast.LENGTH_LONG).show();
      // we believe the request is a failure, don't cache it
      status.invalidate();
    }
  }
  
  

  @Override
  public View getDropDownView(int position, View convertView, ViewGroup parent) {
    Connection connection = getItem(position);
    
    TextView text;
    
    if(convertView != null) {
      text = (TextView)convertView;
    } else {
      text = new TextView(getContext());
    }
    
    text.setText(connection.getTitle());
    
    return text;
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
    
    AQuery aq = new AQuery(layout);
    
    aq.id(R.id.connectionImage).image(connection.getImage());
    
    aq.id(R.id.connectionTitle).text(connection.getTitle());
    
    aq.id(R.id.connectionDescription).text(connection.getDescription());
    
    return layout;
  }
}