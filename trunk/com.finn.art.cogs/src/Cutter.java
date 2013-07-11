import static processing.core.PApplet.sin;
import static processing.core.PConstants.PI;
import processing.core.PApplet;
import geomerative.RG;
import geomerative.RPoint;
import geomerative.RShape;


public class Cutter {
	
	private RShape shape;
	private float radius;
	private PApplet app;
	
	/*** Create a tooth cutting tool 
	 * 
	 * @param curveLength the length of the curve around the gear to be cut
	 * @param cutToCurveRatio the number of times the cutter should rotate before returning to its start point.
	 * @param nTeeth the number of teeth on the cutter
	 * @param toothAmp the amplitude (or length) of the teeth
	 * @param resolution the resolution of the cutter. 
	 * @param app
	 */
	public Cutter(float curveLength, int cutToCurveRatio, int nTeeth, float toothAmp, int resolution, PApplet app) {
		this.app = app;
		radius = curveLength/(2*PI*cutToCurveRatio);
		shape = new RShape();
		float theta = 0;
		float theta2 = 0;
		float dtheta = 2*PI/(float)resolution;
		float dtheta2 = 2*PI*nTeeth/(float)resolution;
		RPoint origin = new RPoint(0, 0);
		float r = radius;
		shape.addMoveTo(r, 0);
		for (int i = 1; i < resolution; i++) {
			r = radius + toothAmp*sin(theta2);
			RPoint p = Vector.radialPoint(r, theta, origin);
			shape.addLineTo(p);
			theta += dtheta;
			theta2 += dtheta2;
		}
		shape.addClose();
	}
	
	public float getRadius(){
		return radius;
	}
	
	public RShape getShape(){
		return shape;
	}
	
	public void draw() {
		RG.shape(shape);
		RPoint centroid = shape.getCentroid();
		app.ellipse(centroid.x,centroid.y,radius*2,radius*2);
	}

}
