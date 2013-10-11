package noncircular;

import static processing.core.PApplet.cos;
import static processing.core.PApplet.sin;
import static processing.core.PConstants.PI;
import geomerative.RG;
import geomerative.RPoint;
import geomerative.RShape;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import processing.core.PApplet;
import processing.core.PShape;

/*** A gear with a radius and angle. ***/
public class Gear {

	private PApplet app;
	private float[] angles;
	private float[] radii;
	private PShape shape;
	private float axelWidth  = 20;
	private Color color = Color.BLACK;
	
	public void setStroke(Color c) {
		shape.setStroke(c.getRGB());
	}
	/*** create a new gear from a drawing loaded from a file.
	 * If at any point there are two radii for a given angle then the first one reached along the curve will be used. ***/
	public static Gear loadFromFile(File file, PApplet app, int resolution){
			List<Float> angles = new ArrayList<Float>();
			List<Float> radii = new ArrayList<Float>();
			RShape s = RG.loadShape(file.getAbsolutePath());
			RPoint center = s.getCenter();
			RPoint pPrev = s.getPoint(0);
			pPrev.sub(center);
			RPoint p1 = s.getPoint(1/(float)resolution);
			p1.sub(center);
			RPoint axis = new RPoint(0,0); //was 10,0 value doesn't seem to make any difference. Why is this here
			
			boolean isClockwise = Vector.clockwise(p1, pPrev);
			for (int i = 0; i < resolution; i++) {
				RPoint point = s.getPoint(i/(float)resolution);
				point.sub(center);
				if (Vector.clockwise(point, pPrev) == isClockwise) {
					//add a point
					float radius = point.norm();
					float angle = Vector.angle(axis,point);
					angles.add(angle);
					radii.add(radius);
					pPrev = point;
				}
			}
		
		
		Gear gear = new Gear(app);
		gear.setProfile(radii, angles, true);
		return gear;
	}
	
	/*** Create a new gear without specifying any shape. ***/
	public Gear(PApplet app) {
		this.app = app;
	}
	
	/*** Set the width in pixels of the circle that should be drawn for the gear axel. ***/
	public void setAxelWidth(float width) {
		this.axelWidth = width;
	}
	
	/*** Get the width in pixels of the circle drawn for the axel. ***/
	public float getAxelWidth(){
		return this.axelWidth;
	}
	
	/*** create an array of floats from a list of Floats. ***/
	private float[] copyListToArray(List<Float> input){
		float[] result = new float[input.size()];
		int indx = 0;
		for (Float f: input) {
			result[indx]  = f;
			indx ++;
		}
		return result;
	}
	
	/*** create a list of Floats from an array of floats. ***/
	private List<Float> copyArrayToList(float[] array) {
		List<Float> result = new ArrayList<Float>();
		for (float f: array) {
			result.add(f);
		}
		return result;
	}
	
	/*** Set the fill and stroke the gear should be painted as. ***/
	public void setColor(Color color) {
		this.color = color;
		shape.setFill(color.getRGB());
		shape.setStroke(color.getRGB());
	}
	
	/*** Set the shape of the gear. ***/
	public void setProfile(List<Float> radii, List<Float> angles, boolean reverseAxis) {
		setProfile(copyListToArray(radii), copyListToArray(angles), reverseAxis);
	}
	
	/*** Set the shape of the gear. ***/
	public void setProfile(float[] radii, float[] angles, boolean reverseAxis) {
		if (angles.length != radii.length) {
			throw new IllegalArgumentException("radii array lenght must match angle array length");
		}
		if (reverseAxis) { //the angles are specified relative to the -ve x axis.
			int indx = 0;
			for (float phi: angles) {
				phi = PI - phi;
				if (phi < 0 ) {phi = phi + 2*PI;}
				angles[indx] = phi;
				indx ++;
			}
		}
		this.angles = angles;
		this.radii = radii;
		shape = createShape(angles, radii, null);
	}
	
	/*** Set the shape of the gear from a shape. 
	 * This takes a shape and breaks it into a series of angles and radii. ***/
	public void setProfile(PShape shape) { 
		//we need to somehow find a center to start at...
	}
	
	/*** Set the shape of the gear as a sinusoidal radius. ***/
	public void setSinousoidalProfile(float baseRadius, float variableRad, int freq, int numPoints) {
		if (variableRad >= baseRadius) {throw new IllegalArgumentException("varialble radius component must be small than base radius");}
		angles = new float[numPoints];
		radii = new float[numPoints];
		float dtheta = 2*PI/numPoints;
		for (int i = 0; i < numPoints; i++) {
			float theta = dtheta*i;
			float r = baseRadius + variableRad * sin(freq*theta);
			angles[i] = theta;
			radii[i] = r;
		}
		setProfile(radii, angles, false);
	}
	
	/*** get the radii of the gear pitch curve. ***/
	public List<Float> getRadii() {
		return copyArrayToList(radii);
	}
	
