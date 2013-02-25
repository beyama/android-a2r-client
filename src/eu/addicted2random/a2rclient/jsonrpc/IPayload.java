package eu.addicted2random.a2rclient.jsonrpc;

import java.util.List;
import java.util.Map;

/**
 * Base interface for all RPC messages and Errors.
 * 
 * Stores a payload object like {@link Response} result or error and 
 * has convenience methods to work with indexed or named
 * payload.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public interface IPayload extends JSONSerializable {

  /**
   * Do we have payload data?
   * 
   * @return
   */
  public abstract boolean hasPayload();

  /**
   * Is payload a {@link List}?
   * 
   * A list payload must be a {@link List} of {@link Object}s.
   * 
   * @return
   */
  public abstract boolean isList();

  /**
   * Is payload a {@link Map}.
   * 
   * A map payload must be a {@link Map} of {@link String} {@link Object} pairs.
   * 
   * @return
   */
  public abstract boolean isMap();

  /**
   * Get the map payload.
   * 
   * This returns null if payload isn't a {@link Map}.
   * 
   * @return
   */
  public abstract Map<String, Object> getMap();

  /**
   * Get the list payload.
   * 
   * This returns null if payload isn't a {@link List}.
   * 
   * @return
   */
  public abstract List<Object> getList();

  /**
   * Get the payload object or null.
   * 
   * @return
   */
  public abstract Object getPayload();

}