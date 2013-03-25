package eu.addicted2random.a2rclient.net;

import java.net.URI;
import java.util.List;

import eu.addicted2random.a2rclient.grid.Layout;
import eu.addicted2random.a2rclient.jam.Jam;
import eu.addicted2random.a2rclient.utils.Promise;

/**
 * Connection handler interface.
 * 
 * Interface to interact with connections.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public interface ConnectionHandler {

  /**
   * Open {@link ConnectionHandler} and return open promise.
   * 
   * The returned open promise should be the same instance for each call to
   * open.
   * 
   * @return
   */
  public Promise<ConnectionHandler> open();

  /**
   * Close {@link ConnectionHandler} and return close promise.
   * 
   * The returned open promise should be the same instance for each call to
   * open.
   * 
   * @return
   * @throws IllegalStateException
   */
  public Promise<ConnectionHandler> close();

  public Promise<ConnectionHandler> getOpenPromise();

  public Promise<ConnectionHandler> getClosePromise();

  /**
   * Returns true if {@link ConnectionHandler} is open otherwise false.
   * 
   * @return
   */
  public boolean isOpen();

  /**
   * Return true if {@link ConnectionHandler} is closed otherwise false.
   * 
   * @return
   */
  public boolean isClosed();

  /**
   * Returns true if the {@link ConnectionHandler} supports jams.
   * 
   * @return
   */
  public boolean hasJams();

  /**
   * Returns true if the {@link ConnectionHandler} supports layouts.
   * 
   * @return
   */
  public boolean hasLayouts();

  /**
   * Returns a promise for a {@link List} of {@link Jam}s or null if this
   * {@link ConnectionHandler} supports no jam selection.
   * 
   * @return
   * @throws IllegalStateException
   */
  public Promise<List<Jam>> getJams();

  /**
   * Returns a promise for a {@link List} of {@link Layout}s or null if this
   * {@link ConnectionHandler} supports no layout selection.
   * 
   * @return
   */
  public Promise<List<Layout>> getLayouts();

  /**
   * Return a promise for a {@link Layout}.
   * 
   * @return
   * @throws IllegalStateException
   */
  public Promise<Layout> getLayout();

  /**
   * Set selected {@link Jam}.
   * 
   * @param jam
   */
  public void setSelectedJam(Jam jam);

  /**
   * Return selected layout
   * 
   * @return
   */
  public Jam getSelectedJam();

  /**
   * Start a session.
   * 
   * @return
   */
  public Promise<Boolean> join();

  /**
   * Leave a session.
   */
  public void leave();

  /**
   * Set selected {@link Layout}. Set selected layout to resolve by
   * {@link ConnectionHandler#getLayout()}.
   * 
   * @param jam
   */
  public void setSelectedLayout(Layout layout);

  /**
   * Get selected layout.
   * 
   * @return
   */
  public Layout getSelectedLayout();

  /**
   * @return the URI
   */
  public URI getUri();

}