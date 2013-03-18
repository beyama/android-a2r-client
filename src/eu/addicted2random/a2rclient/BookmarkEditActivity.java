package eu.addicted2random.a2rclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import eu.addicted2random.a2rclient.fragments.BookmarkEditFragment.OnBookmarkEditListener;
import eu.addicted2random.a2rclient.models.Bookmark;

public class BookmarkEditActivity extends SherlockFragmentActivity implements OnBookmarkEditListener {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_bookmark_edit);
    // Show the Up button in the action bar.
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getSupportMenuInflater().inflate(R.menu.activity_bookmark_edit, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case android.R.id.home:
      // This ID represents the Home or Up button. In the case of this
      // activity, the Up button is shown. Use NavUtils to allow users
      // to navigate up one level in the application structure. For
      // more details, see the Navigation pattern on Android Design:
      //
      // http://developer.android.com/design/patterns/navigation.html#up-vs-back
      //
      NavUtils.navigateUpFromSameTask(this);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBookmarkUpdated(Bookmark bookmark) {
    Intent intent = new Intent("updated");
    intent.putExtra("id", bookmark.getId());
    setResult(RESULT_OK, intent);
    finish();
  }

  @Override
  public void onBookmarkCreated(Bookmark bookmark) {
    Intent intent = new Intent("created");
    intent.putExtra("id", bookmark.getId());
    setResult(Activity.RESULT_OK);
    finish();
  }

  @Override
  public void onBookmarkDestroyed(Bookmark bookmark) {
    Intent intent = new Intent("destroyed");
    intent.putExtra("id", bookmark.getId());
    setResult(Activity.RESULT_OK);
    finish();
  }

}
