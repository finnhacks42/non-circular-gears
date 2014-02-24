package gridfeatures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleDataTest {
	private static SimpleData data;
	private static List<Integer> areas;
	private static int numPeriods;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		 areas = new ArrayList<Integer>();
		 areas.add(123);
		 numPeriods = 1;
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		data = null;
		areas = null;
	}

	@Before
	public void setUp() throws Exception {
		data = new SimpleData(numPeriods, areas);
		Set<String> oneslevels = new HashSet<String>();
		oneslevels.add("1");
		Set<String> crimelevels = new HashSet<String>();
		crimelevels.add("burglary");
		crimelevels.add("theft");
		data.setLevels("ones", oneslevels);
		data.setLevels("crime", crimelevels);
		data.getHierachy().add(123, 2, "2nd");
		data.getHierachy().add(123, 3, "3rd");
		data.validate();
	}


	
	
	@Test
	public void testIncrementCount() {
		for (String category: data.getCategories()) {
			for (String level: data.getLevels(category)) {
				for (String areaNS: data.getHierachy().getNameSpaces()) {
					for (int areaID: data.getHierachy().getAreaIDs(areaNS)) {
						CrimeKey key = new CrimeKey(areaNS, areaID, category, level);
						data.incrementCount(key, 1);
						int count = data.getCount(key, 1);
						assertEquals(1, count);
					}
				}
			}
		}
	}
	
	
	private void incrementAllCounts(int[] days) {
		for (int area: data.getAreas()) {
			for (String category: data.getCategories()) {
				for (String level: data.getLevels(category)) {
					CrimeKey key = new CrimeKey(DataLoader.AREA, area, category, level);
					for (int d: days) {
						data.incrementCount(key, d);
					}
				}
			}
		}	
	}

	@Test
	public void testCalculateCounts() {
		data.calculateCounts(key, day, daysback)
	}

	@Test
	public void testCalculateNormalizedCounts() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCount() {
		for (String category: data.getCategories()) {
			for (String level: data.getLevels(category)) {
				for (String areaNS: data.getHierachy().getNameSpaces()) {
					for (int areaID: data.getHierachy().getAreaIDs(areaNS)) {
						CrimeKey key = new CrimeKey(areaNS, areaID, category, level);
						int count = data.getCount(key, 1);
						assertEquals(0, count);
					}
				}
			}
		}
		
		
	}

}
