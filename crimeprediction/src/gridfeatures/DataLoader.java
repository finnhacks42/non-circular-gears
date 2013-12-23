package gridfeatures;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import model.Crime;

import org.joda.time.LocalDate;

import utilities.SU;


/*** This class loads crime data in a format where there is one row per crime, and transforms it into features.
 *  The data to be loaded must contain a latitude column, a longitude column, an integer period column (equivalent to a binning of the events through time),
 *  and an integer areaID column (equivalent to a binning of the event in space),  in that order.
 *  Any additional columns will be assumed to be 'category' columns.
 *  The latitude and longitude columns must contain decimals, the period and areaID column must be an integer starting from 0, and the category columns can be strings
 *  Counts will be produced separately for each category column. 
 *  The file must be | separated and the first line is assumed to consist of a header containing column names
 ***/
public class DataLoader {
	
	private int numCategories;
	private float[] lat;
	private float[] lon;
	private int[] period;
	private int[] area;

	private String[] categoryNames; //lets us lookup a categoryName from its indx.
	
	private Map<Integer,List<String>> categories = new HashMap<Integer,List<String>>(); // maps from the index of the category column to the list of values
	private Map<String,Integer> categoryNameToIndex = new HashMap<String,Integer>(); //maps from the name of a category column to its column index
	private Map<Integer,Set<String>> categoryLevels = new HashMap<Integer,Set<String>>(); //maps from category index to list of all levels for that category
	private Map<Integer,List<Float>> areaLats = new HashMap<Integer, List<Float>>(); // maps from areaID to list of all latitudes of crimes
	private Map<Integer, List<Float>> areaLongs = new HashMap<Integer,List<Float>>(); // maps from areaID to list of all longitude of crimes
	private Map<CrimeKey,TreeMap<Integer,Integer>> counts = new HashMap<CrimeKey, TreeMap<Integer,Integer>>(); // maps (area, category, category-level) to a sorted map from period to num-crimes
	private List<Integer> areaList = new ArrayList<Integer>(); // a list of all the area IDs under consideration in the study. Not all may actually contain crime and thus appear in the data file.
	private int numDays; //the number of periods, here a day may be a day or a watch, etc
	
	
	
	
	
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
	
	/*** checks that the data structure that has been built is valid. Throws and exception if not. ***/
	private void validate() {
		//TODO
	}
	
	/*** load the specified number of lines from a file. ***/
	public Data load(String dataFile, String areaListFile, int numlines, int lastPeroidID) throws IOException {
		this.areaList = readAreaFile(areaListFile);
		this.numDays = lastPeroidID;
		
		BufferedReader reader = new BufferedReader(new FileReader(dataFile));
		String header = reader.readLine();
		String [] headerFields = header.split("\\|"); 
		this.numCategories = headerFields.length - 4; //there are 4 fixed fields; lat, long, period, area
		
		if (numCategories < 0) {
			reader.close();
			throw new IllegalArgumentException("The input file contains insufficient columns");
		}
		
		this.categoryNames = new String[numCategories];
		for (int i = 4; i < headerFields.length; i ++) {
			int catIndx = i - 4;
			String catName = headerFields[i];
			categoryNameToIndex.put(catName, catIndx);
			categoryNames[catIndx] = catName;	
		}
		
		
		lat = new float[numlines];
		lon = new float[numlines];
		period = new int[numlines];
		area = new int[numlines];
		
		int lineNumber = 0;
		while (lineNumber < numlines) {
			String line = reader.readLine();
			if (line == null) {break;}
			//System.out.println(line);
			String[] data = line.split("\\|");
			//System.out.println(Arrays.toString(data));
			float la =  Float.valueOf(data[0]);
			float lo =  Float.valueOf(data[1]);
			int p = Integer.valueOf(data[2]);
			int a = Integer.valueOf(data[3]);
			lat[lineNumber] = la;
			lon[lineNumber] = lo;
			period[lineNumber] = p;
			area[lineNumber] = a;
			
			if (!areaLats.containsKey(a)) { areaLats.put(a, new ArrayList<Float>());}
			areaLats.get(a).add(la);
			
			if (!areaLongs.containsKey(a)) {areaLongs.put(a , new ArrayList<Float>());}
			areaLongs.get(a).add(lo);
			
			for (int i = 0; i < numCategories; i ++) {
				String categoryValue = data[4+i];
				String categoryName = categoryNames[i];
				CrimeKey key = new CrimeKey(a, categoryName, categoryValue);
				incrementCount(key, p);			
				if (!categories.containsKey(i)){
					categories.put(i, new ArrayList<String>());
					categoryLevels.put(i, new HashSet<String>());
				}
				categories.get(i).add(categoryValue);
				categoryLevels.get(i).add(categoryValue);
			}
			
			lineNumber ++;
		}
		
		if (lineNumber < numlines) {
			System.out.println("Warning:less lines in file than specified to read. Asked for:"+numlines+" read:"+lineNumber);
		} 
		reader.close();
		
		
		Map<String, Set<String>> categoryNameToLevels = new HashMap<String,Set<String>>();
		for (Entry<String,Integer> entry: categoryNameToIndex.entrySet()) {
			categoryNameToLevels.put(entry.getKey(), categoryLevels.get(entry.getValue()));
		}
		 
		Data data = new Data(counts,numDays, areaList, categoryNameToLevels, areaLats, areaLongs);
		return data;
	}
	
	
		
	

}
