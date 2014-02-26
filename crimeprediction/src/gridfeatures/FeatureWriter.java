package gridfeatures;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


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
	
	private String smallestCategoryName;
	private int periodsPerInstance;
	private int totalCategoryLevels;
	private int reportFrequency = -1;
	private int instanceTimewindows;
	//private boolean labelArea = true;
	private Map<String, Integer> areasToIDs;
	private String[] areasToLabel;
	
	
	/*** relables each area with an id that is unique accross all namespaces such that it can be used as a feature ID
	 * 
	 * @param areaNS
	 * @param areaID
	 * @return an id unique across all labled area namespaces, such that it can be used as a featureID. Ranges from 1 to number of areas to be labeled in total.
	 */
	private int getAreaID(String areaNS, int areaID) {
		String key = areaNS+areaID;
		return areasToIDs.get(key);
	}
	
	private Map<String,Integer> buildAreaIDs(String[] areasToLabel) {
		Map<String,Integer> result = new HashMap<String,Integer>();
		int id = 1;
		for (String namespace: areasToLabel) {
			for (int area: data.getHierachy().getAreaIDs(namespace)){
				String key = namespace+area;
				result.put(key, id);
				id ++;
			}
		}
		return result;
	}
	
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
	public FeatureWriter(DataI data, int[] periodsBack, int periodsPerInstance, String[] areasToLabel) throws InvalidDataStoreException {
		this.data = data;
		data.validate();
		
		this.areasToLabel = areasToLabel;
		this.periodsToAggregateOver = periodsBack;
		if (periodsToAggregateOver.length > 0) {
			this.furthestBack = periodsToAggregateOver[periodsToAggregateOver.length - 1];
		}
		
		this.periodsPerInstance = periodsPerInstance;
		validatePeriodsBack(periodsBack);
		
		validateAreasToLabel(areasToLabel);
		this.areasToIDs = buildAreaIDs(areasToLabel);
		
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
		numInstances = instanceTimewindows * data.getHierachy().size(DataLoader.AREA);
		
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
		b.append("First period:").append(furthestBack);
		b.append(", Input periods:").append(data.getNumPeroids());
		b.append(", Output periods:").append(data.getNumPeroids()-furthestBack);
		b.append(", Periods/instance:").append(periodsPerInstance);
		b.append("\n");
		b.append("Target Areas:").append(data.getHierachy().size(DataLoader.AREA));
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
		return data.getTarget(targetArea, period);
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
		BufferedWriter[] targetWriters = new BufferedWriter[3];
		for (int indx = 0; indx < 3; indx ++) {
			writers[indx] = new BufferedWriter(new FileWriter(path+filename+DATA_PARTITIONS[indx]));
			targetWriters[indx] = new BufferedWriter(new FileWriter(path+filename+DATA_PARTITIONS[indx]+".target"));
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
		
		int numColumns = areasToIDs.size() + numFeatures + 1; // note + 1 is for target variable, this is the number of columns that would exist if each area under each namespace is coded via the dummy variable method
		
		
		int count = 0;
		int windowsCount = 0;
		int dataPartion = 0;
		BufferedWriter writer = writers[dataPartion];
		BufferedWriter targetWriter = targetWriters[dataPartion];
		int periodBoundary = boundaries[dataPartion];
		instance.setWriter(writer,numColumns);
		float targetTotal = 0f;
		for (int period = furthestBack; period <= data.getNumPeroids()-periodsPerInstance; period += periodsPerInstance) {
			if (windowsCount >= periodBoundary) {
				dataPartion ++;
				writer = writers[dataPartion];
				targetWriter = targetWriters[dataPartion];
				periodBoundary = boundaries[dataPartion];
				instance.finishWriter();
				instance.setWriter(writer,numColumns);
			}
			for (int area: data.getHierachy().getAreaIDs(DataLoader.AREA)) {
				float target = calculateTarget(period, area);
				targetWriter.write(Float.toString(target));
				targetWriter.newLine();
				targetTotal += target;
				instance.setTarget(target);
				buildAndWriteFeatures(period,area,instance,writer);
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
		String[] header = new String[areasToLabel.length+numFeatures+1];
		header[0]="target";
		int indx = 1;
		for (String areaNS: areasToLabel) {
			header[indx] = areaNS;
		}
			
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
		
		return new RowInstance(header,areasToLabel);
	}
	
	
	private void validateAreasToLabel(String[] areasToLabel){
		if (areasToLabel == null) {throw new IllegalArgumentException("Areas to label must be specified. If no area labling is desired, pass an empty string");}
		for (String areaNS: areasToLabel) {
			if (!data.getHierachy().getNameSpaces().contains(areaNS)){
				throw new IllegalArgumentException("Unknown area namespace: "+areaNS);
			}
		}
	}
	
	/*** build features for the specific instance specified by a period area combination. 
	 * @throws IOException ***/
	private void buildAndWriteFeatures(int period, int targetArea, Instance instance, BufferedWriter writer) throws IOException{
		LocationHierachy hierachy = data.getHierachy();	
		System.out.println(hierachy.getNameSpaces());
		
		
		int featureID = 1;
		
		for (String areaNS: areasToLabel) {
			instance.setNamespace(areaNS);
			Integer area = hierachy.getTargetAreaParent(areaNS, targetArea);
			if (area == null) { // this will be true if we have not seen the area in the data set - so we won't know what parent it should have
				Collection<Integer> idsFound = hierachy.getAreaIDs(areaNS);
				System.out.println(hierachy.getAreaIDs(areaNS).contains(targetArea));
				System.out.println(idsFound.size());
				for (Integer id: idsFound) {
					if (id - targetArea < 10) {
						System.out.println("Found:"+id);
					}
				}
				throw new IllegalStateException("Area:"+targetArea+" not found within namespace:"+areaNS);
			}
			int areaID = getAreaID(areaNS, area);
			instance.addFeature(areaID, 1);
		}
		
		featureID += areasToIDs.size();
		
		
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
	
	
	
	
	
	
	public int getReportFrequency() {
		return reportFrequency;
	}

	public void setReportFrequency(int reportFrequency) {
		this.reportFrequency = reportFrequency;
	}

	
	
	
	

}
