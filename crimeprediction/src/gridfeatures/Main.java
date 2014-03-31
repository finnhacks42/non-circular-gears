package gridfeatures;


//TODO modify this so I can make the target the # of crimes in a given day, week, etc.
//TODO write code to transform the input in lat lon into an x, y, such that x and y are the distances in km from some reference point. Perhaps I can do this in r?
//TODO write code so that I can make features based on nearby cells in other ways ...


import java.io.IOException;

public class Main {
	
	public static void main(String[] args) throws IOException, InvalidDataStoreException {
		DataLoader loader = new DataLoader();
		String path = "/home/finn/phd/data/20140226/";
		String dataFile = path + "events200_all.txt";
		String areaFile = path + "cells200.txt";
		
		DataI data = new Data();
		
		loader.load(dataFile, areaFile,2556, data, "crime","burglary","prem","RESIDENCE");
		
		System.out.println("DATA LOADED");
		System.out.println("INPUT TARGET TOTAL:"+data.getTargetTotal());
		data.checkConsistency();
		System.out.println("Data consistant");
		
		
		int[] daysback = {7,365};
		int reportFrequency = 100000;
		double trainPer = 4/6d; // % of the data for training
		double validPer = 1/6d; // % of the data for validation - remaining % will be test
		String outputName = "VW200all";
	
		
		String[] areasToLabel = {"area","area1000","area5000"}; 
		
		//what if I need to specify multiple constraints on the target (ie crime = 'burglary', place = 'residence')
		FeatureWriter featureGenerator = new FeatureWriter(data,daysback,1,areasToLabel);
		featureGenerator.setReportFrequency(reportFrequency);
		
		System.out.println(featureGenerator);
		
		featureGenerator.write(trainPer, validPer, path, outputName,FeatureWriter.FORMAT_VW);
		
		System.out.println("DONE");
	}

}
