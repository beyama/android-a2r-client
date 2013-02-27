package eu.addicted2random.a2rclient.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;

/**
 * A generic implementation of a {@link Future}.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 * @param <Result>
 *          The return value of the promise.
 */
public class Promise<Result> implements Future<Result> {

  private enum State {
    RUNNING, DONE, FAILED, CANCELED
  };

  private final ReentrantLock lock = new ReentrantLock();
  private final Condition fulfilled = lock.newCondition();

  private Result result;
  private Throwable failure;
  private List<PromiseListener<Result>> listeners;
  private State state = State.RUNNING;

  public Promise() {
    super();
  }

  protected void onFulfilled() {
    if (listeners != null) {
      for (PromiseListener<Result> listener : listeners) {
        try {
          listener.opperationComplete(this);
        } catch (Exception e) {
        }
      }
      listeners = null;
    }
  }

  protected void onSuccess() {
  }

  protected void onFailure() {
  }

  protected boolean onCancel(boolean mayInterruptIfRunning) {
    return true;
  }

  /**
   * Let the promise success.
   * 
   * @param result
   *          The value of the promise.
   */
  public void success(Result result) {
    if (result == null)
      throw new NullPointerException();

    lock.lock();

    try {
      if (isDone())
        throw new IllegalStateException("Promise is already fulfilled");

      this.result = result;
      state = State.DONE;
      onSuccess();
      onFulfilled();

      fulfilled.signalAll();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Let the promise fail.
   * 
   * @param throwable
   *          The cause of the failure.
   */
  public void failure(Throwable throwable) {
    if (throwable == null)
      throw new NullPointerException();

    lock.lock();

    try {
      if (isDone())
        throw new IllegalStateException("Promise is already fulfilled");

      this.failure = throwable;
      state = State.FAILED;
      onFailure();
      onFulfilled();

      fulfilled.signalAll();
    } finally {
      lock.unlock();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.concurrent.Future#cancel(boolean)
   */
  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    lock.lock();
    try {
      if (state == State.RUNNING) {
        if (onCancel(mayInterruptIfRunning)) {
          this.state = State.CANCELED;
          onFulfilled();
          return true;
        }
      }
      return false;
    } finally {
      lock.unlock();
    }
  }

  protected void lock() {
    lock.lock();
  }

  protected void unlock() {
    lock.unlock();
  }

  public boolean isLocked() {
    return lock.isLocked();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.concurrent.Future#isCancelled()
   */
  @Override
  public boolean isCancelled() {
    return state == State.CANCELED;
  }

  /**
   * Returns true if and only if this promise is complete, regardless of whether
   * the operation was successful, failed, or cancelled.
   */
  @Override
  public boolean isDone() {
    return state != State.RUNNING;
  }

  /**
   * Returns true if this promise was successfully fulfilled.
   * 
   * @return
   */
  public boolean isSuccess() {
    return state == State.DONE;
  }

  /**
   * Get the cause of the failure.
   * 
   * @return
   */
  public Throwable getCause() {
    return failure;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.concurrent.Future#get()
   */
  @Override
  public Result get() throws InterruptedException, ExecutionException {
    try {
      return get(0, null);
    } catch (TimeoutException e) {
      return null; // should never happen
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
   */
  @Override
  public Result get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    lock.lock();
    try {
      if (isDone()) {
        if (isSuccess())
          return result;
        else
          throw new ExecutionException(failure);
      }

      if (timeout == 0 && unit == null)
        fulfilled.await();
      else
        fulfilled.await(timeout, unit);

      if (!isSuccess()) {
        if (failure instanceof InterruptedException)
          throw (InterruptedException) failure;
        else if (failure instanceof ExecutionException)
          throw (ExecutionException) failure;
        else if (failure instanceof TimeoutException)
          throw (TimeoutException) failure;
        else
          throw new ExecutionException(failure);
      }
      return result;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Get current value of result without blocking.
   * 
   * @return
   */
  public Result getResult() {
    return result;
  }

  /**
   * Add a promise listener. The listener will be called immediately if the
   * promise is already fulfilled in this case the method will return false.
   * 
   * @param listener
   * @return Returns true if the listener was added to the list of listeners.
   */
  public boolean addListener(PromiseListener<Result> listener) {
    if (listener == null)
      throw new NullPointerException();

    lock.lock();
    try {
      if (isDone()) {
        listener.opperationComplete(this);
        return false;
      }

      if (listeners == null)
        listeners = new LinkedList<PromiseListener<Result>>();

      listeners.add(listener);
      return true;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Remove a promise listener.
   * 
   * All listeners are automatically removed after fulfill.
   * 
   * @param listener
   *          The listener to remove.
   * @return True if the listener was removed from the list of listeners.
   */
  public boolean removeListener(PromiseListener<Result> listener) {
    lock.lock();
    try {
      if (listeners == null)
        return false;
      return listeners.remove(listener);
    } finally {
      lock.unlock();
    }
  }
  
  /**
   * Register an {@link Activity} with a method name to call on fulfill.
   * 
   * This will register a new {@link MethodInvokingActivityPromiseListener} for you {@link Activity}.
   * 
   * @param activity The activity.
   * @param method The method to invoke on fulfill.
   * @return The result of {@link Promise#addListener(PromiseListener)}.
   */
  public boolean addActivityListener(Activity activity, String method) {
    try {
      MethodInvokingActivityPromiseListener<Result> listener = new MethodInvokingActivityPromiseListener<Result>(activity, method);
      return addListener(listener);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Remove an {@link Activity} registered by {@link Promise#addActivityListener(Activity, String)}.
   * @param activity The activity.
   * @return True if a listener was found.
   */
  public boolean removeActivityListener(Activity activity) {
    if(listeners == null) return false;
    
    lock.lock();
    try {
      boolean result = false;
      
      if(listeners != null) {
        Iterator<PromiseListener<Result>> iter = listeners.iterator();
        while(iter.hasNext()) {
          PromiseListener<Result> listener = iter.next();
          if(listener instanceof MethodInvokingActivityPromiseListener) {
            if(((MethodInvokingActivityPromiseListener<Result>)listener).getActivity() == activity) {
              iter.remove();
              result = true;
            }
              
          }
        }
      }
      return result;
    } finally {
      lock.unlock();
    }
  }

}
