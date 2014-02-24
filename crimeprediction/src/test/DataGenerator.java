package test;

import gridfeatures.DataLoader;
import gridfeatures.LocationHierachy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/*** This class contains methods for generating data that can then be used to test other classes. ***/
public class DataGenerator {
	
	private LocationHierachy hierachy;
	private List<String> areaTypes = new ArrayList<String>();
	private String[] categories;
	int rowLength;
	
	public DataGenerator(LocationHierachy hierachy, String[] categories) {
		this.hierachy = hierachy;
		this.categories = categories;
		areaTypes.addAll(hierachy.getNameSpaces());
		
		rowLength = 3 + areaTypes.size()+categories.length;
	}
	
	/*** Generate a header for the data as it would be expected to occur in data set to be loaded from a file. ***/
	public String[] generateHeader() {
		String[] header = new String[rowLength];
		header[0] = DataLoader.LAT;
		header[1] = DataLoader.LON;
		header[2] = DataLoader.PERIOD;
		header[3] = DataLoader.AREA;
		int indx = 4;
		for (String areaType: areaTypes) {
			if (areaType != DataLoader.AREA) {
				header[indx] = areaType;
				indx ++;
			}
		}
		for (String category: categories) {
			header[indx] = category;
			indx ++;
		}
		return header;
	}
	
	/***
	 * Generate an event in the specified area and period.
	 * @param area the primary (smallest resolution) area in which the event occured
	 * @param period the period in which the event occured
	 * @param categoryValues a map from category names to the values for this event
	 * @return a row of strings representing the event
	 */
	public String[] generateEvent(int area, int period, Map<String,String> categoryValues) {
		String[] row = new String[rowLength];
		row[0] = "0.0";
		row[1] = "0.0";
		row[2] = String.valueOf(period);
		row[3] = String.valueOf(area);
		int indx = 4;
		for (String areaType: areaTypes) {
			if (areaType != DataLoader.AREA) {
				int parentArea = hierachy.getTargetAreaParent(areaType, area);
				row[indx] = String.valueOf(parentArea);
				indx ++;
			}
		}
		for (String category: categories) {
			String value = categoryValues.get(category);
			row[indx] = value;
			indx ++;
		}
		return row;
	}
	
	
	
	
	
	
	

}
