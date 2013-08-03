package noncircular;

import geomerative.RPoint;

import java.util.ArrayList;
import java.util.List;



public class SquareTooth implements ToothProfile {

	@Override
	public List<RPoint> getProfile(RPoint start, RPoint guide1, RPoint guide2,	RPoint end) {
		ArrayList<RPoint> result = new ArrayList<RPoint>(2);
		result.add(guide1);
		result.add(guide2);
		return result;
	}

}
