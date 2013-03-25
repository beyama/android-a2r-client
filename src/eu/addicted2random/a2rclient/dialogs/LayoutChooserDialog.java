package eu.addicted2random.a2rclient.dialogs;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.SherlockDialogFragment;

import eu.addicted2random.a2rclient.R;
import eu.addicted2random.a2rclient.grid.Layout;

public class LayoutChooserDialog extends SherlockDialogFragment {

  public interface OnLayoutSelectListener {
    public void onLayoutSelect(int index, Layout layout);
  }

  static public LayoutChooserDialog newInstance(List<Layout> layouts) {
    LayoutChooserDialog fragment = new LayoutChooserDialog();
    fragment.setLayouts(layouts);
    return fragment;
  }

  private List<Layout> mLayouts;
  private OnLayoutSelectListener mListener;
  private ArrayAdapter<Layout> mLayoutAdapter;

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.actionbarsherlock.app.SherlockDialogFragment#onAttach(android.app.Activity
   * )
   */
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mListener = (OnLayoutSelectListener) activity;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
   */
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    mLayoutAdapter = new ArrayAdapter<Layout>(getActivity(), R.layout.layout_list_item);

    // add layouts to the layout adapter
    setLayouts(mLayouts);

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    builder.setTitle(R.string.dialog_choose_layout);

    builder.setAdapter(mLayoutAdapter, new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        Layout layout = mLayoutAdapter.getItem(which);
        mListener.onLayoutSelect(which, layout);
      }
    });

    return builder.create();
  }

  /**
   * @return the layouts
   */
  public List<Layout> getLayouts() {
    return mLayouts;
  }

  /**
   * @param layouts
   *          the layouts to set
   */
  public void setLayouts(List<Layout> layouts) {
    mLayouts = layouts;

    if (mLayoutAdapter != null) {
      mLayoutAdapter.clear();
      
      if (mLayouts != null) {
        for (Layout layout : mLayouts)
          mLayoutAdapter.add(layout);
      }
    }
  }

}
