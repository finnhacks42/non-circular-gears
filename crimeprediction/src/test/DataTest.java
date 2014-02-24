package test;

import static org.junit.Assert.*;
import gridfeatures.CrimeKey;
import gridfeatures.Data;
import gridfeatures.DataI;
import gridfeatures.DataLoader;
import gridfeatures.LocationHierachy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

public class DataTest {
	private static int[] areas = {112,113,121,212};
	private static int numPeriods = 10;
	private static DataI data; 
	static CrimeKey key112burglary = new CrimeKey(DataLoader.AREA, 112, "crime", "burglary");
	static CrimeKey key112other = new CrimeKey(DataLoader.AREA, 112, "crime", "other");
	static CrimeKey key112any = new CrimeKey(DataLoader.AREA, 112, "ones", "1");
	
	static CrimeKey key121burglary = new CrimeKey(DataLoader.AREA, 121, "crime", "burglary");
	static CrimeKey key0th1burglary = new CrimeKey("area0th",1,"crime","burglary");
	
	static CrimeKey key113any = new CrimeKey(DataLoader.AREA,113,"ones","1");
	
	static CrimeKey key212burg = new CrimeKey(DataLoader.AREA,212,"crime","burglary");
	

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		data = new Data();
		
		DataLoader loader = new DataLoader();
		LocationHierachy hierachy = LocationHierachy.buildFromAreaDigits(areas, 2);
		String[] cats = {"ones","crime"};
		DataGenerator gen = new DataGenerator(hierachy, cats);
		
		String[] header = gen.generateHeader();
		List<String[]> rows = new ArrayList<String[]>();
		Map<String,String> burglary = new HashMap<String,String>();
		burglary.put("ones", "1");
		burglary.put("crime", "burglary");
		Map<String,String> other = new HashMap<String,String>();
		other.put("ones", "1");
		other.put("crime", "other");
		System.out.println(hierachy.getNameSpaces());
		System.out.println(Arrays.toString(header));
		
		for (int p = 0; p < 10; p ++) {
			if (p != 4) { // no events on day 4
				if (p % 2 == 0) {
					String[] row = gen.generateEvent(112, p, burglary);
					rows.add(row);
				} else {
					String[] row = gen.generateEvent(112, p, other);
					rows.add(row);
				}
				String[] row = gen.generateEvent(121, p, burglary); // a burglary every day except day 4 in area 121.
				rows.add(row);
				for (int i = 0; i < p; i ++) {
					row = gen.generateEvent(212, p, burglary);
					rows.add(row);
				}
			}
		}
		loader.load(rows, header, areas, numPeriods, data);
		
		
	}
	
	@Test
	public void testGetCount() {
		for (int p = 0; p < numPeriods; p ++) {
			assertEquals(0, data.getCount(key113any, p));
			
			if (p == 4) {
				assertEquals(0, data.getCount(key112burglary, p));
				assertEquals(0, data.getCount(key112any, p));
			} else {
				assertEquals(1, data.getCount(key121burglary, p));
				assertEquals(p, data.getCount(key212burg, p));
				if (p%2 == 0) {
					assertEquals(1, data.getCount(key112burglary, p));
					assertEquals(2,data.getCount(key0th1burglary, p)); 
					assertEquals(0, data.getCount(key112other, p));
					assertEquals(1, data.getCount(key112any, p));
				} else {
					assertEquals(0, data.getCount(key112burglary, p));
					assertEquals(1,data.getCount(key0th1burglary, p));
					assertEquals(1, data.getCount(key112other, p));
					assertEquals(1, data.getCount(key112any, p));
				}	
			}
		}	
	}

	@Test
	public void testCalculateCounts() {
		int[] daysback = {1,10};
		assertArrayEquals(new int[] {9,41}, data.calculateCounts(key212burg, 10, daysback)); //one burglary every day except day 4
		assertArrayEquals(new int[] {0,0}, data.calculateCounts(key113any, 10, daysback));	
	}

	@Test
	public void testCalculateNormalizedCounts() {
		int maxdb = 5;
		int[] daysback = {1,3,maxdb};
		
		for (String areaNS: data.getHierachy().getNameSpaces()) {
			for (Integer areaID: data.getHierachy().getAreaIDs(areaNS)) {
				for (String category: data.getCategories()) {
					for (String level: data.getLevels(category)) {
						CrimeKey key = new CrimeKey(areaNS, areaID, category, level);
						for (int day = data.getNumPeroids() - maxdb; day <= data.getNumPeroids(); day ++){
							int[] counts = data.calculateCounts(key, day, daysback);
							float[] normed = data.calculateNormalizedCounts(key, day, daysback);
							assertEquals(counts.length, normed.length);
							for (int i = 0; i < counts.length; i ++) {
								assertEquals(counts[i]/(float)daysback[i], normed[i],0.0000001);
							}
						}
					}
				}
			}
		}
		
	}

	

}
