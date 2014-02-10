package gridfeatures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GridDistanceNetwork {
	/*** Creates a network of the distances between areas under the assumption that their IDs represent cellIDs.
	 * The cells area assumed to begin at 0 in the top left corner and increase left to right and then top-bottom.
	 * @param gridWidth the number of squares the grid is wide.
	 */
	public void createAreaNetworkFromGrid( int gridWidth) {
		Map<Integer,List<Distance>> result = new HashMap<Integer,List<Distance>>();
		for (int cellID: areas) {
			List<Distance> distances = new ArrayList<Distance>();
			for (int cellID2: areas) {
				int i = cellID % gridWidth;
				int j = cellID/gridWidth;
				int i2 = cellID2 % gridWidth;
				int j2 = cellID2/gridWidth;
				double dist = Math.sqrt(Math.pow(i - i2, 2)+Math.pow(j - j2,2));
				distances.add(new Distance(cellID2, dist));
			}
			Collections.sort(distances);
			result.put(cellID, distances);
		}
		buildNetwork(result);
	}

	/*** Creates a network of the distances between areas based on the centroids (median lat/long) of the crime events recorded in that area.
	 * This method will not include areas with 0 crime in the network because no centroid can be found. ***/
	public void createAreaNetworkFromAreaCentroids(Map<Integer, GeoPoint> centroids){
		Map<Integer,List<Distance>> result = new HashMap<Integer,List<Distance>>();

		for (Entry<Integer,GeoPoint> area: centroids.entrySet()) {
			List<Distance> distances = new ArrayList<Distance>();
			for (Entry<Integer,GeoPoint> otherarea: centroids.entrySet()) {
				if (area.getKey() == otherarea.getKey()) {
					distances.add(new Distance(area.getKey(),0));
				} else {
					double distance = area.getValue().distance(otherarea.getValue());
					distances.add(new Distance(otherarea.getKey(), distance));
				}
			}
			Collections.sort(distances);
			result.put(area.getKey(), distances);	
		}
		buildNetwork(result);
		
	}
	
	/*** Takes a map from the areaID to an ordered list of distances and creates the map from areaID to list of IDs ***/
	private void buildNetwork(Map<Integer,List<Distance>> input) {
		this.areaNetwork = new HashMap<Integer, List<Integer>>();
		for (Entry<Integer,List<Distance>> entry: input.entrySet()) {
			List<Integer> otherAreas = new ArrayList<Integer>();
			for (Distance d: entry.getValue()) {
				otherAreas.add(d.getArea());
			}
			areaNetwork.put(entry.getKey(), otherAreas);
		}
	}
}
