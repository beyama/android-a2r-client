package eu.addicted2random.a2rclient.grid;

import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.addicted2random.a2rclient.osc.Pack;
import eu.addicted2random.a2rclient.osc.PackSupport;
import eu.addicted2random.a2rclient.osc.Types;

/**
 * An element that creates a {@link ToggleButton} in the grid.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
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

  @JsonCreator
  public ToggleButtonElement(@JsonProperty("type") String type, @JsonProperty("x") int x, @JsonProperty("y") int y,
      @JsonProperty("cols") int cols, @JsonProperty("rows") int rows) {
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
	protected void setupView() {
  	// set default padding
   	if(getPaddingLeft() == null && getPaddingTop() == null && getPaddingRight() == null && getPaddingBottom() == null)
   		setPadding(5);

		super.setupView();
	}

	@Override
  protected Pack createPack(ReentrantLock lock) {
    Pack pack = new PackSupport(Types.BOOLEAN_TYPE, false, lock);
    return pack;
  }

  @Override
  protected void onSync() {
    getView().setChecked((Boolean) getPack().get(0));
  }

}
