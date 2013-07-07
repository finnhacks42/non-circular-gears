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
}
