package gridfeatures;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/*** A simple and inefficient implementation of DataInterface, intended to be used for testing. ***/
public class SimpleData extends DataA {
	private HashMap<CrimeKey, TreeMap<Integer,Integer>> counts = new HashMap<CrimeKey, TreeMap<Integer,Integer>>(); 
	
	public SimpleData(int numPeriods, List<Integer> areas){
		super(numPeriods,areas);
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
	
	@Override
	public int[] calculateCounts(CrimeKey key, int day, int[] daysback) {
		int[] result = new int[daysback.length];
		int indx  = 0;
		for (int dayback: daysback) {
			int count = 0;
			for (int back = 1; back <= dayback; back ++) {
				count += getCount(key, day - back);
			}
			result[indx] = count;
			indx ++;
		}
		return result;
	}

	@Override
	public float[] calculateNormalizedCounts(CrimeKey key, int day, int[] daysback) {
		int[] counts = calculateCounts(key, day, daysback);
		float[] result = new float[counts.length];
		for (int i = 0; i < counts.length; i ++) {
			result[i]  = counts[i]/(float)daysback[i];
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


	

	

}
