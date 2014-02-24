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
	public static final String AREA_PREFIX = "area";
	public static final String LAT = "lat";
	public static final String LON = "lon";
	public static final String PERIOD = "period";
	public static final String AREA = "area"; //the target will be grouped by this area.
	private static final List<String> REQUIRED = Arrays.asList(new String [] {LAT,LON,PERIOD,AREA});
	
	private int numCategories;
	private Map<String,Integer> categoryNameToIndex = new HashMap<String,Integer>(); //maps from the name of a category column to its column index
	private Counter<Integer> periodCounts = new Counter<Integer>();
	private Map<String, Integer> nameToIndx = new HashMap<String,Integer>(); //maps column names to column indx.
	private Map<String,Integer> areaToIndx = new HashMap<String,Integer>(); //maps additional area columns to their indexes.

	
	/*** reads a file where each line is an integer specifying an areaID that may occur in the dataset. ***/
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
		readHeader(headerFields);
	}
	
	private void readHeader(String[] headerFields) {
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
	

	private void loadRow(DataI dataStore, String[] data) {
		int period = Integer.valueOf(data[nameToIndx.get(PERIOD)]);
		int area = Integer.valueOf(data[nameToIndx.get(AREA)]);
		
		periodCounts.increment(period);
		
		if (!dataStore.getAreas().contains(area)) {
			throw new IllegalArgumentException("Area "+area+" encountered in data but not in area list");
		}
		
		// add the area and parent(s) to the location hierachy
		for (Entry<String,Integer> a: areaToIndx.entrySet()) {
			String areaNamespace = a.getKey();
			int areaID = Integer.valueOf(data[areaToIndx.get(areaNamespace)]);
			dataStore.getHierachy().add(area, areaID, a.getKey());	
		}
		
		
		for (Entry<String,Integer> c : categoryNameToIndex.entrySet()) {
			String categoryValue = data[c.getValue()];
			Set<String> levels = dataStore.getLevels(c.getKey());
					
			if (levels == null) {
				levels = new HashSet<String>();
				dataStore.setLevels(c.getKey(), levels);
			}
			levels.add(categoryValue);
			
			// for each category and area create a key
			for (Entry<String,Integer> a: areaToIndx.entrySet()) {
				int areaID = Integer.valueOf(data[areaToIndx.get(a.getKey())]);
				CrimeKey key = new CrimeKey(a.getKey(), areaID, c.getKey(), categoryValue);
				dataStore.incrementCount(key, period);	
			}	
		}
	}
	
	/*** load data from a file. Periods are assumed to begin at 0. ***/
	
	public void load(String dataFile, String areaListFile, int lastPeriodID, DataI dataStore) throws IOException {
		List<Integer> areas = readAreaFile(areaListFile);
		dataStore.setAreas(areas);
		dataStore.setNumPeriods(lastPeriodID);
			
		BufferedReader reader = new BufferedReader(new FileReader(dataFile));
		try {
			readHeader(reader);			
			while (true) {
				String line = reader.readLine();
				if (line == null) {break;}
				String[] data = line.split("\\|");
				loadRow(dataStore,data);
			}
			
		} finally {
			reader.close();
		}
		validatePeriods(lastPeriodID);
	}
	
	/*** load the data from memory. Useful for generating test instances of the Data object. ***/
	public void load(List<String[]> rows, String[] header, List<Integer> areas, int lastPeriodID,DataI dataStore) {
		dataStore.setAreas(areas);
		dataStore.setNumPeriods(lastPeriodID);
		
		readHeader(header);
		for (String[] row : rows) {
			loadRow(dataStore,row);
		}
		validatePeriods(lastPeriodID);
	}
	
	
	/*** Checks if any periods are missing in the data and that we don't have any periods that are beyond the stated final one, or prior to zero. ***/
	private void validatePeriods(int lastPeroidID) {
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
	}
	
	
		
	

}
