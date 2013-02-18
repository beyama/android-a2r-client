package eu.addicted2random.a2rclient.models.layout;

import eu.addicted2random.a2rclient.osc.Pack;
import eu.addicted2random.a2rclient.osc.PackSupport;
import eu.addicted2random.a2rclient.osc.Types;
import android.content.Context;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class ToggleButtonElement extends Element<ToggleButton> {
  
  private class ToggleButtonCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
      Pack pack = getPack();
      pack.lock(buttonView);
      pack.set(0, isChecked);
      pack.unlock();
    }
    
  }
  
  public ToggleButtonElement(String type, int x, int y, int cols, int rows) {
    super(type, x, y, cols, rows);
  }

  private static final long serialVersionUID = 4710754252242077877L;

  @Override
  protected ToggleButton createInstance(Context context) {
    ToggleButton btn = new ToggleButton(context);
    btn.setOnCheckedChangeListener(new ToggleButtonCheckedChangeListener());
    return btn;
  }

  @Override
  protected Pack createPack() {
    Pack pack = new PackSupport(Types.BOOLEAN_TYPE, false);
    return pack;
  }

  @Override
  protected void onSync() {
    getView().setChecked((Boolean)getPack().get(0));
  }

}
