package eu.addicted2random.a2rclient.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import eu.addicted2random.a2rclient.ControlGridActivity;
import eu.addicted2random.a2rclient.R;
import eu.addicted2random.a2rclient.grid.Element;
import eu.addicted2random.a2rclient.grid.Layout;
import eu.addicted2random.a2rclient.grid.Section;

public class GridFragment extends SherlockFragment {
  final static String TAG = "GridFragment";
  
  @SuppressWarnings("unused")
  private static void v(String message) {
    Log.v(TAG, message);
  }
  
  @SuppressWarnings("unused")
  private static void v(String message, Object ...args) {
    Log.v(TAG, String.format(message, args));
  }
  
  // Column width in dp
  static public final int COL_WIDTH = 19;
 
  // Max column count
  static public final int COLS = 48;
  
  // Fragment max width (COL_WIDTH * COLS) 
  static public final int MAX_WIDTH = COL_WIDTH * COLS;
  
  private GridLayout mGridLayout;
  
  private float mScale;
  
  private int mColWidthPx;
  
  private String mSectionId;
  
  private Section mSection;
  
  private ControlGridActivity mActivity;
  
  private Layout mLayout;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    
    mScale = activity.getResources().getDisplayMetrics().density;
    mColWidthPx = Math.round(mScale * COL_WIDTH);
    
    mActivity = (ControlGridActivity)activity;

    if(mLayout == null) {
      mLayout = mActivity.getLayout();
      mSection = mLayout.getSection(mSectionId);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    this.setRetainInstance(true);
    
    View view = inflater.inflate(R.layout.grid_fragment_view, container, false);
    mGridLayout = (GridLayout)view.findViewById(R.id.gridLayout);
    
    // Add section elements to grid layout
    for (Element<?> e : mSection.getElements()) {
      add(e);
    }
    
    return view;
  }

  /**
   * Add an element to the grid.
   * 
   */
  public void add(Element<?> e) {
    View view = e.newInstance(getActivity());
    int viewId = mLayout.getViewId(e.getId());
    
    e.setViewId(viewId);
    view.setId(viewId);
    
    GridLayout.Spec colSpec = GridLayout.spec(e.getX(), e.getCols());
    GridLayout.Spec rowSpec = GridLayout.spec(e.getY(), e.getRows(), GridLayout.BASELINE);
    
    GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);
    params.width = mColWidthPx * e.getCols();
    params.height = mColWidthPx * e.getRows();
    
    int padding = Math.round(mScale * 5f);
    
    view.setPadding(padding, padding, padding, padding);
    
    mGridLayout.addView(view, params); 
  }
  
  @Override
  public void onDestroy() {
    super.onDestroy();
    
    for(Element<?> e : mSection.getElements())
      e.resetView();
  }

  public void setSectionId(String sectionId) {
    mSectionId = sectionId;
  }

}
