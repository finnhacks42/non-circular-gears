package noncircular;

import static processing.core.PApplet.cos;
import static processing.core.PApplet.sin;
import static processing.core.PConstants.PI;
import geomerative.RPoint;
import geomerative.RShape;

import java.awt.Color;
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
	
	
	
	public Gear(PApplet app) {
		this.app = app;
	}
	
	public void setAxelWidth(float width) {
		this.axelWidth = width;
	}
	
	public float getAxelWidth(){
		return this.axelWidth;
	}
	
	
	private float[] copyListToArray(List<Float> input){
		float[] result = new float[input.size()];
		int indx = 0;
		for (Float f: input) {
			result[indx]  = f;
			indx ++;
		}
		return result;
	}
	
	public void setColor(int rgb) {
		shape.fill(rgb);
		shape.stroke(rgb);
	}
	
	public void setProfile(List<Float> radii, List<Float> angles, boolean reverseAxis) {
		setProfile(copyListToArray(radii), copyListToArray(angles), reverseAxis);
	}
	
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
		shape.fill(Color.BLACK.getRGB());
	}
	
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
	
	private List<Float> copyArrayToList(float[] array) {
		List<Float> result = new ArrayList<Float>();
		for (float f: array) {
			result.add(f);
		}
		return result;
	}
	
	public List<Float> getRadii() {
		return copyArrayToList(radii);
	}
	
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
		for (int i = 0; i < angles.length; i ++) {
			float r = radii[i];
			float theta = angles[i];
			float d = offsets[i];
			s.vertex((r+d)*cos(theta), (r+d)*sin(theta));
		}
		s.end();
		return s;
		
	}
	
	/*** Adds teeth to this gear. 
	 * The teeth are added only the what is drawn, the underlying angles and radii of the pitch curve are not changed ***/
	public void addTeeth2(int numTeeth, float toothDepth) {
		float[] offsets = new float[angles.length];
		
		int pointsPerTooth = Math.round(angles.length/((float)2*numTeeth));
		boolean up = true;
		
		for (int i = 0; i < angles.length; i ++) {
			if (up) {
				offsets[i] = toothDepth;
			} else {
				offsets[i] = -toothDepth;
			}
			if (i % pointsPerTooth == 0) {
				up = !up;
			}
					
//			//theta needs to go 0-2PI numTeeth times
//			float offset = toothDepth * sin(theta);
//			theta += dtheta;
//			offsets[i] = offset;
		}
		
		this.shape = createShape(angles, radii, offsets);
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
	
	
	
	
	public void addTeeth(int numTeeth, float toothDepth, ToothProfile toothProfile) {
		
		RShape s = getPitchLineShape();
		PShape newShape = app.createShape();
		
		//we want to find n (approximately) equally spaced points on the arc length of the curve.
		int n = numTeeth*2;
		float ds = 1f/n;
		boolean out = true;
		
		RPoint p1 = null;
		RPoint p2 = null;
		RPoint p1Last = null;
		RPoint p2Last = null;
		
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
		newShape.end();
		this.shape = newShape;
	
	}
	
	
	/*** return the arc length of the pitch curve. ***/
	private float getArcLength() {
		float arc = 0;
		float thetaPrev = 0;
		for (int i = 0; i < angles.length; i ++) {
			float theta = angles[i];
			float r = radii[i];
			float dtheta = theta - thetaPrev;
			arc += r * dtheta;
			thetaPrev = theta;
		}
		return arc;
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
	
	public void draw() {
		app.shape(shape);
		//app.stroke(Color.RED.getRGB());
		//drawPitchCurve();
		app.fill(Color.WHITE.getRGB());
		app.ellipse(0, 0, axelWidth, axelWidth);
	}
	
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
