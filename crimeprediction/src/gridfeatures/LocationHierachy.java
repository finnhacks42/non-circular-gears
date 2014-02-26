package gridfeatures;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import utilities.Counter;

/*** maps target locations to parents in a number of different namespaces. 
 * For example the target area could be a 200m grid, and each cell would have parents in the 1000m grid namespace and the reportingarea namespace.
 * @author finn
 *
 */
public class LocationHierachy {
	
	private Map<LocationKey,Integer> hierachy = new HashMap<LocationKey,Integer>();
	private Map<String,Set<Integer>> namespaces = new HashMap<String,Set<Integer>>(); // maps from each namespace to a list of the elements in that namespace.
	
	/*** Creates a location hierachy by assuming that the ids of the areas specify what their parents are
	 * For example if the ids are 123, 145, 235 and 2 namespaces are requested, then it is assumed that 123 has parent 1 in 0th NS and parent 2 in 1th namespace.
	 * @param areas
	 * @param numNamespaces
	 * @return
	 */
	public static LocationHierachy buildFromAreaDigits(int[] areas, int numNamespaces) {
		LocationHierachy hierachy = new LocationHierachy();
		for (int i : areas) {
			String s = String.valueOf(i);
			hierachy.add(i, i, DataLoader.AREA);
			for (int n = 0; n < numNamespaces; n ++) {
				if (s.length() < n) {
					throw new IllegalArgumentException("All areasIds must be at least the required number of namespaces long");
				}
				int parentID = Integer.valueOf(s.substring(n, n+1));
				String parentNS = DataLoader.AREA_PREFIX+n+"th";
				hierachy.add(i, parentID, parentNS);
			}
		}
		
		return hierachy;
	}
	
	/*** Add a target area, specifying its parent in the given namespace. Calling this function multiple times with the same input will not change the results.
	 * @param targetArea the new primary area to add.
	 * @param the containing area of this targetArea in the given namespace
	 * @param the namespace/level of aggregation of the parent area ID.***/
	public void add(int targetArea, int parentArea, String parentNamespace) {
		LocationKey key = new LocationKey(targetArea, parentNamespace);
		hierachy.put(key, parentArea);
		
		Set<Integer> parentAreaValues = namespaces.get(parentNamespace);
		if (parentAreaValues == null) {
			parentAreaValues = new HashSet<Integer>();
			namespaces.put(parentNamespace,parentAreaValues);
		}
		parentAreaValues.add(parentArea);
	}
	
	/*** return the number of namespaces in the hierachy. ***/
	public int depth(){
		return namespaces.keySet().size();
	}
	
	/*** returns all the namespaces in the hierachy ***/
	public Collection<String> getNameSpaces() {
		return namespaces.keySet();
	}
	
	/*** Returns the number of areas in the given namespace. ***/
	public int size(String namespace) {
		return getAreaIDs(namespace).size();
	}
	
	public Collection<Integer> getAreaIDs(String namespace) {
		return namespaces.get(namespace);
	}
	
	/*** 
	 * 
	 * @param parentNamespace
	 * @param targetArea
	 * @return either the parent in the specified namespace or null if there is no record.
	 */
	public Integer getTargetAreaParent(String parentNamespace, int targetArea) {
		LocationKey key = new LocationKey(targetArea, parentNamespace);
		return hierachy.get(key);
	}
	
	static class LocationKey {
		private String combination;
		
		public LocationKey(int targetArea, String parentNamespace) {
			this.combination = targetArea +"_"+parentNamespace;
		}
		
		@Override
		public int hashCode() {
			return combination.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LocationKey other = (LocationKey) obj;
			if (combination == null) {
				if (other.combination != null)
					return false;
			} else if (!combination.equals(other.combination))
				return false;
			return true;
		}
	}

}
