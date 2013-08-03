package noncircular;

import geomerative.RPoint;

import java.util.ArrayList;
import java.util.List;
import static processing.core.PApplet.sqrt;
import static processing.core.PApplet.pow;


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
		//we want to add two points along the line from guide1 to guide2
		ArrayList<RPoint> result = new ArrayList<RPoint>(2);
		float dx = (guide2.x - guide1.x);
		float dy = (guide2.y - guide1.y);
		float slope = dy/dx;
		float length = sqrt(pow(dx,2) + pow(dy,2));
		float l1 = length*(1 - toothTipPercentage)/2f;
		float l2 = l1 + length*toothTipPercentage;
		
		float x1 = sqrt(pow(l1,2)/(1+pow(slope,2)));
		float y1 = slope*x1;
		result.add(new RPoint(x1,y1));
		
		float x2 = sqrt(pow(l2,2)/(1+pow(slope,2)));
		float y2 = slope*x2;
		result.add(new RPoint(x2,y2));
		return result;
	}
	
	

}
