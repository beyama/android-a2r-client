package eu.addicted2random.a2rclient.grid;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;



/**
 * Element id to view id map.
 * 
 * @author Alexander Jentz
 *
 */
public class IdMap implements Serializable {
  private static final long serialVersionUID = 4487470469954353597L;
  
  private int seq = 65536;
  private final Map<String, Integer> map = new HashMap<String, Integer>();

  /**
   * Get a view id for a {@link Element} id.
   * 
   * @param stringId The element id
   * @return The view id
   */
  public int getId(String stringId) {
    if(map.containsKey(stringId))
      return map.get(stringId);
    int id = seq++;
    map.put(stringId, id);
    return id;
  }
  
}