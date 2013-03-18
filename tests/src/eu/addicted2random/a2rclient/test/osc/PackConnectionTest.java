package eu.addicted2random.a2rclient.test.osc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import eu.addicted2random.a2rclient.osc.PackConnection;
import eu.addicted2random.a2rclient.osc.PackSupport;
import eu.addicted2random.a2rclient.osc.Types;
import junit.framework.TestCase;

public class PackConnectionTest extends TestCase {

  public void testPackConnectionDeadLockBug() throws InterruptedException {
    ReentrantLock lock = new ReentrantLock();
    final PackSupport packA = new PackSupport(Types.INTEGER_TYPE, lock);
    final PackSupport packB = new PackSupport(Types.INTEGER_TYPE, lock);

    Map<Integer, Integer> map = new HashMap<Integer, Integer>(1);
    map.put(0, 0);

    new PackConnection(packA, packB, map, map);

    final Object actorA = new Object();
    final Object actorB = new Object();

    Thread threadA = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
        }
        for (int i = 0; i < 1000; i++) {
          packA.lock(actorA);
          packA.set(0, i);
          packA.unlock();
        }
      }
    });

    Thread threadB = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
        }
        for (int i = 0; i < 1000; i++) {
          packB.lock(actorB);
          packB.set(0, i);
          packB.unlock();
        }
      }
    });

    threadA.start();
    threadB.start();

    threadA.join();
    threadB.join();

  }

}
