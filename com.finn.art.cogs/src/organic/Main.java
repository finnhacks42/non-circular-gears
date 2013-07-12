package organic;

import java.awt.Color;
import java.awt.Point;
import java.io.File;

import processing.core.PApplet;
import processing.core.PShape;

public class Main extends PApplet {	
	PShape s;
	float maxAllowedImageDim = 100f;
	int loop = 0;
	int loopMax;
	float smallRad;
	float largeRad;
	float wlarge;
	float wsmall;
	PShape gear2;
	float scaleFactor = 1;
	LineTracer tracer;
	
	/*** loads a shape from a file, translates it to the origin and scales it down. ***/
	private PShape loadFromFile(String fullPath) {
		PShape result = loadShape(fullPath);
		float maxImageDim = Math.max(result.getWidth(), result.getHeight());
		scaleFactor = maxAllowedImageDim/maxImageDim;
		result.scale(scaleFactor);
		result.translate(-result.getWidth()/2, -result.getHeight()/2);
		return result;
	}
	
	/*** A very simple setup for testing. ***/
	public void setup2() {
		size(600,600);
		noSmooth();
		s = loadFromFile("/home/finn/programming/hacking/pictures/steg1.svg");
		translate(width/2,height/2);
		float rwidth = scaleFactor*s.getWidth();
		float rhight = scaleFactor*s.getHeight();
		rect(-rwidth/2,-rhight/2,rwidth,rhight);
		shape(s);
		ellipse(0,0,20,20);
	}
	
	public void setup() {
		size(600,600);
		noSmooth();
		//s = sinusoidalShape(50, 8, 4, 100);
		s = loadFromFile("/home/finn/programming/hacking/pictures/steg1.svg");		
		initializeBaseCircles(3,300,1000);
		//first make everything black.
		background(Color.WHITE.getRGB());
		stroke(Color.BLACK.getRGB());
		fill(Color.BLACK.getRGB());
		rect(0, 0, width, height);
		translate(width/2,height/2);
		
		
		
		// we want to draw a white circle up to the maximum possible size of the generated gear.
		stroke(Color.WHITE.getRGB());
		fill(Color.WHITE.getRGB());
		ellipse(0,0,2*(largeRad+smallRad),2*(largeRad+smallRad));
		stroke(Color.BLACK.getRGB()); //reset fill and stroke to be black.
		fill(Color.BLACK.getRGB());
		//shape(s);
			
	}	
	
	/*** This method initializes the radii and frequencies for the two rotating circles. ***/
	private void initializeBaseCircles(int periodRatio, float gear2minWidth, int numLoops){
		smallRad = Math.min(scaleFactor*s.getWidth(), scaleFactor*s.getHeight())/2;
		System.out.println(smallRad);
		largeRad = periodRatio * smallRad;
		wlarge = 2*PI/numLoops;
		wsmall = periodRatio*wlarge;
		loopMax = numLoops;
	}
	
	Point linePoint = new Point();
	Point prev = null;
	Point next = null;
	public void draw() {
		
		if (loop < loopMax) {
			translate(width/2,height/2);
			rotate(loop*wlarge);	
			pushMatrix();
			translate(smallRad+largeRad,0);	
			rotate(loop*wsmall);
	
			shape(s);
			popMatrix();
			
			loop ++;
		}
		
		if (loop >= loopMax) {
			if (tracer == null) {
				tracer = new LineTracer(this);
				linePoint = tracer.findStart(3);	
				tracer.countColors();
				
			}
			//set the color of the point at p to red - prevents backtracking
			int ploc =tracer.location(linePoint.x,linePoint.y);
			System.out.println("initial color: "+pixels[ploc]); // should be -1 
			pixels[ploc] = Color.RED.getRGB();
			
			System.out.println(linePoint);
			fill(Color.RED.getRGB());
			ellipse(linePoint.x,linePoint.y,10,10);
			next = tracer.nextEdgePoint(linePoint, pixels);
			prev = linePoint;
			linePoint = next;
			
			
//			background(255);
//			gear2.translate(-width/2, -height/2);
//			shape(gear2);
//			s.fill(255);
//			s.translate(smallRad+largeRad, 0);
//			shape(s);
			//noLoop();
		}		
	}
	
//	void setup() {
//		  selectOutput("Select a file to write to:", "fileSelected");
//		}

		void fileSelected(File selection) {
		  if (selection == null) {
		    println("Window was closed or the user hit cancel.");
		  } else {
		    println("User selected " + selection.getAbsolutePath());
		  }
		}
	
	

	/*** create a sinusoidal curve. ***/
	private PShape sinusoidalShape(float radius, float rad2, int freq, int numPoints) {
		PShape result = new PShape();
		float theta = 0;
		float dtheta = 2*PI/numPoints;
		float xmax = Float.MIN_VALUE;
		float ymax = Float.MIN_VALUE;
		float xmin = Float.MAX_VALUE;
		float ymin = Float.MAX_VALUE;
		result.stroke(Color.BLACK.getRGB());
		result.fill(Color.BLACK.getRGB());		
		for (int i = 0; i < numPoints; i ++) {
			float r = radius + rad2*cos(freq*theta);
			theta += dtheta;
			float x = r*cos(theta);
			float y = r*sin(theta);
			result.vertex(r*cos(theta), r*sin(theta));
			if (x > xmax){xmax = x;}
			if (y > ymax){ymax = y;}
			if (x < xmin){xmin = x;}
			if (y < ymin){ymin = y;}
		}	
		result.end();
		result.stroke(Color.BLACK.getRGB());
		result.fill(Color.BLACK.getRGB());	
		result.width = xmax - xmin;
		result.height = ymax - ymin;
		System.out.println(result.getWidth());
		System.out.println(result.getHeight());
		return result;
	}	
}
