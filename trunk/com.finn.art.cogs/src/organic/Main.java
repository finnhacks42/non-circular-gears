package organic;

import java.awt.Color;
import java.awt.Point;
import java.io.File;

import processing.core.PApplet;
import processing.core.PShape;

public class Main extends PApplet {	
	PShape s;
	float maxAllowedImageDim = 300f;
	private float centerTranslationX = maxAllowedImageDim + 10; 
	private float centerTranslationY = maxAllowedImageDim + 10;
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
		size(600,800);
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
		size(1300,650);
		noSmooth();
		//s = sinusoidalShape(50, 8, 4, 100);
		s = loadFromFile("/home/finn/programming/hacking/pictures/steg1.svg");		
		initializeBaseCircles(3,300,100);
		//first make everything black.
		background(Color.WHITE.getRGB());
		stroke(Color.BLACK.getRGB());
		fill(Color.BLACK.getRGB());
		rect(0, 0, width, height);
		
				
		// we want to draw a white circle up to the maximum possible size of the generated gear.
		translate(centerTranslationX,centerTranslationY);
		stroke(Color.WHITE.getRGB());
		fill(Color.WHITE.getRGB());
		ellipse(0,0,2*(largeRad+smallRad),2*(largeRad+smallRad));
		stroke(Color.BLACK.getRGB()); //reset fill and stroke to be black.
		fill(Color.BLACK.getRGB());
			
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
			//translate(width/2,height/2);
			translate(centerTranslationX,centerTranslationY);
			rotate(loop*wlarge);	
			pushMatrix();
			translate(smallRad+largeRad,0);	
			rotate(loop*wsmall);
			shape(s);
			popMatrix();
			loop ++;
		} else if (loop == loopMax) {
			invertBlackWhite();
			translate(centerTranslationX, centerTranslationY);
			white();
			ellipse(0,0,10,10);
			
			//draw the shape
			pushMatrix();
			translate(largeRad*2,smallRad);
			shape(s);
			ellipse(0,0,10,10);
			popMatrix();
			
			// draw the base plate
			pushMatrix();
			translate(largeRad + 10,-largeRad);
			float sidePadding = 100;
			float topBottomPadding = 100;
			float baseWidth = smallRad+largeRad+2*sidePadding;
			float baseHeight = 2*topBottomPadding;
			black();
			rect(0,0,baseWidth,baseHeight,20);
			translate(sidePadding,topBottomPadding);
			white();
			ellipse(0,0,10,10);
			translate(smallRad+largeRad,0);
			ellipse(0,0,10,10);
			popMatrix();
			

			
			loop ++;
		} 
			
			
//			background(255);
//			gear2.translate(-width/2, -height/2);
//			shape(gear2);
//			s.fill(255);
//			s.translate(smallRad+largeRad, 0);
//			shape(s);
			//noLoop();
			
	}
	
	private void white() {
		fill(Color.WHITE.getRGB());
		stroke(Color.WHITE.getRGB());
	}
	
	private void black() {
		fill(Color.BLACK.getRGB());
		stroke(Color.BLACK.getRGB());
	}
	
	private void invertBlackWhite() {
		loadPixels();
		for (int x = 0; x < width; x ++) {
			for (int y = 0; y < height; y ++) {
				int loc = x + y * width;
				int col = pixels[loc];
				if (Color.BLACK.getRGB() == col) {
					pixels[loc] = Color.WHITE.getRGB();
				} else if (Color.WHITE.getRGB() == col) {
					pixels[loc] = Color.BLACK.getRGB();
				}
			}
		}
		updatePixels();
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
