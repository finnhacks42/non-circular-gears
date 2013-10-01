package feature;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import model.Crime;
import model.Distance;
import model.GeoPoint;
import model.Time;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Data {
	private static final DateTimeFormatter FMT = DateTimeFormat.forPattern("MM/dd/yyyy");
	private static final LocalDate MAX_DATE = LocalDate.parse("01/01/2007",FMT);
	private static final Integer MIN_AREA_POINTS = 100;
	private HashMap<String,ArrayList<Crime>> crimes = new HashMap<String,ArrayList<Crime>>();
	
	private HashMap<String,TreeMap<LocalDate,Integer>> eventCounts;
	private Map<String,GeoPoint> centroids;
	private Map<String,ArrayList<Distance>> nearests;
	
	// crime of category X at distance D over period T
	
	/*** Creates a map from the area+ucrCat -> a map from date -> num of events. 
	 * This is used to efficiently get the count of the number of crimes of category X that occured in area A, within D days of date Dt.***/
	private HashMap<String,TreeMap<LocalDate,Integer>> calculateCounts(HashMap<String,ArrayList<Crime>> crimes) {
		HashMap<String,TreeMap<LocalDate,Integer>> eventCounts = new HashMap<String,TreeMap<LocalDate,Integer>>();
		for (Entry<String,ArrayList<Crime>> area : crimes.entrySet()) {
			for (Crime crime: area.getValue()) {
				String ucr = crime.getUcrCat();
				String key = area.getKey()+ucr;
				LocalDate dt = crime.getDate();
				
				TreeMap<LocalDate,Integer> countsThisAreaAndCat = eventCounts.get(key);
				if (countsThisAreaAndCat == null) {
					countsThisAreaAndCat = new TreeMap<LocalDate,Integer>();
					countsThisAreaAndCat.put(dt, 1);
					eventCounts.put(key, countsThisAreaAndCat);
				} else {
					Integer count = countsThisAreaAndCat.get(dt);
					if (count == null) {count = 0;}
					countsThisAreaAndCat.put(dt, count + 1);
				}	
			}
		}	
		return eventCounts;
	}
	
	
	/***
	 * Returns a count of the number of crimes in given area and category, for a range of periods of days back from the specified date. 
	 * @param area the reportinarea
	 * @param ucr the two digit category based on ucr code
	 * @param dt the date to go back from.
	 * @param daysback an ordered array of the number of days to go back, from smallest to largest.
	 * @return
	 */
	public int[] getCrimesCount(String area,String ucr, LocalDate dt, int[] daysback ) {
		int[] result = new int[daysback.length];
	
		TreeMap<LocalDate,Integer> counts = eventCounts.get(area+ucr);
		if (counts == null) {return result;} // return all 0's.
		Integer sum = 0;
		LocalDate endRange = dt;
		int indx = 0;
		for (int day: daysback) {
			LocalDate startRange = dt.minusDays(day);
			for (Integer numCrimes : counts.subMap(startRange, true, endRange, false).values()) {
				sum += numCrimes;
			}
			result[indx] = sum;
			endRange = startRange;
			indx ++;
		}
		return result;
	}
	
	
	/*** 
	 * Calculates the distance from each reporting-area to all the others and stores the results.
	 * @param centroids A map from reportingarea name to its centroid 
	 * @return a map from reportingarea name to a list of all the other reportingareas, ordered by distance, closest first.
	 */
	private Map<String,ArrayList<Distance>> calculateNearest(Map<String,GeoPoint> centroids){
		Map<String,ArrayList<Distance>> result = new HashMap<String,ArrayList<Distance>>();
		for (Entry<String,GeoPoint> area: centroids.entrySet()) {
			ArrayList<Distance> distances = new ArrayList<Distance>();
			for (Entry<String,GeoPoint> area2: centroids.entrySet()) {
				if (!area.getKey().equals(area2.getKey())){
					double dist = area.getValue().distance(area2.getValue());
					Distance d = new Distance(area2.getKey(), dist);
					distances.add(d);
				}
			}
			Collections.sort(distances);
			result.put(area.getKey(), distances);
		}
		return result;
	}
	
	/*** Sorts the input list by the specified comparator and returns the middle element. ***/
	private Crime sortAndExtractMedian(List<Crime> crimes, Comparator<Crime> c) {
		Collections.sort(crimes,c);
		int indx = crimes.size()/2;
		return crimes.get(indx);
	}
	
	/*** returns a list of distances to all the other areas, ordered by distance, with the closest first. ***/
	public ArrayList<Distance> getNeighboursOrderedByDistance(String area) {
		return nearests.get(area);
	}
	
	/*** Returns a map from reporting area name to the position of its centroid. ***/
	public Map<String,GeoPoint> getCentroids(){
		return this.centroids;
	}
	
	/*** returns a list of all the reporting areas in the data for which a centroid could be calculated. ***/
	public List<String> getAreas(){
		ArrayList<String> result = new ArrayList<String>();
		result.addAll(centroids.keySet());
		return result;
	}
	
	/*** returns a list of all the crimes in the specified area sorted by date, first to last, ***/
	public List<Crime> getCrimes(String area) {
		return crimes.get(area);
	}
	
	/*** calculates the centroids of each reporting area as the median latitude and longitude and returns the results as a map from area to centroid.
	 * The points considered will be those up-to the specified maxDate and with confidence > confThreshold
	 * Only areas with more than min points will be added to the map of centroid ***/
	private Map<String,GeoPoint> calculateCentroids(float confThreshold, LocalDate maxDate, int minPoints) {
		Map<String,GeoPoint> result = new HashMap<String,GeoPoint>();
		for (Entry<String,ArrayList<Crime>> area: crimes.entrySet()) {
			ArrayList<Crime> filtered = new ArrayList<Crime>();
			for (Crime crime: area.getValue()) {
				LocalDate dt = crime.getDate();
				GeoPoint geo = crime.getPoint();
				if (dt.compareTo(maxDate) <= 0 && geo != null && geo.getConf() >= confThreshold) {
					filtered.add(crime);
				}
			}
			if (filtered.size() > minPoints) {
				float medlat = sortAndExtractMedian(filtered,new Crime.LatComparator()).getPoint().getLat();
				float medlon =  sortAndExtractMedian(filtered,new Crime.LonComparator()).getPoint().getLon();
				GeoPoint centroid = new GeoPoint(medlat, medlon, 1);
				result.put(area.getKey(), centroid);
			}
		}
		return result;
	}
	
	/*** reads crime data from a file in the format of the DALLAS 911 calls. 
	 * @throws IOException ***/
	public void readDallas911Data(File f) throws IOException {
		CSVRead reader = new CSVRead(f,"\t");		
	    reader.readNext(); //read the header
	    
		int count = 0;
		while (count < 500000) {
			String[] fields = reader.readNext();
			if (fields == null) {break;}
			LocalDate date = LocalDate.parse(fields[3],FMT);
			if (date != null) {
				Crime crime = new Crime(fields[1], date);
				crime.setBeat(fields[8]);
				crime.setDescription(fields[4]);
				crime.setMethod(fields[36]);
				crime.setPremises(fields[30]);
				String reportingArea = fields[9];
				crime.setReportingArea(reportingArea);
				crime.setResonceTimes(Time.parseTime(fields[7]), Time.parseTime(fields[5]), Time.parseTime(fields[6]));
				crime.setUcr(fields[34]);
				crime.parseGeo(fields[49], fields[50], fields[51]);
			
				ArrayList<Crime> events = crimes.get(reportingArea);
				if (events == null) {
					events = new ArrayList<Crime>();
					crimes.put(reportingArea, events);
				}
				events.add(crime);
				
				if (count % 10000 == 0) {
					System.out.println(" READING LINE: "+count);
				}
				count +=1;
			}
		}
		reader.close();
		System.out.println("COMPLETED READING DATA");
		
		//now we need to sort the crimes by the date on which they occur with the earliest first
		for (ArrayList<Crime> events: crimes.values()) {
			Collections.sort(events, new Crime.DateComparator());
		}
		System.out.println("DONE SORTING");
		
		this.centroids = calculateCentroids(0.8f, MAX_DATE, MIN_AREA_POINTS);
		System.out.println("CENTROIDS CALCULATED");
		this.nearests = calculateNearest(this.centroids);
		System.out.println("NEAREST CALCULATED");
		this.eventCounts = calculateCounts(crimes);
		System.out.println("COUNTS CALCULATED");
	}
	
	public static void main(String[] args) throws IOException {
		Data data = new Data();
		data.readDallas911Data(new File("/home/finn/phd/data/rawwithgeo.txt"));
		BufferedWriter out = new BufferedWriter(new FileWriter("/home/finn/phd/data/distances.txt"));
		for (Entry<String,GeoPoint> centroid: data.getCentroids().entrySet()) {
			for (Entry<String,GeoPoint> centroid2: data.getCentroids().entrySet()) {
				if (centroid.getKey().compareTo(centroid2.getKey()) > 0) {
					double dist = Math.abs(centroid.getValue().distance(centroid2.getValue()));
					out.write(dist+"\n");
				}
			}
		}
		out.close();
	}
	
	
	
	

}
