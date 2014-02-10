package gridfeatures;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import utilities.AU;


/*** This class generates and writes out a sparse representation of the features. ***/
//there will be areas where crime never occurs in the dataset. These will still be instances, but not required for features (in a sparse representation)
			// do the translation to sparse feature representation.
			/*** Create sparse features space representation.
			 * Create an instance for each period, for each possible areaID.
			 * Features for each instance ... 
			 * 	 periods back X nearest n squares X 
			 */
			

public class FeatureWriter {
	public static final String[] DATA_PARTITIONS = {"train","valid","test"};
	public static final String TEST = "test";
	public static final String TRAIN = "train";
	public static final String VALIDATE = "valid";
	private Data data;
	private int numFeatures;
	private int numInstances;
	private int[] periodsToAggregateOver;
	private  int furthestBack;
	private String targetCategoryName;
	private String targetCategoryValue;
	private String smallestCategoryName;
	private int periodsPerInstance;
	private int totalCategoryLevels;
	private int reportFrequency = -1;
	private int instanceTimewindows;
	
	
	/*** 
	 * 
	 * @param data
	 * @param periodsBack the periods to aggregate counts over when generating features
	 * @param numNeighbours 
	 * @param targetCategoryName what category is the target based on. If this is null then the target is assumed to be the total count across all categories
	 * @param targetCategoryValue what level of the specified category is the target based on. Ignored if targetCategoryName is null.
	 * @param periodsPerInstance the number of time periods that should be grouped together to create each instance.
	 */
	public FeatureWriter(Data data, int[] periodsBack, String targetCategoryName, String targetCategoryValue, int periodsPerInstance) {
		this.data = data;
		this.periodsToAggregateOver = periodsBack;
		this.furthestBack = periodsToAggregateOver[periodsToAggregateOver.length - 1];
		this.targetCategoryName = targetCategoryName;
		this.targetCategoryValue = targetCategoryValue;
		this.periodsPerInstance = periodsPerInstance;
		
		validatePeriodsBack(periodsBack);
		
		if (targetCategoryName != null) {
			Set<String> targetCategoryLevelSet = data.getLevels(targetCategoryName);
			if (targetCategoryLevelSet == null) {
				throw new IllegalArgumentException("Specified targetCategoryName:"+targetCategoryName+" does not exist");
			} if (!targetCategoryLevelSet.contains(targetCategoryValue)) {
				throw new IllegalArgumentException("Specified targetCategoryValue is not a level of specified targetCategoryName");
			}
		}
		
		numFeatures = 0;
		int numAreasTimeNumPeriods =  periodsToAggregateOver.length*data.getHierachy().depth();// * numAreas;
		int leastLevels = Integer.MAX_VALUE;
		totalCategoryLevels = 0;
		for (String category: data.getCategories()){
			int numLevels = data.getLevels(category).size();
			totalCategoryLevels += numLevels;
			if (numLevels < leastLevels) {
				leastLevels = numLevels;
				smallestCategoryName = category;
			}
			numFeatures += numLevels * numAreasTimeNumPeriods;
			System.out.println("Category:"+category+" levels:"+numLevels);
		}
		instanceTimewindows = (int) (Math.floor((data.getNumPeroids() - furthestBack)/((float)periodsPerInstance)));
		numInstances = instanceTimewindows * data.getAreas().size();
		
	}
	
	private void validatePeriodsBack(int[] daysback) {
		int prev = 0;
		for (int back: daysback) {
			if (back <= prev) {throw new IllegalArgumentException("periods back must be specified in order from smallest to largest");}
			prev = back;
		}
	}
	
