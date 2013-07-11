import static processing.core.PApplet.cos;
import static processing.core.PApplet.sin;
import static processing.core.PConstants.PI;
import geomerative.RG;
import geomerative.RMatrix;
import geomerative.RPoint;
import geomerative.RShape;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;



import processing.core.PApplet;

/*** This class represents a drawable non-linear gear. ***/
public class GearShape {

	private PApplet app;
	private float centerRadius = 10;
	private int angleIndxResolution = 10000;
	
	private List<Float> angles;
	private List<Float> radii;
	private List<RPoint> norms;
	private List<RPoint> points; //this duplicates infomation held by radii and angle but makes calculations quicker.
	private RShape shape; 
	private int[] angleIndx;
	private float x = 0;
	private float y = 0;
	
	/*** Create a new gear with the specified radial points at the corresponding angles.
	 * 
	 * @param radialFunction the points
	 * @param angles the angles, must contain the same number of elements as the points
	 * @param app
	 */
	public GearShape(PApplet app) {
		this.app = app;
	}
	
	/*** set the profile from an svg drawing. ***/
	public void setProfile(File svgDrawing, int resolution){
		RShape s = RG.loadShape(svgDrawing.getAbsolutePath());
		RPoint center = s.getCenter();
		s.translate(-center.x,-center.y);
		List<Float> radii = new ArrayList<Float>();
		List<Float> angles = new ArrayList<Float>();
		for (int i = 0; i < resolution; i++) {
			float adv = i/(float)resolution;
			RPoint p = s.getPoint(adv);
			float r = p.norm();
			float angle = (float) Math.atan2(p.y, p.x);
			if (angle < 0) {
				angle += 2*PI;
			}
			radii.add(r);
			angles.add(angle);
		}
		setProfile(radii, angles);
	}
	
	/*** set the profile of the gear from a list of radii. The radii are assumed to be at equally spaced angles. ***/
	public void setProfile(List<Float> radialFunction) {
		List<Float> angles = Vector.createEvenAngles(radialFunction.size());
		setProfile(radialFunction,angles);
	}
	
	/*** Creates an RShape based on the input radii at the specified angles. ***/
	private RShape buildShape(List<Float> radialFunction, List<Float> angles) {
		RShape result = new RShape();
		if (radialFunction.size() > 0) {
			float r = radialFunction.get(0);
			float theta = angles.get(0);
			result.addMoveTo(r*cos(theta), r*sin(theta));
			for (int i = 1; i < angles.size(); i++) {
				r = radialFunction.get(i);
				theta = angles.get(i);
				result.addLineTo(r*cos(theta),r*sin(theta));
			}
			result.addClose();
		}
		return result;
	}
	
	/*** From a shape, pre-calculate lists of points, radii, angles and norms. 
	 * The points are sampled at equal points along the arc of the shape NOT at equal angles. 
	 * @param shape the shape to sample
	 * @param nPoints the number of points to sample from the shape
	 */
	private void resampleDataFromShape(RShape shape, int nPoints){
		this.points = new ArrayList<RPoint>(nPoints);
		this.norms = new ArrayList<RPoint>(nPoints);
		this.angles = new ArrayList<Float>(nPoints);
		this.radii = new ArrayList<Float>(nPoints);

		for (int i = 0; i < nPoints; i ++) {
			float adv = i/(float)nPoints;
			RPoint tangent = shape.getTangent(adv);
			RPoint norm = Vector.getUnitNorm(tangent);
			RPoint point = shape.getPoint(adv);
			//float angle = point.angle(xAxis);
			float angle = (float) Math.atan2(point.y,point.x);
			if (angle < 0) {angle += 2*PI;}

			float radius = point.norm();
			this.angles.add(angle);
			radii.add(radius);
			points.add(point);
			norms.add(norm);
		}

		buildAngleIndx(angleIndxResolution);
		testEquivelence();
	}
	
	
	public void cutTeeth() {
		int numTeeth = 20;
		float toothWidth = shape.getCurveLength()/(2f*numTeeth);
		float toothGap = toothWidth;
		float pointsPerUnitCurve = shape.getCurveLength()/radii.size();
		int pointsPerTooth = Math.round(pointsPerUnitCurve*toothWidth);
		float toothDepth = 10;
		
		RPoint norm = norms.get(0);
		boolean cut = true;
		int toothPoint = 0;
		for (int indx = 0; indx < radii.size(); indx ++) {
			if (indx % pointsPerTooth == 0) {
				cut = !cut;
				norm = norms.get(indx);
			}
			if (cut) {
				//shift the radius inwards by the tooth depth in the direction of the current norm
				float currentRadius = radii.get(indx);
				RPoint r = points.get(indx);
				float angle = r.angle(norm);
				float adjust = toothDepth*cos(angle);
				radii.set(indx, currentRadius - adjust);
			}
		
			
		}
		
		
		
		
		
		
	}
	
	
	/*** set the profile of the gear. ***/
	public void setProfile(List<Float> radialFunction, List<Float> angles) {
		if (radialFunction.size() != angles.size()) {
			throw new IllegalArgumentException("number of angles must match number of radii");
		}
		
		this.shape = buildShape(radialFunction, angles);
		resampleDataFromShape(shape, angles.size());
	}
	
