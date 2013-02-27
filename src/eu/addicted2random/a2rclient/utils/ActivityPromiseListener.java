package eu.addicted2random.a2rclient.utils;

import java.lang.ref.WeakReference;

import android.app.Activity;

/**
 * This class implements {@link PromiseListener} and is designed to handle
 * results of asynchronous background tasks in the UI thread.
 * 
 * The listener holds a weak reference to the corresponding activity and checks
 * the activity live-state before calling the
 * {@link ActivityPromiseListener#onFulfilled()} method. So this invalidates
 * itself if the activity is finishing or garbage collected.
 * 
 * If you don't implement this class as non-static inner class of your activity
 * class then the activity can be garbage collected without waiting for the
 * completion of the background task.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 * @param <V>
 * @param <Result>
 */
public abstract class ActivityPromiseListener<V extends Activity, Result> implements PromiseListener<Result>, Runnable {

  private final WeakReference<V> activityReference;

  private Promise<Result> promise;
  
  public ActivityPromiseListener(V activity) {
    super();
    activityReference = new WeakReference<V>(activity);
  }

  /**
   * Override this to work with the fulfilled promise.
   */
  protected abstract void onFulfilled(Promise<Result> promise);

  /**
   * Run {@link ActivityPromiseListener#onFulfilled()} on the UI thread.
   * 
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    V activity = activityReference.get();

    if (activity != null && !activity.isFinishing())
      onFulfilled(getPromise());
  }

  /**
   * Run this in UI thread if the activity is still alive.
   * 
   * @see eu.addicted2random.a2rclient.utils.PromiseListener#opperationComplete(eu.addicted2random.a2rclient.utils.Promise)
   */
  @Override
  public void opperationComplete(Promise<Result> promise) {
    this.promise = promise;

    V activity = activityReference.get();

    if (activity != null && !activity.isFinishing())
      activity.runOnUiThread(this);
  }

  /**
   * Get the promise.
   * 
   * @return
   */
  public Promise<Result> getPromise() {
    return promise;
  }

  /**
   * Get the activity.
   * 
   * @return
   */
  public V getActivity() {
    return activityReference.get();
  }

}
