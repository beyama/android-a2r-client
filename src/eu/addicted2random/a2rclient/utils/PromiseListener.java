package eu.addicted2random.a2rclient.utils;

/**
 * Listener to listen for fulfilment of a {@link Promise}.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 * @param <V> Result type of the {@link Promise}.
 */
public interface PromiseListener<V> {
  void opperationComplete(Promise<V> result);
}
