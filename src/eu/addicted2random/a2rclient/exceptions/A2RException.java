/**
 * 
 */
package eu.addicted2random.a2rclient.exceptions;

import android.content.Context;

/**
 * Base class of all recoverable A2R exceptions.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public abstract class A2RException extends Exception {

  private static final long serialVersionUID = -4925456681502719569L;

  public A2RException() {
    super();
  }

  public A2RException(String detailMessage, Throwable throwable) {
    super(detailMessage, throwable);
  }

  public A2RException(String detailMessage) {
    super(detailMessage);
  }

  public A2RException(Throwable throwable) {
    super(throwable);
  }

  /**
   * Give a localized message.
   * 
   * @param context
   * @return
   */
  abstract public String getLocalizedMessage(Context context);

}
