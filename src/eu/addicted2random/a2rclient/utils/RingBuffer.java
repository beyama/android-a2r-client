package eu.addicted2random.a2rclient.utils;

/**
 * 
 * A simple ring buffer.
 * 
 * @author Alexander Jentz, beyama.de
 *
 * @param <T>
 */
public class RingBuffer<T> {
	
	private final int capacity;

	private final Object[] buffer;
	
	private int start, end, length = 0;
	
	public RingBuffer(int capacity) {
		super();
		
		if(capacity < 1)
			throw new IllegalArgumentException("Capacitiy must be a non-negative greater than 1");
		
		this.capacity = capacity;
		this.buffer = new Object[this.capacity];
	}
	
	/**
	 * Add element to the end of the buffer.
	 * 
	 * @param elem
	 */
	public void add(T elem) {
		buffer[end] = elem;
		end = (end + 1) % capacity;
		
		if(length < capacity)
			length++;
		else
			start = (start + 1) % capacity;
	}
	
	/**
	 * Get value from position.
	 * 
	 * @param pos
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T get(int pos) {
		if (pos < length && pos >= 0)
      return (T) buffer[(start + pos) % capacity];
		return null;
	}
	
	public T last() {
		if(length > 0)
			return get(length-1);
		return null;
	}
	
	/**
	 * Returns the value that will be overridden on next add.
	 * 
	 * This is useful for recycling objects.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T getValueFromEndPointer() {
		return (T) buffer[end];
	}

	/**
	 * Get capacity.
	 * 
	 * @return
	 */
	public int capacity() {
		return capacity;
	}

	/**
	 * Get start index.
	 * 
	 * @return
	 */
	public int start() {
		return start;
	}

	/**
	 * Get end index.
	 * 
	 * @return
	 */
	public int end() {
		return end;
	}

	/**
	 * Get length.
	 * 
	 * @return
	 */
	public int length() {
		return length;
	}
	
}