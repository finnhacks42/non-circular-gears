package gridfeatures;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import utilities.Counter;
//TODO extend to allow multiple group labels in terms of area. 


/*** This class loads crime data in a format where there is one row per crime, and transforms it into features.
 *  The file must be | separated and the first line is assumed to consist of a header containing column names
 *  lat, lon, period and area are compulsory.
 *  There can be any number of additional area fields which should be named with names beginning with area
 *  Any other fields will assumed to represent categories.
 *  
 *  The latitude and longitude columns must contain decimals, the period and areaID column must be an integer starting from 0, and the category columns can be strings
 *  Counts will be produced separately for each category column. 
 *  
 ***/
public class DataLoader {
	private static final String AREA_PREFIX = "area";
	private static final String LAT = "lat";
	private static final String LON = "lon";
	private static final String PERIOD = "period";
	public static final String AREA = "area"; //the target will be grouped by this area.
	private static final List<String> REQUIRED = Arrays.asList(new String [] {LAT,LON,PERIOD,AREA});
	
	private int numCategories;
	private Map<String,Integer> categoryNameToIndex = new HashMap<String,Integer>(); //maps from the name of a category column to its column index
	private Map<CrimeKey,TreeMap<Integer,Integer>> counts = new HashMap<CrimeKey, TreeMap<Integer,Integer>>(); // maps (area, category, category-level) to a sorted map from period to num-crimes
	private List<Integer> areaList = new ArrayList<Integer>(); // a list of all the area IDs under consideration in the study. Not all may actually contain crime and thus appear in the data file.
	private Counter<Integer> periodCounts = new Counter<Integer>();
	private int numDays; //the number of periods, here a day may be a day or a watch, etc
	private Map<String, Integer> nameToIndx = new HashMap<String,Integer>(); //maps column names to column indx.
	private Map<String,Integer> areaToIndx = new HashMap<String,Integer>(); //maps additional area columns to their indexes.

	
	
	
	
	private void incrementCount(CrimeKey key, int period) {
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
	
	private List<Integer> readAreaFile(String areaListFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(areaListFile));
		ArrayList<Integer> result = new ArrayList<Integer>();
		while (true) {
			String line = reader.readLine();
			if (line == null) {break;}
			int area = Integer.valueOf(line);
			result.add(area);
		}
		reader.close();
		return result;
	}
	
	
	/*** reads the header and sets up all the info we need. 
	 * @throws IOException ***/
	private void readHeader(BufferedReader reader) throws IOException {
		String header = reader.readLine();
		String [] headerFields = header.split("\\|");
		int indx = 0;
		for (String s: headerFields) {
			nameToIndx.put(s,indx);
			if (s.startsWith(AREA_PREFIX)) {
				areaToIndx.put(s, indx);
			} else if (!REQUIRED.contains(s)) {
				categoryNameToIndex.put(s, indx);
			}
			indx ++;
		}
		for (String required: REQUIRED) {
			if (!nameToIndx.containsKey(required)) {
				throw new IllegalArgumentException("File missing required column: "+required);
			}
		}
		this.numCategories = categoryNameToIndex.size();
		if (this.numCategories <= 0) {
			throw new IllegalArgumentException("The input file contains no category columns");
		}
	
		
	}
	
	
	/*** load the specified number of lines from a file. Periods are assumed to begin at 0. ***/
	public Data load(String dataFile, String areaListFile, int lastPeroidID) throws IOException {
		this.areaList = readAreaFile(areaListFile);
		this.numDays = lastPeroidID;
		
		Map<String, Set<String>> categoryNameToLevels = new HashMap<String,Set<String>>();
		LocationHierachy heirachy = new LocationHierachy();
		
		BufferedReader reader = new BufferedReader(new FileReader(dataFile));
		try {
		
			readHeader(reader);
						
			while (true) {
				String line = reader.readLine();
				if (line == null) {break;}
				String[] data = line.split("\\|");
				//float lat =  Float.valueOf(data[nameToIndx.get(LAT)]);
				//float lon =  Float.valueOf(data[nameToIndx.get(LON)]);
				int period = Integer.valueOf(data[nameToIndx.get(PERIOD)]);
				periodCounts.increment(period);

				int area = Integer.valueOf(data[nameToIndx.get(AREA)]);
				if (!areaList.contains(area)) {
					throw new IllegalArgumentException("Area "+area+" encountered in data but not in area list");
				}
				
				
				for (Entry<String,Integer> a: areaToIndx.entrySet()) {
					int areaID = Integer.valueOf(data[areaToIndx.get(a.getKey())]);
					heirachy.add(area, areaID, a.getKey());	
				}
				
				for (Entry<String,Integer> c : categoryNameToIndex.entrySet()) {
					String categoryValue = data[c.getValue()];
					Set<String> levels = categoryNameToLevels.get(c.getKey());
					if (levels == null) {
						levels = new HashSet<String>();
						categoryNameToLevels.put(c.getKey(), levels);
					}
					levels.add(categoryValue);
					
					for (Entry<String,Integer> a: areaToIndx.entrySet()) {
						int areaID = Integer.valueOf(data[areaToIndx.get(a.getKey())]);
						CrimeKey key = new CrimeKey(a.getKey(), areaID, c.getKey(), categoryValue);
						incrementCount(key, period);	
					}	
				}
			}
			
		} finally {
			reader.close();
		}
		
		
		int lastPeriodInData = Collections.max(periodCounts.getKeys());
		int firstPeriodInData = Collections.min(periodCounts.getKeys());
		
		if (lastPeriodInData > lastPeroidID) {
			throw new IllegalArgumentException("Period "+lastPeriodInData +" in data > specified last period:"+lastPeroidID);
		}
		
		if (firstPeriodInData < 0) {
			throw new IllegalArgumentException("Period "+firstPeriodInData +" < 0");
		}
		
		List<Integer> missing = new ArrayList<Integer>();
		for (int p = 0; p <= lastPeroidID; p ++) {
			if (!periodCounts.containsKey(p)) {
				missing.add(p);
			}
		}
		if (missing.size() > 0 ) {
			String message = "WARNING: no events for "+missing.size() +" periods in the data";
			if (missing.size() < 20) {message += " "+missing;}
			System.out.println(message);
		}
		
		Data data = new Data(counts,numDays, areaList, categoryNameToLevels,heirachy);
		
		return data;
	}
	
	
		
	

}
