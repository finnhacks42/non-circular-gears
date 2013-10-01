package feature;

import java.util.List;
import java.util.Map.Entry;

import model.Crime;
import model.GeoPoint;

public class FeatureBuilder {
	private Data data;
		
	
	public FeatureBuilder(Data data) {
		this.data = data;
	}
	// crime of category X at distance D over period T
	
	
	
	
	public void buildFeatures(Data data) {
		for (String area: data.getAreas()) {
			
		}	
	}
	
	/*** build the feature set for a given area. ***/
	private void buildFeatures(Data data, String area) {
		for (Crime crime: data.getCrimes(area)){
			// find the number of crimes of category X over most recent T days in areas with centroids within distance D
		}
	}
	
	
	private void countCrimes(List<String> areas){
		//try not to recalculate stuff you have already figured out...
		// calculate totals by category and by period
		for (String area : areas) {
			
		}
		
	}

}
