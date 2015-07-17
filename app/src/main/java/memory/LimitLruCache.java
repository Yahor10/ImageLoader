package memory;

import java.util.LinkedHashMap;
import java.util.Map;

public class LimitLruCache<K, V> implements MemoryCache<K, V> {

	private long size;
	private final long maxSize;

	private final LinkedHashMap<K, V> contaner;

	public LimitLruCache(int limit) {
		maxSize = limit;
		size = 0;

		contaner = new LinkedHashMap<K, V>();

	}

	@Override
	public boolean put(K key, V value) {
	      if (key == null || value == null) {
	            throw new NullPointerException("key == null || value == null");
	        }

	        V previous;
	        synchronized (this) {	            
	            size += sizeOf(key, value);
	            previous = contaner.put(key, value);
	            if (previous != null) {
	                size -= sizeOf(key, previous);
	            }
	        }

	        trimToSize(maxSize);
	        return previous != null;	    
	}

	@Override
	public V get(K key) {
		if (key == null) {
			throw new NullPointerException(key.getClass().getSimpleName()
					+ " key == null ");
		}

		V val;
		synchronized (this) {
			val = contaner.get(key);
			if (val != null) {
				return val;
			}
		}
		return val;
	}

	@Override
	public V remove(K key) {
		if (key == null) {
			throw new NullPointerException(key.getClass().getSimpleName()
					+ " key == null ");
		}
		
		V previous;
		synchronized (this) {
			previous = contaner.remove(key);
			if (previous != null) {
				size -= sizeOf(key, previous);
				return previous;
			}
		}
		
		return null;
	}

	@Override
	public void clear() {
		contaner.clear();
		size = 0;
	}

	@Override
	public int sizeOf(K key, V value) {
		// TODO Auto-generated method stub
		throw new IllegalArgumentException("not implemented");		
	}

	private void trimToSize(long maxSize) {
		while (true) {
			K key;
			V value;
			synchronized (this) {
				if (size < 0 || (contaner.isEmpty() && size != 0)) {
					throw new IllegalStateException(getClass().getName()
							+ ".sizeOf() is reporting inconsistent results!");
				}

				if (size <= maxSize || contaner.isEmpty()) {
					break;
				}

				Map.Entry<K, V> toEvict = contaner.entrySet().iterator().next();
				key = toEvict.getKey();
				value = toEvict.getValue();
				contaner.remove(key);
				size -= sizeOf(key, value);
			}
		}
	}

}
