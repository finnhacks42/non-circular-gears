package gridfeatures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class Data {
	
	private Map<CrimeKey,TreeMap<Integer,Integer>> counts = new HashMap<CrimeKey, TreeMap<Integer,Integer>>();
	private int numPeriods;
	private List<Integer> areas;
	private Map<String,Set<String>> categoryNameToLevels;
	private Map<Integer, List<Integer>> areaNetwork;
	

	public Data(Map<CrimeKey,TreeMap<Integer,Integer>> counts, int numPeriods, List<Integer> areas, Map<String, Set<String>> categoryNameToLevels, Map<Integer,List<Float>> areaLats, Map<Integer,List<Float>> areaLons){
		this.counts = counts;
		this.numPeriods = numPeriods;
		this.areas = areas;
		this.categoryNameToLevels = categoryNameToLevels;
		
		// create a very simple network where each area only has itself as a neighbour.
		areaNetwork = new HashMap<Integer,List<Integer>>();
		for (int id:areas) {
			ArrayList<Integer> neighbours = new ArrayList<Integer>();
			neighbours.add(id);
			areaNetwork.put(id, neighbours);
		}
	}
	
	
	/*** Creates a network of the distances between areas under the assumption that their IDs represent cellIDs.
	 * The cells area assumed to begin at 0 in the top left corner and increase left to right and then top-bottom.
	 * @param gridWidth the number of squares the grid is wide.
	 */
	public void createAreaNetworkFromGrid( int gridWidth) {
		Map<Integer,List<Distance>> result = new HashMap<Integer,List<Distance>>();
		for (int cellID: areas) {
			List<Distance> distances = new ArrayList<Distance>();
			for (int cellID2: areas) {
				int i = cellID % gridWidth;
				int j = cellID/gridWidth;
				int i2 = cellID2 % gridWidth;
				int j2 = cellID2/gridWidth;
				double dist = Math.sqrt(Math.pow(i - i2, 2)+Math.pow(j - j2,2));
				distances.add(new Distance(cellID2, dist));
			}
			Collections.sort(distances);
			result.put(cellID, distances);
		}
		buildNetwork(result);
	}

	/*** Creates a network of the distances between areas based on the centroids (median lat/long) of the crime events recorded in that area.
	 * This method will not include areas with 0 crime in the network because no centroid can be found. ***/
	public void createAreaNetworkFromAreaCentroids(Map<Integer, GeoPoint> centroids){
		Map<Integer,List<Distance>> result = new HashMap<Integer,List<Distance>>();

		for (Entry<Integer,GeoPoint> area: centroids.entrySet()) {
			List<Distance> distances = new ArrayList<Distance>();
			for (Entry<Integer,GeoPoint> otherarea: centroids.entrySet()) {
				if (area.getKey() == otherarea.getKey()) {
					distances.add(new Distance(area.getKey(),0));
				} else {
					double distance = area.getValue().distance(otherarea.getValue());
					distances.add(new Distance(otherarea.getKey(), distance));
				}
			}
			Collections.sort(distances);
			result.put(area.getKey(), distances);	
		}
		buildNetwork(result);
		
	}
	
	/*** Takes a map from the areaID to an ordered list of distances and creates the map from areaID to list of IDs ***/
	private void buildNetwork(Map<Integer,List<Distance>> input) {
		this.areaNetwork = new HashMap<Integer, List<Integer>>();
		for (Entry<Integer,List<Distance>> entry: input.entrySet()) {
			List<Integer> otherAreas = new ArrayList<Integer>();
			for (Distance d: entry.getValue()) {
				otherAreas.add(d.getArea());
			}
			areaNetwork.put(entry.getKey(), otherAreas);
		}
	}


	/*** calculate crime counts for the category and level (ie type:burglary or premsis:house).
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
	
	/*** get the number of temporal peroids in the data. ***/
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
	
	/*** return all the areas for which crime occurred in the data set ordered by distance to the specified one. 
	 * The specified area itself will be the first element in the list***/
	public List<Integer> getAreasOrderedByDistance(int area) { 
		return areaNetwork.get(area);
	}
	
	/*** returns and ordered list of the closest numAreas areas to the input area.
	 * The input area itself will be the first element in the list. ***/
	public List<Integer> getAreasOrderedByDistance(int area, int numAreas) {
		List<Integer> all = getAreasOrderedByDistance(area);
		if (all == null) {return null;}
		else {
			return getAreasOrderedByDistance(area).subList(0, numAreas);
		}
	}

}