	private void buildAngleIndx(int numSteps) {
		angleIndx = new int[numSteps];
		int numAngles = angles.size();
		int j = 0;
		for (int i = 0; i < numSteps; i ++) {
			float currentAngle  = angles.get(j);
			float nextAngle = angles.get((j+1)%numAngles);
			float ourAngle = 2*PI*i/numSteps;
			if (nextAngle < currentAngle) {
				nextAngle += 2*PI;
			}
			if (ourAngle > (currentAngle + nextAngle)/2f){
				j ++;
			}
			if (j >= angles.size()) {
				j = 0;
			}
			angleIndx[i] = j;
		}
	}
	
	
	public int getRadialIndx(float angle) {
		return angleIndx[(Math.round(angle*angleIndx.length/(2*PI))+angleIndx.length)%angleIndx.length];
	}
	
	
	/*** Create a circular gear modified by a sin wave. 
	 * The radial function is of the form R = A + B*sin(C*t). 
	 * @A the minimum radius
	 * @B the variable radius
	 * @C the frequency
	 * Primarily for testing purposes. ***/		
	public void setSinusoidalProfile(float minRadius, float variableRadius, int frequency, int nPoints) {
		if (variableRadius > minRadius) {throw new IllegalArgumentException("B must be strictly smaller than A, otherwise the radial function will be negative at some points.");}
		List<Float> points = new ArrayList<Float>(nPoints);
		List<Float> angles = Vector.createEvenAngles(nPoints);
		for (int i = 0; i < nPoints; i++) {
			float theta = angles.get(i);
			float value = minRadius + variableRadius*sin(frequency*theta);
			points.add((float) value);
		}
		setProfile(points, angles);
	}
	
	/*** Create the gear used as an example in various papers by B. Laczic. Used for testing purposes. ***/
	public void setLaczikGearProfile(int nPoints, float scale) {
		List<Float> points = new ArrayList<Float>(nPoints);
		List<Float> angles = Vector.createEvenAngles(nPoints);
		for (int i = 0; i < nPoints; i++) {
			float theta = angles.get(i);
			float a = 1/7.0f;
			float b = 1/9.0f;
			float c = -2/32.0f;
			float val = a*cos(theta)+2*b*cos(2*theta)+3*c*cos(3*theta);
			float radius = (scale)*((val + 1)/(val + 2));
			points.add(radius);
		}
		setProfile(points, angles);
	}
	
	public void setNonSymetricProfile(int nPoints, float r, float r2) {
		List<Float> angles = Vector.createEvenAngles(nPoints);
		List<Float> radii = new ArrayList<Float>(nPoints);
		for (float angle: angles) {
			if (angle < PI) {
				radii.add(r);
			} else {
				radii.add(r+r2*sin(3*angle));
			}
		}
		setProfile(radii, angles);
	}

	public int getNumAngles(){
		return radii.size();
	}
	
	public List<Float> getRadii() {
		return this.radii;
	}
	
	public List<RPoint> getNorms() {
		return this.norms;
	}
	
	public List<RPoint> getPoints(){
		return this.points;
	}
	
	public List<Float> getAngles(){
		return angles;
	}
	
	
	/*** rotate the gear about its center. ***/
	public void rotate(float theta) {
		shape.rotate(theta,x,y);
		
	}
	
	public void translate(float dx, float dy){
		x += dx;
		y += dy;
		shape.translate(dx, dy);
	}
	
	/*** reflect the gear over the y-axis. Apply before translation. ***/
	public void flip(){
		RMatrix trans = new RMatrix(-1f, 0, 0, 0, 1, 0);
		shape.transform(trans);
	}
	
	/*** Draw the gear centered at the specified point. ***/
	public void draw(){
		RG.shape(shape);
		app.ellipse(x,y,centerRadius,centerRadius);	
		
		
	}
		
	public void drawRadiiFunction(){
		app.stroke(Color.red.getRGB());
		int numAngles = getNumAngles();
		float r = radii.get(0);
		float theta = angles.get(0);
		float xLast = r*cos(theta);
		float yLast = r*sin(theta);
		for (int i = 1; i < numAngles; i ++) {
			r = radii.get(i);
			theta = angles.get(i);
			float xnew = r*cos(theta);
			float ynew = r*sin(theta);
			app.line(xLast, yLast, xnew, ynew);
			xLast = xnew;
			yLast = ynew;
			
		}
		app.line(xLast,yLast,r,0);
		app.stroke(Color.BLACK.getRGB());
	}
	
	
	
	public float getCenterRadius() {
		return centerRadius;
	}

	public void setCenterRadius(float centerRadius) {
		this.centerRadius = centerRadius;
	}

	public RShape getShape() {
		return shape;
	}
	
	/*** This function tests that the data stored in the radii and angle is consistent with the data in the points. ***/
	private void testEquivelence(){
		int numAngles = getNumAngles();
		
		for (int i = 0; i < numAngles; i ++) {
			float adv = i/(float)numAngles;
			RPoint p1 = shape.getPoint(adv);
			RPoint p2 = points.get(i);
			float dist = p1.dist(p2); //this should equal 0
			float r = p2.norm();
			float angle = (float) Math.atan2(p1.y,p1.x);
			if (angle < 0) {angle += 2*PI;}
			float rDiff = r - radii.get(i);
			float angleDiff = angle - angles.get(i);
			
			if (dist != 0f || rDiff != 0f || angleDiff != 0f) {
				throw new IllegalStateException("Angle and radius data is incositant with points. ");
			}
		}
	}
	
}