	/*** get the angles at which the radius of the gear pitch curve is specified. ***/
	public List<Float> getAngles() {
		return copyArrayToList(angles);
	}
	
	/*** create a shape based on the specified angles and radii.
	 * 
	 * @param angles
	 * @param radii
	 * @param offsets used to create teeth or to expand the radii at each point. Can be null
	 * @return
	 */
	private PShape createShape(float[] angles, float[] radii, float[] offsets) {
		if (offsets == null) {
			offsets = new float[angles.length];
			Arrays.fill(offsets, 0);
		}
		PShape s = app.createShape();
		float r0 = radii[0]+offsets[0];
		float theta0 = angles[0];
		s.beginShape();
		for (int i = 0; i < angles.length; i ++) {
			float r = radii[i];
			float theta = angles[i];
			float d = offsets[i];
			s.vertex((r+d)*cos(theta), (r+d)*sin(theta));
		}
		s.vertex(r0*cos(theta0), r0*sin(theta0));
		s.endShape();
		s.setFill(color.getRGB());
		s.setStroke(color.getRGB());
		return s;	
	}
	
	/*** Produces an RShape in the shape of the pitch line. ***/
	private RShape getPitchLineShape(){
		RShape s = new RShape();
		float rStart = radii[0];
		float thetaStart = angles[0];
		s.addMoveTo(rStart*cos(thetaStart),rStart*sin(thetaStart));
		for (int i = 1; i < angles.length; i ++) {
			float theta = angles[i];
			float r = radii[i];
			s.addLineTo(r*cos(theta),r*sin(theta));
		}
		s.addLineTo(rStart*cos(thetaStart), rStart*sin(thetaStart));
		s.addClose();
		return s;
	}
	
	/*** Add teeth to this gear. This does not modify the pitch-line of the shape, only the shape that will be drawn. ***/
	public void addTeeth(int numTeeth, float toothDepth, ToothProfile toothProfile) {
		RShape s = getPitchLineShape();
		//we want to find n (approximately) equally spaced points on the arc length of the curve.
		int n = numTeeth*2;
		float ds = 1f/n;
		boolean out = true;
		RPoint p1 = null;
		RPoint p2 = null;
		RPoint p1Last = null;
		RPoint p2Last = null;
		RPoint first = null;
		PShape newShape = app.createShape();
		newShape.beginShape();
		for (int i = 0; i < n; i ++) {
			float arc = i*ds;
			RPoint p = s.getPoint(arc);
			RPoint tangent = s.getTangent(arc);
			RPoint norm = Vector.getUnitNorm(tangent);
			norm.scale(toothDepth/2);
			RPoint outSidePoint = new RPoint(p.x-norm.x, p.y-norm.y);
			RPoint insidePoint = new RPoint(p.x+norm.x,p.y+norm.y);
			if (out) {
				p1 = insidePoint;
				p2 = outSidePoint;
				newShape.vertex(p1.x, p1.y);
				if (first == null) {
					first = p1;
				}
			} else {
				p1 = outSidePoint;
				p2 = insidePoint;
				if (p1Last != null && p2Last != null) {
					List<RPoint> toothPoints = toothProfile.getProfile(p1Last, p2Last, p1, p2);
					for (RPoint tp: toothPoints) {
						newShape.vertex(tp.x, tp.y);
					}
					newShape.vertex(p2.x, p2.y);
				}
			}
			p1Last = p1;
			p2Last = p2;
			out =! out;	
		}
		newShape.vertex(first.x, first.y);
		newShape.endShape();
		this.shape = newShape;
	}
	
	
	/*** Creats a new shape that is an expansion of the pitch curve.
	 *  the shape that is drawn for the gear such that it can cut into the blanks in the other gear.
	 * Does not modify the pitch curve. 
	 */
	public void expand(float depth) {
		float [] offsets = new float[angles.length];
		Arrays.fill(offsets, depth);
		this.shape = createShape(angles, radii, offsets);
	}
	
	/*** draw the gear (with teeth) if any. ***/
	public void draw() {
		app.shape(shape);
		//app.stroke(Color.RED.getRGB());
		//drawPitchCurve();
		app.fill(Color.WHITE.getRGB());
		app.ellipse(0, 0, axelWidth, axelWidth);
	}
	
	/*** draw the pitch curve of the gear. ***/
	private void drawPitchCurve(){
		float theta = angles[0];
		float r = radii[0];
		float startx = r*cos(theta);
		float starty = r*sin(theta);
		float x1 = startx;
		float y1 = starty;
		
		for (int i = 1; i < angles.length; i++) {
			theta = angles[i];
			r = radii[i];
			float x2 = r*cos(theta);
			float y2 = r*sin(theta);
			app.line(x1, y1, x2, y2);
			x1 = x2;
			y1 = y2;
		}
		app.line(x1, y1, startx, starty);
	}
}
