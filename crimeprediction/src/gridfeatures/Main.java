package gridfeatures;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
	
	public static void main(String[] args) throws IOException {
		DataLoader loader = new DataLoader();
		String dataFile = "/home/finn/phd/data/topic_output.txt";
		String areaFile = "/home/finn/phd/data/topic_cells.txt";
		Data data = loader.load(dataFile, areaFile, 380975, 730);
		System.out.println("Data Loaded");
		FeatureWriter featureGenerator = new FeatureWriter(data);
		System.out.println(featureGenerator);
		BufferedWriter output = new BufferedWriter(new FileWriter("/home/finn/phd/data/grid_features_sparse2.txt"));
		featureGenerator.write(output);
		
		System.out.println("DONE");
	}

}
