package gridfeatures;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/*** This class represents crime data in a format that makes it efficient to calcuate counts across a range of different variables. ***/
public class Data extends DataA {
	
	private Map<CrimeKey,TreeMap<Integer,Integer>> counts = new HashMap<CrimeKey, TreeMap<Integer,Integer>>();
	
	
	@Override
	public int[] calculateCounts(CrimeKey key, int day, int[] daysback) {
		int[] result = new int[daysback.length];

		TreeMap<Integer,Integer> events = counts.get(key);
		if (events == null) {return result;} // return all 0's as there are no events for this area, category, level combination anywhere/anytime

		int indx = 0;
		int sum = 0;
		int end = day;
		for (int daysEalier: daysback) {
			int start = day - daysEalier;
			Collection<Integer> crimesInPeroid = events.subMap(start, true, end, false).values();
			for (int numCrimes: crimesInPeroid) {
				sum +=numCrimes;
			}
			result[indx] = sum;
			end = start;
			indx ++;
		}
		return result;
	}
	
	
	@Override
	public float[] calculateNormalizedCounts(CrimeKey key, int day, int[] daysback) {
		int[] counts = calculateCounts(key, day, daysback);
		float[] result = new float[counts.length];
		for (int i = 0; i < counts.length; i ++) {
			result[i] = (float)counts[i]/daysback[i];
		}
		return result;
	}
	
	

	@Override
	public int getCount(CrimeKey key, int day) {
		TreeMap<Integer,Integer> events = counts.get(key);
		if (events == null) {return 0;}
		Integer count = events.get(day);
		if (count == null) {return 0;}
		return count;
	}
	
	
	
	public void incrementCount(CrimeKey key, int period) {
		TreeMap<Integer,Integer> periodToEventCount = counts.get(key);
		if (periodToEventCount == null) {
			periodToEventCount = new TreeMap<Integer,Integer>();
			periodToEventCount.put(period,1);
		} else {
			Integer current = periodToEventCount.get(period);
			if (current == null) {
				periodToEventCount.put(period, 1);
			}
			else {
				periodToEventCount.put(period, current+1);
			}
		}
		counts.put(key, periodToEventCount);
	}
	
	

}


