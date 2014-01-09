package gridfeatures;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
	
	public static void main(String[] args) throws IOException {
		DataLoader loader = new DataLoader();
		String dataFile = "/home/finn/phd/data/20140109/events_2000_2002_150.txt";
		String areaFile = "/home/finn/phd/data/20140109/cells150.txt";
		//TODO remember to change the number of lines, max day and grid with parameters
		
		Data data = loader.load(dataFile, areaFile, 571742, 1095);
		//data.createAreaNetworkFromGrid(9);
		
		System.out.println("Data Loaded");
		System.out.println("Number of areas:"+data.getAreas().size());
		
		FeatureWriter featureGenerator = new FeatureWriter(data);
		System.out.println(featureGenerator);
		
		
		BufferedWriter output = new BufferedWriter(new FileWriter("/home/finn/phd/data/20140109/f_2000_2002_150.txt"));
		featureGenerator.writeFull(output);
		output.close();
		
		//BufferedWriter output = new BufferedWriter(new FileWriter("/home/finn/apps/vowpal_wabbit-7.4/finn/crime_5000_test.dat"));
		//BufferedWriter targetsOutput = new BufferedWriter(new FileWriter("/home/finn/apps/vowpal_wabbit-7.4/finn/crime_5000_test.labels"));
		//featureGenerator.writeSparce(output, targetsOutput);
		//output.close();
		//targetsOutput.close();
		System.out.println("DONE");
	}

}