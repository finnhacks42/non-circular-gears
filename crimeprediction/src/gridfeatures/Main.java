package gridfeatures;


//TODO modify this so I can make the target the # of crimes in a given day, week, etc.
//TODO write code to transform the input in lat lon into an x, y, such that x and y are the distances in km from some reference point. Perhaps I can do this in r?
//TODO write code so that I can make features based on nearby cells in other ways ...


import java.io.IOException;

public class Main {
	
	public static void main(String[] args) throws IOException {
		DataLoader loader = new DataLoader();
		String path = "/home/finn/phd/data/20140204/";
		String dataFile = path + "events200.txt";
		String areaFile = path + "cells200.txt";
		
		
		Data data = loader.load(dataFile, areaFile,1460);
		System.out.println("DATA LOADED");
		
		int[] daysback = {7,365};
		int reportFrequency = 100000;
		double trainPer = 1/3d; // a third of the data for training
		double validPer = 1/3d; // a third of the data for validation - remaining 3rd will be test
		String outputName = "fVW200";
		
		
		FeatureWriter featureGenerator = new FeatureWriter(data,daysback,null,null,1);
		featureGenerator.setReportFrequency(reportFrequency);
		System.out.println(featureGenerator);
		
		
		featureGenerator.writeVW(trainPer, validPer, path, outputName);
		
		
		System.out.println("DONE");
	}

}
