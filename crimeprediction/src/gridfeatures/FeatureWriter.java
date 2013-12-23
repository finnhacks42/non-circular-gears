package gridfeatures;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;



/*** This class generates and writes out a sparse representation of the features. ***/
//there will be areas where crime never occurs in the dataset. These will still be instances, but not required for features (in a sparse representation)
			// do the translation to sparse feature representation.
			/*** Create sparse features space representation.
			 * Create an instance for each period, for each possible areaID.
			 * Features for each instance ... 
			 * 	 periods back X nearest n squares X 
			 */
			
			//int[] periods = {1,2,3,4,5,6,7,14,28,56,112,365};

public class FeatureWriter {
	private static final int[] PERIODS_BACK = {1,2,3,4,5,6,7,14,28,56,90,182,362};
	private static int FURTHEST_BACK = PERIODS_BACK[PERIODS_BACK.length - 1];
	
	private Data data;
	private int numFeatures;
	private int numInstances;
	
	public FeatureWriter(Data data) {
		this.data = data;
		numFeatures = 0;
		int numAreasTimeNumPeriods =  PERIODS_BACK.length * 5; //data.getAreas().size()
		for (String category: data.getCategories()){
			int numLevels = data.getLevels(category).size();
			numFeatures += numLevels * numAreasTimeNumPeriods;
		}
		numInstances = (data.getNumPeroids() - FURTHEST_BACK) * data.getAreas().size();
	}
	
	public String toString(){
		return "features:"+numFeatures+", instances:"+numInstances;
	}
	
	
	
	public void write(BufferedWriter writer) throws IOException {
		int count = 0;
		// one instance per peroid per area
		for (int period = FURTHEST_BACK; period < data.getNumPeroids(); period ++) {
			for (int area: data.getAreas()) {
				int[] features = buildFeatures(period, area);
				int target = calculateTarget(period, area);
				writeInstance(features, target, writer);
				count ++;
				if (count % 1000 == 0) {
					System.out.println(count);
				}
			}
		}
		
	}
	
	private void writeInstance(int[] features, int target, BufferedWriter writer) throws IOException {
		StringBuilder text = new StringBuilder();
		//text.append(target).append(" | ");
		text.append("| ");
		for (int i = 0; i < features.length; i++) {
			int f = features[i];
			if (f != 0) {
				text.append("f").append(i).append(":").append(f).append(" ");
			}
		}
		if (text.length() > 4) {
			text.deleteCharAt(text.length()-1);
			writer.write(text.toString());
			writer.newLine();
		}
		//text.append(target);
		
		
	}
	
	/*** returns the amount of crime in the specified area in the specified period. ***/
	public int calculateTarget(int period, int targetArea) {
		CrimeKey key = new CrimeKey(targetArea, "ones", "1");
		return data.getCount(key, period);
	}
	
	/*** built the features for a specific instance specified by a peroid/area combination. 
	 * Features consist of;
	 * for each category, amount of crime for each level of that cateogory, in each other area (ordered by distance to this one) in each of the specified number of days back.
	 */
	private int[] buildFeatures(int period, int targetArea) {
		int[] result = new int[numFeatures];
		int indx = 0;
		
		List<Integer> otherAreas = data.getAreasOrderedByDistance(targetArea,5);
		if (otherAreas == null) {
			return result; //this area has no crime and thus we could not calculate a centroid //TODO this is wrong - I should have kept the centroids from R.
		}
		for (int area: otherAreas){
			for (String category: data.getCategories()){
				for (String level: data.getLevels(category)) {
					CrimeKey key = new CrimeKey(area, category, level);
					int[] featureSubset = data.calculateCounts(key, period, PERIODS_BACK);
					indx = arrayInsert(featureSubset, result, indx);
				}
			}
		}
		return result;
	}
	
	/*** inserts the elements of one array into another starting from the specified indx. 
	 * Returns the index directly after the last value inserted into the target array.***/
	private int arrayInsert(int[] insertion ,int[] target, int indx) {
		int insertionIndx = indx;
		for (int value: insertion) {
			target[insertionIndx] = value;
			insertionIndx ++;
		}
		return insertionIndx;
	}

}
