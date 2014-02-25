package gridfeatures;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface DataI {

	/*** record that a crime with specified key occured within the specified period. Note each event will normally correpond to multiple keys so will be recorded multiple times***/
	public abstract void incrementCount(CrimeKey key, int period);
	
	/*** calculate crime counts for the category and level (ie type:burglary or premsis:house) over the specified number of days back.
	 * 
	 * @param category the name of the category
	 * @param catlevel the level of that category to query
	 * @param areaID the areaID to query for
	 * @param day the day to query for
	 * @param daysback an array of the number of days to aggregate back over from the current day
	 * @return an array of length daysback with the corresponding aggregated counts.
	 */
	public abstract int[] calculateCounts(CrimeKey key, int day, int[] daysback);

	/*** as for calculateCounts but normalizes by dividing by the number of days in the period to go back. ***/
	public abstract float[] calculateNormalizedCounts(CrimeKey key, int day, int[] daysback);

	/*** get the count of crime under the specified key on the specified day. ***/
	public abstract int getCount(CrimeKey key, int day);

	/*** get the number of temporal periods in the data. ***/
	public abstract int getNumPeroids();

	/*** get all the areas in which crime could occur in the data. ***/
	public abstract List<Integer> getAreas();

	/*** return a list of the names of all the categories the data is broken down by. ***/
	public abstract Collection<String> getCategories();

	/*** return a list of all the levels for a specific cateogry. These may be modified. ***/
	public abstract Set<String> getLevels(String category);
	
	/*** Set the levels for a given category. If the category does not already exist it will be created. Should be set before any counts are calculated. ***/
	public abstract void setLevels(String category, Set<String> levels);

	public abstract LocationHierachy getHierachy();
	
	/*** set the areas in which crime could possibly occur. Should be set once, before any counts are calculated. ***/
	public abstract void setAreas(List<Integer> areas);
	
	/*** set the number of the last period in the data set. (The first period is assumed to be 0. Should be set once, before any counts are calculated.***/
	public abstract void setNumPeriods(int numPeriods);
	
	/*** can be called to validate the current store is in a valid state to start returning counts. 
	 * @throws InvalidDataStoreException ***/
	public abstract void validate() throws InvalidDataStoreException;
	
	public void checkConsistency() throws InvalidDataStoreException;
	
	/*** set the target variable for a given area and period. ***/
	public void incrementTarget(int area, int period);
		
	/*** get the target variable for a given area and period. ***/
	public int getTarget(int area, int period);
	
	/*** get the sum of the targets across all areas and periods. ***/
	public int getTargetTotal();

}