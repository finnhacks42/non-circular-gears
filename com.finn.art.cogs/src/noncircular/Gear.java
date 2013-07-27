package noncircular;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import processing.core.PApplet;
import processing.core.PShape;
import static processing.core.PApplet.cos;
import static processing.core.PApplet.sin;
import static processing.core.PConstants.PI;

/*** A gear with a radius and angle. ***/
public class Gear {

	private PApplet app;
	private float[] angles;
	private float[] radii;
	private PShape shape;
	
	
	
	public Gear(PApplet app) {
		this.app = app;
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
		shape = app.createShape();
		
		for (int i = 0; i < angles.length; i ++) {
			float r = radii[i];
			float theta = angles[i];
			shape.vertex(r*cos(theta), r*sin(theta));
		}
		shape.end();
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
	
	/*** Adds teeth to this gear. 
	 * The teeth are added only the what is drawn, the underlying angles and radii of the pitch curve are not changed ***/
	public void addTeeth(int numTeeth, float toothDepth) {
		
	}
	
	/*** Expands the shape that is drawn for the gear such that it can cut into the blanks in the other gear.
	 * Does not modify the pitch curve. 
	 */
	public void expand(float depth) {
		
	}
	
	public void draw() {
		app.shape(shape);		
	}
	
	
	

}
