package feature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.joda.time.LocalDate;

import au.com.bytecode.opencsv.CSVWriter;

/*** This class extracts information to examine how different types of crime are correlated to one another***/
public class Correlation {
	private Data data;
	private String[] ucrcodes =  {"06","08","05","14","43","07","03","04","24"};
	
	
	// basically choose a time period (num of days), go through all the days in the data set, and spit out the number of crimes of each type on that day 
	
	public Correlation(Data d) {
		this.data = d;
	}

	/***
	 * 
	 * @param period the number of days over which we are aggregating
	 * @param forward the number of days to move forward between calculations, allows for sliding windows if forward < period, 
	 * @throws IOException 
	 */
	public void output(int period, int forward, File f) throws IOException{
		CSVWriter writer = new CSVWriter(new FileWriter(f), '\t');
		if (forward > period) {
			throw new IllegalArgumentException("Specifying forward greature than period would skip days in data");
		}
		//for each area, for each period, output number of crimes of each type. One row per area per period.
		LocalDate end = Data.MIN_DATE.plusDays(period);
		int periodID = 1;
		String[] header = new String[ucrcodes.length+2];
		header[0] = "area";
		header[1] = "periodID";
		for (int i = 0; i < ucrcodes.length; i++) {
			header[i+2] = "ucr"+String.valueOf(ucrcodes[i]);
		}
		writer.writeNext(header);
		while (end.isBefore(Data.MAX_DATE)) {
			for (String area:data.getAreas()){
				String[] result = new String[ucrcodes.length+2];
				result[0] = area;
				result[1] = String.valueOf(periodID);
				int indx = 2;
				for (String ucr: ucrcodes) {
					int count = data.getCrimesCount(area, ucr,end,period);
					result[indx] = String.valueOf(count);
					indx ++;
				}
				writer.writeNext(result);
			}
			end = end.plusDays(forward);
			periodID +=1;
			
		}
		writer.close();
		
	}
}
