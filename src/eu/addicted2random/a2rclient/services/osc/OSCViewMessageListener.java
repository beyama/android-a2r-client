package eu.addicted2random.a2rclient.services.osc;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.view.View;

import com.illposed.osc.OSCMessage;

/**
 * An OSC message listener that queues messages and posts itself to the view
 * if new messages are available.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public abstract class OSCViewMessageListener implements OSCMessageListener, Runnable {

  private final Queue<OSCMessage> queue = new ConcurrentLinkedQueue<OSCMessage>();

  private final View view;
  
  /**
   * Constructor to construct a new {@link OSCViewMessageListener} for
   * the supplied view.
   * 
   * @param view
   */
  public OSCViewMessageListener(View view) {
    super();
    this.view = view;
  }
  
  /**
   * Get the message queue.
   * 
   * @return
   */
  public Queue<OSCMessage> getQueue() {
    return queue;
  }
  
  public Iterator<OSCMessage> iterator() {
    return queue.iterator();
  }

  /**
   * Queue message and post this to view.
   */
  @Override
  public void onOSCMessage(OSCMessage message) {
    queue.add(message);
    view.post(this);
  }

}
