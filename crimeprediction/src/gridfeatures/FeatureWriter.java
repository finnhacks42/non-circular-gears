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
			

public class FeatureWriter {
	private static final int[] PERIODS_BACK = {1,7,14,28,365};
	private static int FURTHEST_BACK = PERIODS_BACK[PERIODS_BACK.length - 1];
	private static final int[] FURTHEST_BACK_A = {FURTHEST_BACK};
	private static final int NUM_NEIGHBOURS = 1; //the number of neighbours to generate features based on. 1 will be only the area itself, <= 0 will be all areas.
	
	private Data data;
	private int numFeatures;
	private int numInstances;
	
	public FeatureWriter(Data data) {
		this.data = data;
		numFeatures = 0;
		int numAreas = NUM_NEIGHBOURS > 0 ? NUM_NEIGHBOURS : data.getAreas().size();
		int numAreasTimeNumPeriods =  PERIODS_BACK.length * numAreas;
		for (String category: data.getCategories()){
			int numLevels = data.getLevels(category).size();
			numFeatures += numLevels * numAreasTimeNumPeriods;
			System.out.println("Category:"+category+" levels:"+numLevels);
		}
		numInstances = (data.getNumPeroids() - FURTHEST_BACK) * data.getAreas().size();
	}
	
	public String toString(){
		return "areas:"+data.getAreas().size()+", input periods:"+data.getNumPeroids()+", output periods:"+(data.getNumPeroids()-FURTHEST_BACK)+", features:"+numFeatures+", instances:"+numInstances;
	}
	
	
	/*** writes standard VW format with cellid and count namespaces. 
	 * @throws IOException ***/
	public void writeVW(BufferedWriter writer) throws IOException {
		int count = 0;
		for (int period = FURTHEST_BACK; period < data.getNumPeroids(); period ++) {
			for (int area: data.getAreas()) {
				float[] features = buildFeatures(period, area);
				int target = calculateTarget(period, area);
				
				writeVWInstance(area, target, features, writer);
				
				count ++;
				if (count % 100000 == 0) {
					System.out.println(count);
				}
			}
		}	
	}
	
	
	private void writeVWInstance(int area, int target, float[] countFeatures, BufferedWriter writer) throws IOException {
		StringBuilder text = new StringBuilder();
		text.append(target).append(" |a ").append(area).append(":").append(1).append(" |b ");
		for (int i = 0; i < countFeatures.length; i++) {
			float f = countFeatures[i];
			if (f != 0) {
				text.append(i+1).append(":").append(f).append(" ");
			}
		}
		writer.write(text.toString());
		writer.newLine();
	}
	
	
	
	public void writeVWLDA(BufferedWriter writer, BufferedWriter targets) throws IOException {
		int count = 0;
		// one instance per peroid per area
		for (int period = FURTHEST_BACK; period < data.getNumPeroids(); period ++) {
			for (int area: data.getAreas()) {
				float[] features = buildFeatures(period, area);
				int target = calculateTarget(period, area);
				int baseline = calculateBaseLineFeature(period, area);
				writeLDAInstance(features, writer);
				targets.write(String.valueOf(target));
				targets.write(",");
				targets.write(String.valueOf(baseline));
				targets.newLine();
				count ++;
				if (count % 10000 == 0) {
					System.out.println(count);
				}
			}
		}	
	}
	
