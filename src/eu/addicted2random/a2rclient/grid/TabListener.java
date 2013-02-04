package eu.addicted2random.a2rclient.grid;

import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.addicted2random.a2rclient.GridFragment;
import eu.addicted2random.a2rclient.grid.models.Section;

public class TabListener implements ActionBar.TabListener {

  private final static String TAG = "TabListener";

  @SuppressWarnings("unused")
  private static void v(String message) {
    Log.v(TAG, message);
  }

  @SuppressWarnings("unused")
  private static void v(String message, Object... args) {
    Log.v(TAG, String.format(message, args));
  }

  private GridFragment mFragment;
  private final SherlockFragmentActivity mActivity;
  private final Section mSection;
  private final IdMap mIdMap;

  /**
   * Constructor used each time a new tab is created.
   * 
   * @param activity
   *          The host Activity, used to instantiate the fragment
   * @param tag
   *          The identifier tag for the fragment
   * @param section
   *          The layout section model for this fragment
   */
  public TabListener(SherlockFragmentActivity activity, Section section, IdMap idMap) {
    mActivity = activity;
    mSection = section;
    mIdMap = idMap;
  }

  /* The following are each of the ActionBar.TabListener callbacks */
  public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
    if(mFragment == null)
      mFragment = (GridFragment)mActivity.getSupportFragmentManager().findFragmentByTag(mSection.getName());

    // Check if the fragment is already initialized
    if (mFragment == null) {
      // If not, instantiate and add it to the activity
      mFragment = (GridFragment) SherlockFragment.instantiate(mActivity, GridFragment.class.getName());
      mFragment.setSection(mSection);
      mFragment.setIdMap(mIdMap);

      ft.add(android.R.id.content, mFragment, mSection.getName());
    } else {
      // If it exists, simply attach it in order to show it
      ft.attach(mFragment);
    }
  }

  public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    if (mFragment != null) {
      // Detach the fragment, because another one is being attached
      ft.detach(mFragment);
    }
  }

  public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    // User selected the already selected tab. Usually do nothing.
  }
}