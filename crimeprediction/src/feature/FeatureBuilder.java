package feature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.Distance;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import au.com.bytecode.opencsv.CSVWriter;

public class FeatureBuilder {
	private static int[] periods = {1,7,28,112,365};
	//private static String[] categories = {"06","08","05","14","43","07","03","04","24"};
	private static int NUM_NEAREST = 5;
	private static final String[] LIGHT = {"14","24","16","13","43"};
	private static final String[] VIOLENT = {"04","01","02","03","08"};
	private static final String[] PROPERTY = {"06","05","07"};
	private static final String[][] cats = {LIGHT,VIOLENT,PROPERTY};
	private static final String[] catNames = {"light","violent","property"};
	private static final int NUM_AREAS = 1;
	private static ArrayList<String> urcCats = new ArrayList<String>(); // a list of all the urc codes included in the groups - assumed not to be duplicates.
	static {
		for (String[] group: cats) {
			for (String ucr: group) {
				urcCats.add(ucr);
			}
		}
	}
	
	
	
	
	
	private ArrayList<LocalDate> allDays;
	
	private Data data;
	
	public FeatureBuilder(Data data) {
		this.data = data;
		LocalDate endRange = Data.MAX_DATE;
		LocalDate startRange = Data.MIN_DATE.plusDays(periods[periods.length - 1]);
		allDays = new ArrayList<LocalDate>();
		allDays.add(startRange);
		int numDays = Days.daysBetween(startRange, endRange).getDays();
		for (int i = 1; i < numDays; i ++){
			LocalDate nextDay = startRange.plusDays(i);
			allDays.add(nextDay);
		}
	}
	
	static class Row {
		String[] row;
		int indx = 0;
		public Row(int length) {
			row = new String[length];
		}
		
		public void add(int value) {
			row[indx] = String.valueOf(value);
			indx ++;
		}
		
		public void add(int[] values) {
			for (int value: values) {
				add(value);
			}
		}
		
		public String[] getRow(){
			return this.row;
		}
			
	}
	
	public String[] getHeader() {
		String[] head = new String[periods.length * cats.length*(NUM_NEAREST+1)+5];
		head[0] = "target";
		head[1] = "year";
		head[2] = "area";
		head[3] = "dow";
		head[4] = "month";
		int indx = 5;
		for (int closeness = 0; closeness <= NUM_NEAREST; closeness ++) {
			for (int cIndx = 0; cIndx < cats.length;cIndx ++){
				for (int pIndx = 0; pIndx < periods.length; pIndx ++) {	
					String category = catNames[cIndx];
					int period = periods[pIndx];
					head[indx] = "dist"+closeness+"period"+period+"category"+category;
					indx ++;
				}
			}
		}
		return head;
	}
	
	public void outputFeatures(File f) throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(f), '\t');
		int ncolumns = periods.length*cats.length*(NUM_NEAREST+1)+5;
		int rowCount = 0;
		List<String> areas = data.getTopAreas(NUM_AREAS);
		int numAreas = areas.size();
		int numDays = allDays.size();
		System.out.println("BUILDING FEATURES FOR "+numDays+" DAYS AND " +numAreas+" AREAS, TOTAL: "+numDays*numAreas);
		System.out.println(areas);
		writer.writeNext(getHeader());
		for (String area: areas) {
			List<Distance> neighbours = data.getNeighboursOrderedByDistance(area);
			for (LocalDate day: allDays ) {
				rowCount ++;
				if (rowCount%10000 == 0) {
					System.out.println(rowCount);
				}
				// start a new row
				Row row = new Row(ncolumns);
				row.add(calculateTargetVariable(area, day)); //target variable
				row.add(day.getYear());
				row.add(Integer.valueOf(area));
				row.add(day.getDayOfWeek());
				row.add(day.getMonthOfYear());
				row.add(calculateCounts(area, day)); // crime counts this area
				// get the nearest NUM_NEAREST areas and calculate counts for them
				for (int i = 0; i < NUM_NEAREST; i ++) {
					Distance dist = neighbours.get(i);
					String nextArea = dist.getArea();
					row.add(calculateCounts(nextArea,day));
				}
				//output the row
				writer.writeNext(row.getRow());
			}
		}
		writer.close();
	}
	

	/*** current target is number of thefts. ***/
	private int calculateTargetVariable(String area, LocalDate day) {
		return data.getCrimeCount(area, "06", day);
	}
	
	
	/*** 
	 * 
	 * @param area
	 * @param day
	 * @return an array of length ncategories*nperiods, containing the counts of crime for each category for each period back from the specified day for this area.
	 */
	private int[] calculateCounts(String area, LocalDate day) {
		int[] result = new int[cats.length * periods.length];
		int indx = 0;
		
		for (String[] group: cats) {
			int[] totalCounts = new int[periods.length];
			for (String ucrCode: group) {
				int[] counts = data.getCrimesCount(area, ucrCode, day, periods);
				for (int i = 0; i < counts.length; i++) {
					totalCounts[i] += counts[i];
				}
			}
			
			for (int numCrimes : totalCounts) {
				result[indx] = numCrimes;
				indx ++;
			}
		}
		return result;
	}
	
}
