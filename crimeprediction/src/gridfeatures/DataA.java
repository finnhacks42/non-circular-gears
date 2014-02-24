package gridfeatures;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*** This class represents crime data in a format that makes it efficient to calcuate counts across a range of different variables. ***/
public abstract class DataA implements DataI {
	
	private int numPeriods;
	private List<Integer> areas;
	private Map<String,Set<String>> categoryNameToLevels = new HashMap<String,Set<String>>();
	private LocationHierachy heirachy = new LocationHierachy();
	
	public DataA() {};

	public DataA(int numPeriods, List<Integer> areas){
		this.numPeriods = numPeriods;
		this.areas = areas;
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
		return numPeriods;
	}
	
	@Override
	public List<Integer> getAreas(){
		return areas;
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
	public LocationHierachy getHierachy() {
		return this.heirachy;
	}


	@Override
	public void setAreas(List<Integer> areas) {
		this.areas = areas;
	}



	@Override
	public void setNumPeriods(int numPeriods) {
		this.numPeriods = numPeriods;
		
	}
	
	@Override
	public void validate() throws InvalidDataStoreException {
		if (areas == null) {
			throw new InvalidDataStoreException("Possible areas in which crime could occur must be specified");
		}
		
		if (this.numPeriods <= 0) {
			throw new InvalidDataStoreException("The number of periods for the data must be set and > 0");
		}
		
		if (this.categoryNameToLevels.size() == 0 ){
			throw new InvalidDataStoreException("At least one category must be set");
		}
	}
	
}


