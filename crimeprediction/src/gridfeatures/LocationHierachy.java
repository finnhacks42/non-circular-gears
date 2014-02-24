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
	private Map<String,Set<Integer>> namespaces = new HashMap<String,Set<Integer>>();
	
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
	
	public Collection<String> getNameSpaces() {
		return namespaces.keySet();
	}
	
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
