package utilities;

import java.util.Collection;
import java.util.HashMap;

/*** This class is used to keep track of the number occurrences for each of a number of keys. ***/
public class Counter<T> {
	private HashMap<T,MutInt> counts = new HashMap<T,MutInt>();
	
	public void increment(T key) {
		increment(key,1);
	}
	
	public void increment(T key, int value) {
		MutInt count = counts.get(key);
		if (count == null) {
			counts.put(key, new MutInt(value)); 
		} else {
			count.increment(value);
		}
	}
	
	public Collection<T> getKeys() {
		return counts.keySet();
	}
	
	/*** Returns null if they key is not in the counter. ***/
	public Integer getCount(T key) {
		MutInt val = counts.get(key);
		if (val == null) {return null;}
		return val.get();
	}
	
	public boolean containsKey(T key) {
		return counts.containsKey(key);
	}
	
	static class MutInt {
		private int value;
		
		public MutInt(int value) {
			this.value = value;
		}
		public void set(int value) {
			this.value = value;
		}
		public int get() {
			return this.value;
		}
		
		public void increment(int inc) {
			this.value = value + inc;
		}
	}
	

}
