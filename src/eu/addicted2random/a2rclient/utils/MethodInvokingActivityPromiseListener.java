package eu.addicted2random.a2rclient.utils;

import java.lang.reflect.Method;

import android.app.Activity;

/**
 * A generic implementation of {@link ActivityPromiseListener} that calls a named method on the activity with the
 * fulfilled {@link Promise}.
 * 
 * @author Alexander Jentz, beyama.de
 *
 * @param <Result>
 */
public class MethodInvokingActivityPromiseListener<Result> extends ActivityPromiseListener<Activity, Result> {

  private final Method method;
  
  public MethodInvokingActivityPromiseListener(Activity activity, String method) throws NoSuchMethodException {
    super(activity);
    this.method = activity.getClass().getDeclaredMethod(method, new Class[] { Promise.class });
    this.method.setAccessible(true);
  }

  @Override
  protected void onFulfilled(Promise<Result> promise) {
    try {
      method.invoke(getActivity(), promise);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

}
