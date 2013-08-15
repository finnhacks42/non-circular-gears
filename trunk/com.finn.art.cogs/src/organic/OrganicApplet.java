package organic;

import java.awt.Color;

import java.io.File;


import processing.core.PApplet;
import processing.core.PShape;

public class OrganicApplet extends PApplet {	
	PShape s;
	float maximumImageDim = 300f;
	float minImageDim = 150f;
	private float centerTranslationX = maximumImageDim + 10; 
	private float centerTranslationY = maximumImageDim + 10;
	int loop = 0;
	int loopMax;
	float smallRad;
	float largeRad;
	float wlarge;
	float wsmall;
	PShape gear2;
	float scaleFactor = 1;

	
	private void initialize() {
		maximumImageDim = 300f;
		minImageDim = 150f;
		centerTranslationX = maximumImageDim + 10; 
		centerTranslationY = maximumImageDim + 10;
		loop = 0;
		scaleFactor = 1;
		s = loadFromFile("/home/finn/programming/hacking/organic_gears/blob2.svg",true);		
		initializeBaseCircles(1,1000);
		

		//first make everything black.
		background(Color.BLACK.getRGB());
		
		// we want to draw a white circle up to the maximum possible size of the generated gear.
		white();
		ellipse(0,0,2*(largeRad+smallRad),2*(largeRad+smallRad));
		black();		
		
	}
	
	public void setup() {
		size(1300,650);
		noSmooth();
		translate(centerTranslationX,centerTranslationY); //we do this once and only once.
		initialize();	
	}	
	
	/*** loads a shape from a file, translates it to the origin and scales it down. ***/
	private PShape loadFromFile(String fullPath, boolean scaleToMin) {
		PShape result = loadShape(fullPath);
		float maxImageDim = Math.max(result.getWidth(), result.getHeight());
		if (scaleToMin) {
			scaleFactor = minImageDim/Math.min(result.getWidth(), result.getHeight());
			if (scaleFactor*maxImageDim > maximumImageDim) {
				throw new IllegalArgumentException("Cannot scale to match desired minimum dimension without exceeding maximum allowed dimension");
			}
		} else {
			scaleFactor = maximumImageDim/maxImageDim;
		}
		result.scale(scaleFactor);
		result.translate(-result.getWidth()/2, -result.getHeight()/2);
		return result;
	}
	
	
	
	
	/*** This method initializes the radii and frequencies for the two rotating circles. ***/
	private void initializeBaseCircles(int periodRatio, int numLoops){
		smallRad = Math.min(scaleFactor*s.getWidth(), scaleFactor*s.getHeight())/2;
		System.out.println(smallRad);
		largeRad = periodRatio * smallRad;
		wlarge = 2*PI/numLoops;
		wsmall = periodRatio*wlarge;
		loopMax = numLoops;
		System.out.println("Large Radius: "+largeRad);
		System.out.println("Small Radius:" +smallRad);
		
	}
	
	
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
			pushMatrix();
			invertBlackWhite();
			translate(centerTranslationX, centerTranslationY);
			white();
			ellipse(0,0,10,10);
			
			//draw the shape
			pushMatrix();
			translate(largeRad+maximumImageDim/2,maximumImageDim/2+10);
			shape(s);
			ellipse(0,0,10,10);
			popMatrix();
			
			// draw the base plate
			pushMatrix();
			float sidePadding = 75;
			float topBottomPadding = 75;
			float baseWidth = smallRad+largeRad+2*sidePadding;
			float baseHeight = 2*topBottomPadding;
			
			translate(largeRad + smallRad/2+10,-baseHeight);
			
			black();
			rect(0,0,baseWidth,baseHeight,20);
			translate(sidePadding,topBottomPadding);
			white();
			ellipse(0,0,10,10);
			translate(smallRad+largeRad,0);
			ellipse(0,0,10,10);
			popMatrix();
			
			loop ++;
			popMatrix();
		} 
						
	}
	
	@Override
	public void keyPressed() {
		if (key == 's') {
			 selectOutput("Select a file to write to:", "fileSelected");
		}
		if (key == 'r') { //r for reset
			initialize();
			
		}
	}
	
	public void fileSelected(File selection) {
		if (selection == null) {
			println("Window was closed or the user hit cancel.");
		} else {
			println("User selected " + selection.getAbsolutePath());
			save(selection.getAbsolutePath());
		}
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
	
	/*** create a sinusoidal curve. ***/
	private PShape sinusoidalShape(float radius, float rad2, int freq, int numPoints) {
		PShape result = new PShape();
		result.beginShape();
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
		result.endShape();
		result.stroke(Color.BLACK.getRGB());
		result.fill(Color.BLACK.getRGB());	
		result.width = xmax - xmin;
		result.height = ymax - ymin;
		System.out.println(result.getWidth());
		System.out.println(result.getHeight());
		return result;
	}	
}
