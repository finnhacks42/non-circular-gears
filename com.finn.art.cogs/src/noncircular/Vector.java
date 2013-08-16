package noncircular;
import java.util.ArrayList;
import java.util.List;

import geomerative.RPoint;
import static processing.core.PApplet.pow;
import static processing.core.PApplet.sqrt;
import static processing.core.PApplet.cos;
import static processing.core.PApplet.sin;
import static processing.core.PApplet.PI;


/*** A class to contain useful static methods for manipulating vectors. ***/
public class Vector {

	
	/*** Gets the unit norm vector to a surface given the tangent vector at that point.
	 * So to draw the unit normal you would draw a line from x,y to x+n.x,y+n.y. ***/
	public static final RPoint getUnitNorm(RPoint tangent) {
		tangent.normalize();
		return new RPoint(tangent.y,-tangent.x);
	}
	
	/*** create an RPoint vector at the specified radius and angle from the input origin. ***/
	public static RPoint radialPoint(double radius, double angle, RPoint origin) {
		RPoint p = new RPoint(radius*Math.cos(angle) + origin.x, radius*Math.sin(angle) + origin.y);
		return p;
	}
	
	/*** Create an array of angles of length nPoints, equally spaced from 0 to 2PI. ***/
	public static List<Float> createEvenAngles(int nPoints) {
		List<Float> result = new ArrayList<Float>(nPoints);
		float theta = 0;
		float stepSize = PI*2/((float)nPoints);
		for (int i = 0; i < nPoints; i++) {
			result.add(theta);
			theta += stepSize;
		}
		return result;
	}
	
	/*** return a point the specified percentage along the input line. ***/
	public static RPoint getPoint(float percentage, float x1, float y1, float x2, float y2){
		float dx = (x2 - x1);
		float dy = (y2 - y1);
		float dx2 = percentage*dx;
		float dy2 = percentage*dy;
		return new RPoint(x1+dx2, y1+dy2);
	}
	
	/*** returns true if p1 is clockwise from p2 otherwise false. ***/
	public static boolean clockwise(RPoint p1, RPoint p2) {
		float theta1 = (float) Math.atan2(p1.y, p1.x); //a number between -PI and PI
		float theta2 = (float) Math.atan2(p2.y, p2.x);
		float diff = theta2 - theta1;
		float diffMag = Math.abs(diff);
		if (diffMag >= PI) {
			diff = - diff;
		}
		if (diff < 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public static float angle(RPoint p1, RPoint p2) {
		float theta1 = (float) Math.atan2(p1.y, p1.x); //a number between -PI and PI
		float theta2 = (float) Math.atan2(p2.y, p2.x);
		float diff = theta2 - theta1;
		if (diff <0) {diff += 2*PI;} 
		return diff;
	}
}