	public String toString(){
		LocationHierachy h = data.getHierachy();
		StringBuilder b = new StringBuilder();	
		for (String areaNamespace: h.getNameSpaces()) {
			b.append("Area:").append(areaNamespace).append(", size:").append(h.size(areaNamespace)).append(" ,");
		}
		b.append("\n");
		b.append("Input periods:").append(data.getNumPeroids());
		b.append(", Output periods:").append(data.getNumPeroids()-furthestBack);
		b.append(", Periods/instance:").append(periodsPerInstance);
		b.append("\n");
		b.append("Target Areas:").append(data.getAreas().size());
		b.append(", Instance timewindows:").append(instanceTimewindows);
		b.append(", INSTANCES:").append(numInstances);
		b.append("\n");
		b.append("Time buckets:").append(periodsToAggregateOver.length);
		b.append(",Total category levels:").append(totalCategoryLevels);
		b.append(", area aggregation levels:").append(data.getHierachy().getNameSpaces().size());
		b.append(", FEATURES:").append(numFeatures);
		return b.toString();
	}
	
	
	/*** returns the crime count as broken down by targetCategoryName/Value in the specified area on the specified day ***/
	private int calculateSinglePeriodTarget(int period, int targetArea) {
		if (targetCategoryName == null) {
			int total = 0;
			for (String level: data.getLevels(smallestCategoryName)) {
				CrimeKey key = new CrimeKey(DataLoader.AREA,targetArea, smallestCategoryName, level);
				total += data.getCount(key, period);
			}
			return total;
		} else {
			CrimeKey key = new CrimeKey(DataLoader.AREA,targetArea, targetCategoryName, targetCategoryValue);
			return data.getCount(key, period);
		}
	}
	/*** returns the crime count as broken down by targetCategoryName/Value in the specified area for the days from period to period + periodsPerInstance ***/
	private float calculateTarget(int period, int target) {
		int count = 0;
		for (int i = 0; i < periodsPerInstance; i ++) {
			count += calculateSinglePeriodTarget(period+i, target);
		}
		return count/(float)periodsPerInstance;
	}

	/***
	 * Writes a non-sparse, csv representation of the features, with a header.
	 * format is f1, f2, ... fn, area, target
	 * @param writer the file to write the data to
	 * @param reportFrequency if > 0, prints out the line number every reportFrequency instances.
	 * @throws IOException if the specified file cannot be opened for writing.
	 */
	public void writeFull(BufferedWriter writer) throws IOException {
		
		writeFullHeader(writer);
		int count = 0;
		for (int period = furthestBack; period <= data.getNumPeroids()-periodsPerInstance; period += periodsPerInstance) {
			for (int area: data.getAreas()) {
				float[] features = buildFeatures(period, area);
				float target = calculateTarget(period, area);
				writeFullInstance(features, target, area, writer);
				count ++;
				if (reportFrequency > 0 && count % reportFrequency == 0) {
					System.out.println(count);
				}
			}
		}	
	}
	
	
	
	
	/*** writes one instance in non-sparce, csv format. f1, f2, ... fn, area, target. ***/
	private void writeFullInstance(float[] features,float target,int area, BufferedWriter writer) throws IOException {
		StringBuilder text = new StringBuilder();
		for (Object f: features) {
			text.append(f).append(",");
		}
		text.append(area).append(",");
		text.append(target);
		writer.write(text.toString());
		writer.newLine();
	}
	
	
	
