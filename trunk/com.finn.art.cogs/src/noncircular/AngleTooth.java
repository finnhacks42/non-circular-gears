package noncircular;

import geomerative.RPoint;

import java.util.ArrayList;
import java.util.List;

public class AngleTooth implements ToothProfile {
	private float toothTipPercentage;
	
	/*** The tooth tip percentage determines how steeply the angles come in towards the tip of the tooth. 
	 * If it is one then the sides are square and the tooth has the maximum tooth width at its tip.
	 * If it is zero then the tooth will be triangular (with 0 width at its tip).
	 * @param toothTipPercentage a float between 0 and 1.
	 */
	public AngleTooth(float toothTipPercentage) {
		this.toothTipPercentage = toothTipPercentage;
	}
	

	@Override
	public List<RPoint> getProfile(RPoint start, RPoint guide1, RPoint guide2, RPoint end) {
		//adds two points along the line from guide1 to guide2
		ArrayList<RPoint> result = new ArrayList<RPoint>(2);
		float p1 = (1 - toothTipPercentage) / 2;
		float p2 = p1+toothTipPercentage;
		result.add(Vector.getPoint(p1, guide1.x, guide1.y, guide2.x, guide2.y));
		result.add(Vector.getPoint(p2, guide1.x, guide1.y, guide2.x, guide2.y));
		return result;
	}
	
	

}
