package test;

import static org.junit.Assert.*;
import gridfeatures.DataLoader;
import gridfeatures.LocationHierachy;

import java.util.Arrays;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

public class LocationHierachyTest {

	private static LocationHierachy h;
	private static final int EXTRA_NAMESPACES = 2;
	private static final int[] AREAS = {10,11,12,13,14,15,16,17,18,19};
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		h = LocationHierachy.buildFromAreaDigits(AREAS, EXTRA_NAMESPACES);
	}

	@Test
	public void testDepth() {
		assertEquals(EXTRA_NAMESPACES+1,h.depth());
	}

	@Test
	public void testGetNameSpaces() {
		Collection<String> names = h.getNameSpaces();
		assertEquals(EXTRA_NAMESPACES + 1,names.size());
		assertTrue(names.contains(DataLoader.AREA));
		for (int n = 0; n < EXTRA_NAMESPACES; n++) {
			assertTrue(names.contains(DataLoader.AREA_PREFIX+n+"th"));
		}
	}

	@Test
	public void testSize() {
		assertEquals(AREAS.length,h.size(DataLoader.AREA));
		assertEquals(1,h.size(DataLoader.AREA_PREFIX+"0th"));
		assertEquals(10,h.size(DataLoader.AREA_PREFIX+"1th"));
	}

	@Test
	public void testGetAreaIDs() {
		Collection<Integer> baseAreas = h.getAreaIDs(DataLoader.AREA);
		assertEquals(AREAS.length,baseAreas.size());
		for (int area: AREAS) {
			assertTrue(baseAreas.contains(area));
		}
		Collection<Integer> zeroth = h.getAreaIDs(DataLoader.AREA_PREFIX+"0th");
		assertEquals(1,zeroth.size());
		assertTrue(zeroth.contains(1));
	}

	@Test
	public void testGetTargetAreaParent() {
		for (int a: AREAS) {
			int parent = h.getTargetAreaParent(DataLoader.AREA, a);
			assertEquals(a, parent);
			String chars = Integer.toString(a);
			for (int n = 0; n < EXTRA_NAMESPACES; n++) {
				int expectedParent = Integer.valueOf(chars.substring(n, n+1));
				int actual = h.getTargetAreaParent(DataLoader.AREA_PREFIX+n+"th", a);
				assertEquals(expectedParent, actual);
			}	
		}
		
		
	}

}
