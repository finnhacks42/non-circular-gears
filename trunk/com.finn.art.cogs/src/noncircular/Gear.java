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
	
	public void setProfile(List<Float> radii, List<Float> angles) {
		setProfile(copyListToArray(radii), copyListToArray(angles));
	}
	
	public void setProfile(float[] radii, float[] angles) {
		if (angles.length != radii.length) {
			throw new IllegalArgumentException("radii array lenght must match angle array length");
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
		setProfile(radii, angles);
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
	
	
	
	
	public void draw() {
		app.shape(shape);
	}
	
	
	

}
