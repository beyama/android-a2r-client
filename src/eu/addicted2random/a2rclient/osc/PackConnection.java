package eu.addicted2random.a2rclient.osc;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Connect two Packs and sync their values.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public class PackConnection implements Pack.PackListener {

  private final Pack source;
  private final Pack target;
  // TODO: use SparseIntArray
  private final Map<Integer, Integer> fromToIndices; 
  private final Map<Integer, Integer> toFromIndices;
  
  /**
   * Create a new {@link PackConnection} that synchronizes values from source to target pack.
   * If toFromIndices isn't null this will synchronize in both directions.
   * 
   * TODO: boundary and compatibility checks.
   * 
   * @param source
   * @param target
   * @param fromToIndices
   * @param toFromIndices
   */
  public PackConnection(Pack source, Pack target, Map<Integer, Integer> fromToIndices, Map<Integer, Integer> toFromIndices) {
    super();
    this.source = source;
    this.target = target;
    this.fromToIndices = fromToIndices;
    this.toFromIndices = toFromIndices;
    
    this.source.addPackListener(this);
    
    if(toFromIndices != null)
      this.target.addPackListener(this);
    
    // copy values
    this.target.lock(this);
    
    for(Entry<Integer, Integer> e : this.fromToIndices.entrySet()) {
      Object value = this.source.get(e.getKey());
      
      if(value != null) {
        // TODO: scale ranges
        this.target.set(e.getValue(), value);
      }
    }
    
    this.target.unlock();
  }
  
  /**
   * Create a new {@link PackConnection} that maps value changes from source to values
   * in target.
   * 
   * @param source
   * @param target
   * @param fromToIndices
   */
  public PackConnection(Pack source, Pack target, Map<Integer, Integer> fromToIndices) {
    this(source, target, fromToIndices, null);
  }

  @Override
  public void onValueChanged(Pack source, Object actor, int index, Object oldValue, Object newValue) {
    // TODO: scale ranges
    if(this.source == source) {
      if(fromToIndices.containsKey(index)) {
        int targetIndex = fromToIndices.get(index);
        
        this.target.lock(this);
        this.target.set(targetIndex, newValue);
        this.target.unlock();
      }
    } else if(toFromIndices != null && this.target == source) {
      if(toFromIndices.containsKey(index)) {
        int sourceIndex = toFromIndices.get(index);
        
        this.source.lock(this);
        this.source.set(sourceIndex, newValue);
        this.source.unlock();
      }
    }
  }

  @Override
  public void onPacked(Pack source) {
  }
  
  /**
   * Unregister this from source and target.
   */
  public void dispose() {
    source.removePackListener(this);
    if(toFromIndices != null)
      target.removePackListener(this);
  }
  
}
