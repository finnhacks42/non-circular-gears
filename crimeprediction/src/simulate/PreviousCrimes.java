package simulate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*** This object is able to store what days previous crime occurred at each location. ***/
public class PreviousCrimes {
	private Map<Integer, ArrayList<Integer>> prevCrime = new HashMap<Integer,ArrayList<Integer>>();
	
	/*** returns a list of the days on which crime occurred for this day. ***/
	public List<Integer> getPreviousCrimes(int location) {
		return prevCrime.get(location);
	}
	
	/*** record that a crime occurred on the given day at the given location. ***/
	public void addCrime(int day, int location) {
		ArrayList<Integer> days = prevCrime.get(location);
		if (days == null) {
			days = new ArrayList<Integer>();
		}
		days.add(day);
		prevCrime.put(location, days);
	}

}
