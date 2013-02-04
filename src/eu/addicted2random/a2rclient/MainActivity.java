package eu.addicted2random.a2rclient;

import java.net.URI;
import java.net.URISyntaxException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import eu.addicted2random.a2rclient.services.ConnectionService;

public class MainActivity extends Activity {
  
  final static String TAG = "MainActivity";
  
  private Intent mConnectionServiceIntent = null;
  
  @SuppressWarnings("unused")
  private static void v(String message) {
    Log.v(TAG, message);
  }
  
  @SuppressWarnings("unused")
  private static void v(String message, Object ...args) {
    Log.v(TAG, String.format(message, args));
  }
	
	private EditText mUriInput;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mUriInput = (EditText)findViewById(R.id.uri);
	}

  @Override
  protected void onDestroy() {
    super.onDestroy();
    
    if(mConnectionServiceIntent != null)
      stopService(mConnectionServiceIntent);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    
    mConnectionServiceIntent = (Intent)savedInstanceState.getParcelable("connectionServiceIntent");
  }

  @Override
  protected void onResume() {
    super.onResume();
    
    if(mConnectionServiceIntent != null) {
      v("Stopping connection service");
      stopService(mConnectionServiceIntent);
      mConnectionServiceIntent = null;
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    
    if(mConnectionServiceIntent != null)
      outState.putParcelable("connectionServiceIntent", mConnectionServiceIntent);
  }

  @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
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
	
	public void onConnectClicked(View view) {
	  URI uri;
    try {
      uri = new URI(mUriInput.getText().toString());
      Intent intent = new Intent(this, ControlGridActivity.class);
      intent.putExtra("uri", uri);
      
      // start connection service
      mConnectionServiceIntent = new Intent(this, ConnectionService.class);
      mConnectionServiceIntent.putExtra("uri", uri);
      startService(mConnectionServiceIntent);
      
      startActivity(intent);
    } catch (URISyntaxException e) {
      Toast.makeText(this, R.string.invalid_uri, Toast.LENGTH_SHORT).show();
    }
	}

}
