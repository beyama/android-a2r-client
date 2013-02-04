package eu.addicted2random.a2rclient;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import eu.addicted2random.a2rclient.grid.IdMap;
import eu.addicted2random.a2rclient.grid.models.Element;
import eu.addicted2random.a2rclient.grid.models.Section;

public class GridFragment extends Fragment {
  final static String TAG = "GridFragment";
  
  @SuppressWarnings("unused")
  private static void v(String message) {
    Log.v(TAG, message);
  }
  
  private static void v(String message, Object ...args) {
    Log.v(TAG, String.format(message, args));
  }
  
  // Column width in dp
  static public final int COL_WIDTH = 19;
 
  // Max column count
  static public final int COLS = 48;
  
  // Fragment max width (COL_WIDTH * COLS) 
  static public final int MAX_WIDTH = COL_WIDTH * COLS;
  
  private GridLayout mGridLayout = null;
  
  private float mScale;
  
  private int mColWidthPx;
  
  private int mCols = 0;
  
  private Section mSection;
  
  private IdMap mIdMap;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    
    mScale = activity.getResources().getDisplayMetrics().density;
    
    mColWidthPx = Math.round(mScale * COL_WIDTH);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    
    View view = inflater.inflate(R.layout.grid_fragment_view, container, false);
    mGridLayout = (GridLayout)view.findViewById(R.id.gridLayout);
    
    if(savedInstanceState != null) {
      mSection = (Section)savedInstanceState.getSerializable("section");
      mIdMap = (IdMap)savedInstanceState.getSerializable("idMap");
    }
    
    for (Element<?> e : mSection.getElements()) {
      v("Add element from section %s", e.getId());
      add(e);
    }
    
    return view;
  }

  /**
   * Add an element to the grid.
   * 
   */
  public void add(Element<?> e) {
    int c = e.getX() + e.getCols();
    
    if(c > 24)
      throw new IllegalArgumentException("Maximal 24 rows allowed");
    
    View view = e.createInstance(getActivity());
    int viewId = mIdMap.getId(e.getId());
    
    e.setViewId(viewId);
    view.setId(viewId);
    
    if(c > mCols)
      mCols = c;
    
    GridLayout.Spec colSpec = GridLayout.spec(e.getX(), e.getCols());
    GridLayout.Spec rowSpec = GridLayout.spec(e.getY(), e.getRows(), GridLayout.BASELINE);
    
    GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);
    params.width = mColWidthPx * e.getCols();
    params.height = mColWidthPx * e.getRows();
    
    int padding = Math.round(mScale * 5f);
    
    view.setPadding(padding, padding, padding, padding);
    
    e.setupView(view);
    
    v("Add view with id %d", view.getId());
    mGridLayout.addView(view, params);
  }
  
  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putSerializable("section", mSection);
    outState.putSerializable("idMap", mIdMap);
  }
  
  public void setIdMap(IdMap idMap) {
    mIdMap = idMap;
  }
  
  public void setSection(Section section) {
    mSection = section;
  }

  /**
   * Get column count of the grid.
   * 
   * @return
   */
  public int getCols() {
    return mCols;
  }

}
