package eu.addicted2random.a2rclient.grid;

import android.view.View;

public abstract class ViewBinding {
  public interface ViewValueChangedListener {
    public void onViewValueChanged(View view, int index, Object value);
  }
  
  private final View view;
  private ViewValueChangedListener listener;
  
  public ViewBinding(View view) {
    super();
    this.view = view;
  }
  
  abstract public void onUpdate(int index, Object value);
  
  public void changed(int index, Object value) {
    if(listener != null)
      listener.onViewValueChanged(view, index, value);
  }
  
  public void setViewValueChangedListener(ViewValueChangedListener listener) {
    this.listener = listener;
  }
  
}
