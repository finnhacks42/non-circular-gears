package gridfeatures;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/*** This class represents crime data in a format that makes it efficient to calcuate counts across a range of different variables. ***/
public class Data {
	
	private Map<CrimeKey,TreeMap<Integer,Integer>> counts = new HashMap<CrimeKey, TreeMap<Integer,Integer>>();
	private int numPeriods;
	private List<Integer> areas;
	private Map<String,Set<String>> categoryNameToLevels;
	//private Map<Integer, List<Integer>> areaNetwork;
	private LocationHierachy heirachy;
	

	public Data(Map<CrimeKey,TreeMap<Integer,Integer>> counts, int numPeriods, List<Integer> areas, Map<String, Set<String>> categoryNameToLevels, LocationHierachy hierachy){
		this.counts = counts;
		this.numPeriods = numPeriods;
		this.areas = areas;
		this.categoryNameToLevels = categoryNameToLevels;
		this.heirachy = hierachy;
		
		// create a very simple network where each area only has itself as a neighbour.
//		areaNetwork = new HashMap<Integer,List<Integer>>();
//		for (int id:areas) {
//			ArrayList<Integer> neighbours = new ArrayList<Integer>();
//			neighbours.add(id);
//			areaNetwork.put(id, neighbours);
//		}
	}
	
	
	/*** calculate crime counts for the category and level (ie type:burglary or premsis:house) over the specified number of days back.
	 * 
	 * @param category the name of the category
	 * @param catlevel the level of that category to query
	 * @param areaID the areaID to query for
	 * @param day the day to query for
	 * @param daysback an array of the number of days to aggregate back over from the current day
	 * @return an array of length daysback with the corresponding aggregated counts.
	 */
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
	
	
	/*** as for calculateCounts but normalizes by dividing by the number of days in the period to go back. ***/
	public float[] calculateNormalizedCounts(CrimeKey key, int day, int[] daysback) {
		int[] counts = calculateCounts(key, day, daysback);
		float[] result = new float[counts.length];
		for (int i = 0; i < counts.length; i ++) {
			result[i] = (float)counts[i]/daysback[i];
		}
		return result;
	}
	
	
	/*** get the count of crime under the specified key on the specified day. ***/
	public int getCount(CrimeKey key, int day) {
		TreeMap<Integer,Integer> events = counts.get(key);
		if (events == null) {return 0;}
		Integer count = events.get(day);
		if (count == null) {return 0;}
		return count;
	}
	
	/*** get the number of temporal periods in the data. ***/
	public int getNumPeroids(){
		return numPeriods;
	}
	
	/*** get all the areas in which crime could occur in the data. ***/
	public List<Integer> getAreas(){
		return Collections.unmodifiableList(areas);
	}
	
	/*** return a list of the names of all the categories the data is broken down by. ***/
	public Collection<String> getCategories(){
		return categoryNameToLevels.keySet();
	}
	
	/*** return a list of all the levels for a specific cateogry. ***/
	public Set<String> getLevels(String category) {
		return categoryNameToLevels.get(category);
	}
	
	public LocationHierachy getHierachy() {
		return this.heirachy;
	}
	
	/*** return all the areas for which crime occurred in the data set ordered by distance to the specified one. 
	 * The specified area itself will be the first element in the list***/
//	public List<Integer> getAreasOrderedByDistance(int area) { 
//		return areaNetwork.get(area);
//	}
	
//	/*** returns and ordered list of the closest numAreas areas to the input area.
//	 * The input area itself will be the first element in the list. ***/
//	public List<Integer> getAreasOrderedByDistance(int area, int numAreas) {
//		List<Integer> all = getAreasOrderedByDistance(area);
//		if (all == null) {return null;}
//		else {
//			return getAreasOrderedByDistance(area).subList(0, numAreas);
//		}
//	}

}


