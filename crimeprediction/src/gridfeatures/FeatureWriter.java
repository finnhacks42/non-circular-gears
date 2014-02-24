package gridfeatures;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	public static final int FORMAT_VW = 1;
	public static final int FORMAT_MATRIXMARKET = 2;
	public static final int FORMAT_ROW = 3;
	public static final String[] DATA_PARTITIONS = {"train","valid","test"};
	public static final String TEST = "test";
	public static final String TRAIN = "train";
	public static final String VALIDATE = "valid";

	private DataI data;
	private int numFeatures;
	private int numInstances;
	private int[] periodsToAggregateOver;
	private  int furthestBack = 0;
	private String targetCategoryName;
	private String targetCategoryValue;
	private String smallestCategoryName;
	private int periodsPerInstance;
	private int totalCategoryLevels;
	private int reportFrequency = -1;
	private int instanceTimewindows;
	private boolean labelArea = true;
	private Map<Integer,Integer> areaToAreaID = new HashMap<Integer,Integer>(); //maps the areas to a unique id between 1 and num_areas
	
	
	/*** 
	 * 
	 * @param data
	 * @param periodsBack the periods to aggregate counts over when generating features
	 * @param numNeighbours 
	 * @param targetCategoryName what category is the target based on. If this is null then the target is assumed to be the total count across all categories
	 * @param targetCategoryValue what level of the specified category is the target based on. Ignored if targetCategoryName is null.
	 * @param periodsPerInstance the number of time periods that should be grouped together to create each instance.
	 * @throws InvalidDataStoreException 
	 */
	public FeatureWriter(DataI data, int[] periodsBack, String targetCategoryName, String targetCategoryValue, int periodsPerInstance, boolean labelArea) throws InvalidDataStoreException {
		this.data = data;
		data.validate();
		this.labelArea = labelArea;
		this.periodsToAggregateOver = periodsBack;
		if (periodsToAggregateOver.length > 0) {
			this.furthestBack = periodsToAggregateOver[periodsToAggregateOver.length - 1];
		}
		this.targetCategoryName = targetCategoryName;
		this.targetCategoryValue = targetCategoryValue;
		this.periodsPerInstance = periodsPerInstance;
		
		int id = 1;
		for (Integer area: data.getAreas()) {
			areaToAreaID.put(area, id);
			id ++;
		}
		
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
	 * 
	 * @param trainPercentage percentage of the instanceTimewindows that should be in the training data
	 * @param validatePercentage percentage of the instanceTimewindows that should be in the validation data
	 * @param testPercentage percentage of the instanceTimewindows that should be used for the test data
	 * @param path
	 * @param filename
	 * @throws IOException
	 */
	public void write(double trainPercentage, double validatePercentage, String path, String filename, int format) throws IOException {
		if (trainPercentage + validatePercentage > 1) {
			throw new IllegalArgumentException("train + validation percentage must be <= 1");
		}
		BufferedWriter[] writers = new BufferedWriter[3];
		for (int indx = 0; indx < 3; indx ++) {
			writers[indx] = new BufferedWriter(new FileWriter(path+filename+DATA_PARTITIONS[indx]));
		}
		int[] boundaries = {(int)Math.floor(trainPercentage*instanceTimewindows), (int)Math.floor((trainPercentage+validatePercentage)*instanceTimewindows),instanceTimewindows};
		
		Instance instance = null;
		if (FORMAT_VW == format) {
			instance = new VWInstance();
		} else if (FORMAT_MATRIXMARKET == format) {
			instance = new MatrixMarketInstance();
		} else if (FORMAT_ROW == format) {
			instance = rowInstance();
		} else {
			throw new IllegalArgumentException("Unknown format requested");
		}
		
		
		int numColumns = labelArea ? data.getAreas().size() + numFeatures + 1: numFeatures + 1; //note +1 is for the target variable
		
		int count = 0;
		int windowsCount = 0;
		int dataPartion = 0;
		BufferedWriter writer = writers[dataPartion];
		int periodBoundary = boundaries[dataPartion];
		instance.setWriter(writer,numColumns);
		float targetTotal = 0f;
		for (int period = furthestBack; period <= data.getNumPeroids()-periodsPerInstance; period += periodsPerInstance) {
			if (windowsCount >= periodBoundary) {
				dataPartion ++;
				writer = writers[dataPartion];
				periodBoundary = boundaries[dataPartion];
				instance.finishWriter();
				instance.setWriter(writer,numColumns);
			}
			for (int area: data.getAreas()) {
				float target = calculateTarget(period, area);
				targetTotal += target;
				instance.setTarget(target);
				buildAndWriteFeatures(period,area,instance,writer,labelArea);
				instance.endInstanceAndclear();
				count ++;
				if (reportFrequency > 0 && count % reportFrequency == 0) {
					System.out.println(count);
				}
			}
			windowsCount ++;
		}
		instance.finishWriter();
		//System.out.println("windows count:" +windowsCount+", instances:"+count);
		for (Writer w: writers) {
			w.close();
		}
		System.out.println("Target total:"+targetTotal);
	}
	
	
	private RowInstance rowInstance() {
		String[] header = null;
	
		int indx = 0;
		if (labelArea) {
			header = new String[numFeatures + 2];
			header[1] = "area";
			indx = 2;
		} else {
			header = new String[numFeatures + 1];
			indx = 1;
		}
		header[0]="target";
		LocationHierachy hierachy = data.getHierachy();
		
		for (String areaAggregation: hierachy.getNameSpaces()) {
			for (String category:data.getCategories()) {
				for (String level: data.getLevels(category)) {
					for (int period: periodsToAggregateOver) {
						String columnName = areaAggregation+category+level+period;
						header[indx] = columnName;
						indx ++;
					}
				}
				
			}
		}
		
		return new RowInstance(header,this.labelArea);
	}
	
	
	/*** build features for the specific instance specified by a period area combination. 
	 * @throws IOException ***/
	private void buildAndWriteFeatures(int period, int targetArea, Instance instance, BufferedWriter writer, boolean labelArea) throws IOException{
		LocationHierachy hierachy = data.getHierachy();	
		int featureID = 1;
		if (labelArea) {
			if (labelArea) {
				instance.setNamespace(DataLoader.AREA);
				int areaID = areaToAreaID.get(targetArea);
				instance.addFeature(areaID, 1);
			}
			featureID += areaToAreaID.size();
		}
		
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
	
	
	
	public int getReportFrequency() {
		return reportFrequency;
	}

	public void setReportFrequency(int reportFrequency) {
		this.reportFrequency = reportFrequency;
	}

	
	
	
	

}
