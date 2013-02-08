package eu.addicted2random.a2rclient.models.layout;

import android.content.Context;
import eu.addicted2random.a2rclient.osc.Pack;
import eu.addicted2random.a2rclient.widgets.ADSR;

public class ADSRElement extends Element<ADSR> {
  private static final long serialVersionUID = -885815685358024664L;
  
  public ADSRElement(String type, int x, int y, int cols, int rows) {
    super(type, x, y, cols, rows);
  }

  @Override
  protected ADSR createInstance(Context context) {
    return new ADSR(context);
  }

  @Override
  protected void onSync() {
  }

  @Override
  protected Pack createPack() {
    // TODO Auto-generated method stub
    return null;
  }

}