	/***
	 * 
	 * @param trainPercentage percentage of the instanceTimewindows that should be in the training data
	 * @param validatePercentage percentage of the instanceTimewindows that should be in the validation data
	 * @param testPercentage percentage of the instanceTimewindows that should be used for the test data
	 * @param path
	 * @param filename
	 * @throws IOException
	 */
	public void writeVW(double trainPercentage, double validatePercentage, String path, String filename) throws IOException {
		if (trainPercentage + validatePercentage > 1) {
			throw new IllegalArgumentException("train + validation percentage must be <= 1");
		}
		BufferedWriter[] writers = new BufferedWriter[3];
		for (int indx = 0; indx < 3; indx ++) {
			writers[indx] = new BufferedWriter(new FileWriter(path+filename+DATA_PARTITIONS[indx]));
		}
		int[] boundaries = {(int)Math.floor(trainPercentage*instanceTimewindows), (int)Math.floor((trainPercentage+validatePercentage)*instanceTimewindows),instanceTimewindows};
		
		
		VWInstance instance = new VWInstance();
		int count = 0;
		int windowsCount = 0;
		int dataPartion = 0;
		BufferedWriter writer = writers[dataPartion];
		int periodBoundary = boundaries[dataPartion];
		
		for (int period = furthestBack; period <= data.getNumPeroids()-periodsPerInstance; period += periodsPerInstance) {
			if (windowsCount >= periodBoundary) {
				//System.out.println("windows count:" +windowsCount+", boundary:"+periodBoundary+", instances:"+count);
				dataPartion ++;
				writer = writers[dataPartion];
				periodBoundary = boundaries[dataPartion];
			}
			for (int area: data.getAreas()) {
				instance.setTarget(calculateTarget(period, area));
				instance.setNamespace("area");
				instance.addFeature(area, 1);
				buildAndWriteFeatures(period,area,instance,writer);
				instance.clear();
				count ++;
				writer.newLine();
//				if (count < numInstances) {
//					writer.newLine();
//				} else {
//					
//				}
				if (reportFrequency > 0 && count % reportFrequency == 0) {
					System.out.println(count);
				}
			}
			windowsCount ++;
		}	
		//System.out.println("windows count:" +windowsCount+", instances:"+count);
		for (Writer w: writers) {
			w.close();
		}
	}
	
	
	
	/*** build features for the specific instance specified by a period area combination. 
	 * @throws IOException ***/
	private void buildAndWriteFeatures(int period, int targetArea, VWInstance instance, BufferedWriter writer) throws IOException{
		LocationHierachy hierachy = data.getHierachy();	
		int featureID = 1;
		for (String areaAggregation: hierachy.getNameSpaces()) {
			Integer area = hierachy.getTargetAreaParent(areaAggregation, targetArea);
			for (String category: data.getCategories()) {
				String namespace = category+areaAggregation;
				instance.setNamespace(namespace);
				if (area != null) { //if area is null an instance will still be written. The target value will be 0. No other features will be included but we still write the namespaces.
					for (String level: data.getLevels(category)) {
						CrimeKey key = new CrimeKey(areaAggregation, area, category, level);
						float [] featureSubset = data.calculateNormalizedCounts(key, period, periodsToAggregateOver);
						for (float feature: featureSubset) {
							instance.addFeature(featureID, feature);
							featureID ++;
						}
					}
				}
			}
		}
		instance.write(writer);	
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
		LocationHierachy hierachy = data.getHierachy();
		
		for (String areaNameSpace: hierachy.getNameSpaces()){
			Integer area = hierachy.getTargetAreaParent(areaNameSpace, targetArea);
			if (area == null) {
				return result;
			}
			for (String category: data.getCategories()){
				for (String level: data.getLevels(category)) {
					CrimeKey key = new CrimeKey(areaNameSpace, area, category, level);
					float [] featureSubset = data.calculateNormalizedCounts(key, period, periodsToAggregateOver);
					indx = AU.arrayInsert(featureSubset, result, indx);
				}
			}
		}
		return result;
	}
	
	
	
	private void writeFullHeader(BufferedWriter writer) throws IOException {
		
//		StringBuilder header = new StringBuilder();
//		//int numAreas = numberOfNeighbours <=0 ? data.getAreas().size() :  numberOfNeighbours;
//		
//		//for (int nthClosestArea = 0; nthClosestArea < numAreas; nthClosestArea ++) {
//			for (String category: data.getCategories()) {
//				for (String level: data.getLevels(category)) {
//					for (int period: periodsToAggregateOver) {
//						header.append("A").append(nthClosestArea).append(category).append("_").append(level).append("_").append(period).append(",");
//					}
//				}
//			}
//		//}
//		header.append("area").append(",");
//		header.append("target");
//		writer.write(header.toString());
//		writer.newLine();
	}
	
	public int getReportFrequency() {
		return reportFrequency;
	}

	public void setReportFrequency(int reportFrequency) {
		this.reportFrequency = reportFrequency;
	}

	
	
	
	

}
