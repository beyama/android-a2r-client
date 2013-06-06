package eu.addicted2random.a2rclient;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		TextView text = (TextView)findViewById(R.id.about);
		text.setMovementMethod(LinkMovementMethod.getInstance());
	}

}
