package eu.addicted2random.a2rclient;

import java.net.URI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import eu.addicted2random.a2rclient.models.Connection;
import eu.addicted2random.a2rclient.services.ConnectionService;

public class MainActivity extends SherlockFragmentActivity {
  final static String TAG = "MainActivity";
  
  private URI mUri;
  
  @SuppressWarnings("unused")
  private static void v(String message) {
    Log.v(TAG, message);
  }
  
  @SuppressWarnings("unused")
  private static void v(String message, Object ...args) {
    Log.v(TAG, String.format(message, args));
  }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

  @Override
  protected void onResume() {
    super.onResume();
    
    if(mUri != null)
      stopService(new Intent(this, ConnectionService.class));
  }

  @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
  
  public void onConnectionSelected(Connection connection) {
    mUri = connection.getUri();
    
    Intent serviceIntent = new Intent(this, ConnectionService.class);
    serviceIntent.putExtra("uri", mUri);
    
    Intent activityIntent = new Intent(this, ControlGridActivity.class);
    activityIntent.putExtra("uri", mUri);

    startService(serviceIntent);
    startActivity(activityIntent);
  }
	
	@Override
  public boolean onOptionsItemSelected(MenuItem item) {
	  Intent intent;
	  
	  switch (item.getItemId()) {
    case R.id.menu_sensors:
      intent = new Intent(this, SensorActivity.class);
      startActivity(intent);
      break;
    default:
      return super.onOptionsItemSelected(item);
    }
	  return true;
  }

}
