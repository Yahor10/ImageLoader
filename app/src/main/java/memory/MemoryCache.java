package memory;

public interface MemoryCache <K,E> {

	
	boolean put(K key, E value);
	
	E get(K key);
	
	E remove(K key);
	
	void clear();
	
	int sizeOf(K key, E value) ;

	long size();

}
