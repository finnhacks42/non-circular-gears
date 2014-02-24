package gridfeatures;


//TODO modify this so I can make the target the # of crimes in a given day, week, etc.
//TODO write code to transform the input in lat lon into an x, y, such that x and y are the distances in km from some reference point. Perhaps I can do this in r?
//TODO write code so that I can make features based on nearby cells in other ways ...


import java.io.IOException;

public class Main {
	
	public static void main(String[] args) throws IOException, InvalidDataStoreException {
		DataLoader loader = new DataLoader();
		String path = "/home/finn/phd/data/20140220/";
		String dataFile = path + "events1000_bc.txt";
		String areaFile = path + "cells1000.txt";
		
		DataI data = new Data();
		
		loader.load(dataFile, areaFile,2556, data);
		System.out.println("DATA LOADED");
		
		int[] daysback = {7,365};
		int reportFrequency = 100000;
		double trainPer = 4/6d; // % of the data for training
		double validPer = 1/6d; // % of the data for validation - remaining % will be test
		String outputName = "fVW1000bc";
		boolean labelArea = true;
		
		FeatureWriter featureGenerator = new FeatureWriter(data,daysback,"crime","burglary",1,labelArea);
		featureGenerator.setReportFrequency(reportFrequency);
		
		System.out.println(featureGenerator);
		
		featureGenerator.write(trainPer, validPer, path, outputName,FeatureWriter.FORMAT_VW);
		
		System.out.println("DONE");
	}

}
