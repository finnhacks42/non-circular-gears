package gridfeatures;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*** This class represents crime data in a format that makes it efficient to calculate counts across a range of different variables. ***/
public abstract class DataA implements DataI {
	
	private int maxPeriod;
	private Map<String,Set<String>> categoryNameToLevels = new HashMap<String,Set<String>>();
	private LocationHierachy heirachy;
	private Map<IntTuple, Integer> targets = new HashMap<IntTuple,Integer>();
	
	public DataA() {};

	public DataA(int numPeriods, List<Integer> areas){
		this.maxPeriod = numPeriods;
	}
	
	@Override
	public int getTargetTotal() {
		int total = 0;
		for (int t: targets.values()) {
			total += t;
		}
		return total;
	}
	
	@Override
	public int getTarget(int area, int period) {
		IntTuple tpl = new IntTuple(area, period);
		Integer count = targets.get(tpl);
		if (count == null) {
			return 0;
		}
		return count;
	}
	
	@Override
	public void incrementTarget(int area, int period) {
		IntTuple tpl = new IntTuple(area, period);
		Integer count = targets.get(tpl);
		if (count == null) {
			targets.put(tpl,1);
		} else {
			targets.put(tpl, count + 1);
		}
	}
	
	static class IntTuple {
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IntTuple other = (IntTuple) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}
		private int x;
		private int y;
		public IntTuple(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
	}
	
	public void checkConsistency() throws InvalidDataStoreException {
		//what invariants do I expect to hold ...
						
		//The same events are grouped into different categories and regions - so over any given period, the totals should be the same.	
		for (int period = 0; period <= maxPeriod; period ++) {
			int prev = -1;
			//get the total for over all the level of a given category
			for (String category: getCategories()) {
				for (String areaCat: getHierachy().getNameSpaces()) {
					int total = 0;
					for (int areaID: getHierachy().getAreaIDs(areaCat)) {
						for (String level: getLevels(category)) {
							CrimeKey key = new CrimeKey(areaCat, areaID, category, level);
							int count = getCount(key, period);
							total += count;
						}
					}
					if (prev < 0 ){prev = total;}
					else {
						if (total != prev) {
							String message = category+","+areaCat+","+period+","+prev+","+total;
							throw new InvalidDataStoreException(message);
						}
					}
				}
			}
		}
	}
	
	@Override
	public abstract void incrementCount(CrimeKey key, int period);
	
	
	@Override
	public abstract int[] calculateCounts(CrimeKey key, int day, int[] daysback);
		
	
	@Override
	public abstract float[] calculateNormalizedCounts(CrimeKey key, int day, int[] daysback);
	
	
	@Override
	public abstract int getCount(CrimeKey key, int day);
	
	
	@Override
	public int getNumPeroids(){
		return maxPeriod;
	}
	
	
	
	@Override
	public Collection<String> getCategories(){
		return categoryNameToLevels.keySet();
	}
	

	@Override
	public Set<String> getLevels(String category) {
		return categoryNameToLevels.get(category);
	}
	
	@Override
	public void setLevels(String category, Set<String> levels) {
		categoryNameToLevels.put(category, levels);
	}
	
	@Override
	public void setHierachy(LocationHierachy hierachy) {
		this.heirachy = hierachy;
		
	}
	
	@Override
	public LocationHierachy getHierachy() {
		return this.heirachy;
	}


	@Override
	public void setNumPeriods(int numPeriods) {
		this.maxPeriod = numPeriods;
		
	}
	
	@Override
	public void validate() throws InvalidDataStoreException {
		
		if (this.maxPeriod <= 0) {
			throw new InvalidDataStoreException("The number of periods for the data must be set and > 0");
		}
		
		if (this.categoryNameToLevels.size() == 0 ){
			throw new InvalidDataStoreException("At least one category must be set");
		}
	}
	
}