	public void writeFull(BufferedWriter writer) throws IOException {
		writeFullHeader(writer);
		int count = 0;
		for (int period = FURTHEST_BACK; period < data.getNumPeroids(); period ++) {
			for (int area: data.getAreas()) {
				float[] features = buildFeatures(period, area);
				
				int target = calculateTarget(period, area);
				writeFullInstance(features, target, area, writer);
				count ++;
				if (count % 10000 == 0) {
					System.out.println(count);
				}
			}
		}	
	}
	
	
	
	
	private void writeFullInstance(float[] features,int target,int area, BufferedWriter writer) throws IOException {
		StringBuilder text = new StringBuilder();
		for (Object f: features) {
			text.append(f).append(",");
		}
		text.append(area).append(",");
		text.append(target);
		writer.write(text.toString());
		writer.newLine();
	}
	
	
	private void writeLDAInstance(float[] features, BufferedWriter writer) throws IOException {
		StringBuilder text = new StringBuilder();
		
		text.append("| ");
		for (int i = 0; i < features.length; i++) {
			float f = features[i];
			if (f != 0) {
				text.append(i).append(":").append(f).append(" ");
			}
		}
		if (text.length() > 4) {
			text.deleteCharAt(text.length()-1);
			writer.write(text.toString());
			writer.newLine();
		}	
	}
	
	/*** returns the amount of crime in the specified area in the specified period. ***/
	public int calculateTarget(int period, int targetArea) {
		CrimeKey key = new CrimeKey(targetArea, "ones", "1");
		return data.getCount(key, period);
	}
	
	/*** return the amount of crime (of relevent type/category) in this cell over the longest timeperiod. ***/
	public int calculateBaseLineFeature(int period, int targetArea) {
		CrimeKey key = new CrimeKey(targetArea, "prem","RESIDENCE");
		int[] result = data.calculateCounts(key, period, FURTHEST_BACK_A);
		return result[0];
	}
	
	
	/*** built the features for a specific instance specified by a peroid/area combination. 
	 * Features consist of;
	 * for each category, amount of crime for each level of that cateogory, in each other area (ordered by distance to this one) in each of the specified number of days back.
	 * Ordering of features:
	 * features for the closest area are returned first, followed by the 2nd closest etc.
	 */
	private float[] buildFeatures(int period, int targetArea) {
		float[] result = new float[numFeatures];
		int indx = 0;
		List<Integer> otherAreas = NUM_NEIGHBOURS <=0 ? data.getAreasOrderedByDistance(targetArea): data.getAreasOrderedByDistance(targetArea, NUM_NEIGHBOURS);
		if (otherAreas == null) {
			return result; 
		}
		for (int area: otherAreas){
			for (String category: data.getCategories()){
				for (String level: data.getLevels(category)) {
					CrimeKey key = new CrimeKey(area, category, level);
					float [] featureSubset = data.calculateNormalizedCounts(key, period, PERIODS_BACK);
					//int[] featureSubset = data.calculateCounts(key, period, PERIODS_BACK);
					indx = arrayInsert(featureSubset, result, indx);
				}
			}
		}
		return result;
	}
	
	
	
	private void writeFullHeader(BufferedWriter writer) throws IOException {
		
		StringBuilder header = new StringBuilder();
		int numAreas = NUM_NEIGHBOURS <=0 ? data.getAreas().size() :  NUM_NEIGHBOURS;
		
		for (int nthClosestArea = 0; nthClosestArea < numAreas; nthClosestArea ++) {
			for (String category: data.getCategories()) {
				for (String level: data.getLevels(category)) {
					for (int period: PERIODS_BACK) {
						header.append("A").append(nthClosestArea).append(category).append("_").append(level).append("_").append(period).append(",");
					}
				}
			}
		}
		header.append("area").append(",");
		header.append("target");
		writer.write(header.toString());
		writer.newLine();
	}
	
	/*** inserts the elements in insertion in the array target starting from the specified indx. 
	 * Returns the index directly after the last value inserted into the target array.***/
	private int arrayInsert(int[] insertion ,int[] target, int indx) {
		int insertionIndx = indx;
		for (int value: insertion) {
			target[insertionIndx] = value;
			insertionIndx ++;
		}
		return insertionIndx;
	}
	
	private int arrayInsert(float[] insertion, float[] target, int indx) {
		int insertionIndx = indx;
		for (float value: insertion) {
			target[insertionIndx] = value;
			insertionIndx ++;
		}
		return insertionIndx;
	}

}
