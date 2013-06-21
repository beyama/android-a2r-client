package eu.addicted2random.a2rclient.grid;

import java.util.concurrent.locks.ReentrantLock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import android.content.Context;
import eu.addicted2random.a2rclient.osc.Pack;
import eu.addicted2random.a2rclient.osc.PackSupport;
import eu.addicted2random.a2rclient.osc.Types;
import eu.addicted2random.a2rclient.widgets.Spinner;

public class SpinnerElement extends Element<Spinner> {
  @JsonCreator
  public SpinnerElement(@JsonProperty("type") String type, @JsonProperty("x") int x, @JsonProperty("y") int y,
      @JsonProperty("cols") int cols, @JsonProperty("rows") int rows) {
    super(type, x, y, cols, rows);
  }
  
  private class SpinnerEventListener implements Spinner.SpinnerEventListener {

    @Override
    public void onSpinnerLengthCalculated(float length) {
      Spinner spinner = SpinnerElement.this.getView();
      Pack pack = SpinnerElement.this.getPack();
      
      pack.lock(spinner);
      pack.set(0, length);
      pack.unlock();
    }
    
  }

  private static final long serialVersionUID = -6173773756422942893L;

  @Override
  protected Spinner onCreateView(Context context) {
    Spinner spinner = new Spinner(context);
    spinner.setListener(new SpinnerEventListener());
    return spinner;
  }

  @Override
	protected void onSetupView() {
  	// set default padding
  	if(getPaddingLeft() == null && getPaddingTop() == null && getPaddingRight() == null && getPaddingBottom() == null)
  		setPadding(5);
  	
		super.onSetupView();
	}

	@Override
  protected void onResetView() {
    super.onResetView();
    getView().setListener(null);
  }

  @Override
  protected Pack onCreatePack(ReentrantLock lock) {
    return new PackSupport(Types.FLOAT_TYPE, Float.valueOf(0), lock);
  }

  @Override
  protected void onSync() {
  }

}
