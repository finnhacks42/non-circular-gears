package noncircular;

import geomerative.RPoint;

import java.util.ArrayList;
import java.util.List;

/*** represents a tooth profile that is square at the bottom and angled at the top. ***/
public class SquareAndAngleTooth implements ToothProfile {
	private float toothThicknessPercentage;
	private float squarePercentage;
	
	public SquareAndAngleTooth(float toothThicknessPercent, float squarePercent) {
		this.toothThicknessPercentage =toothThicknessPercent;
		this.squarePercentage = squarePercent;
	}
	
	@Override
	public List<RPoint> getProfile(RPoint start, RPoint guide1, RPoint guide2,	RPoint end) {
		List<RPoint> result = new ArrayList<RPoint>();
		result.add(Vector.getPoint(squarePercentage, start.x, start.y, guide1.x, guide1.y));
		float p1 = (1 - toothThicknessPercentage) / 2f;
		float p2 = p1 + toothThicknessPercentage;
		result.add(Vector.getPoint(p1, guide1.x, guide1.y, guide2.x, guide2.y));
		result.add(Vector.getPoint(p2, guide1.x, guide1.y, guide2.x, guide2.y));
		result.add(Vector.getPoint((1- squarePercentage), guide2.x, guide2.y, end.x, end.y));
		return result;
	}

}
