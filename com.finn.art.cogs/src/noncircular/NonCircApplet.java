package noncircular;

import geomerative.RG;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.awt.Point;

import processing.core.PApplet;
import processing.core.PShape;
import processing.pdf.*;
import utilities.LineTracer;



public class NonCircApplet extends PApplet {
	static final float BASE_PLATE_ADDITION = 40; //how much on either side of all the holes in the base plate.
	static final float AXEL_WIDTH = 20;
	static final float HOLE_SIZE = 20;
	
	Gear gear1; 
	Gear gear2;
	Conjugate cj;
	int resolution = 600;
	int loop = 0;
	ToothProfile profile = new SquareAndAngleTooth(.5f, .3f);
	
	public void setup() {  
		  size(1900,800,P2D);
		  noSmooth();
		  background(Color.WHITE.getRGB());
		  translate(width/3,height/2);
		  
		  RG.init(this);
		  gear1 = Gear.loadFromFile(new File("/home/finn/programming/hacking/non_circ_gears/blob2.svg"), this, resolution);
		  //gear1 = new Gear(this);
		  //gear1.setSinousoidalProfile(150, 80, 2, resolution);
		  
		  resolution = gear1.getAngles().size(); //there may be less angles in the loaded gear than specified in the resolution due to overhangs
		  gear2 = new Gear(this);
		  cj = new Conjugate(gear1.getRadii(), gear1.getAngles(), .000001f);
		  gear2.setProfile(cj.getRadialFunction(), cj.getMovementFunction(),true);
		  gear2.addTeeth(49, 12,profile);
		  gear2.setColor(Color.WHITE);
		 
		  gear1.expand(10);
		  gear1.setColor(Color.BLACK);
		  gear1.draw(); 
	}
	
	/*** This rotates the 2nd gear around the first which stays motionless. ***/
	public void draw(){
		noSmooth();
		pushMatrix();
		translate(width/3, height/2);
		if (loop < resolution) {
			float angle1 = gear1.getAngles().get(loop);
			rotate(angle1);
			translate(cj.getGearSeparation(),0);
			float angle2 = PI - gear2.getAngles().get(loop);
			rotate(angle2);
			gear2.draw();
			loop = (loop + 1); //% resolution;
			popMatrix();
		} else if (loop == resolution) { //do the stuff we need to be able to cut ...
			popMatrix(); //set 0,0 back to left hand upper corner.
			
			LineTracer lt = new LineTracer(this);
			PShape traced = lt.trace(10);
			traced.setStroke(Color.RED.getRGB());
			traced.setFill(ALPHA);
			
			background(255);
			beginRecord(PDF,"/home/finn/programming/hacking/non_circ_gears/blog_gear.pdf");
			ellipse(width/3,height/2,5,5); 
			shape(traced); //how do we know where the centroid is? we knew it by the radii so just get it back again. Its at 0,0?
			endRecord();
			
			System.out.println("TRACE DONE");
			noLoop();
			
//			gear2.setColor(Color.BLACK);
//			pushMatrix();
//			translate(cj.getGearSeparation()+100,0);
//			gear2.draw();
//			popMatrix();
//			pushMatrix();
//			fill(Color.BLACK.getRGB());
//			translate(0,300);
//			rect(0,0,cj.getGearSeparation()+2*BASE_PLATE_ADDITION + AXEL_WIDTH,2*BASE_PLATE_ADDITION+AXEL_WIDTH);
//			fill(Color.WHITE.getRGB());
//			translate(BASE_PLATE_ADDITION+AXEL_WIDTH/2f,BASE_PLATE_ADDITION+AXEL_WIDTH/2f);
//			ellipse(0,0,AXEL_WIDTH,AXEL_WIDTH);
//			translate(cj.getGearSeparation(),0);
//			ellipse(0,0,AXEL_WIDTH,AXEL_WIDTH);
//			popMatrix();
//			loop ++;
			
			
			
		}
		// else do nothing but keep looping listening for s to be pressed to select the file to save to.
	}
	
	/*** Draw an enlarged version of the pixels in the specified array. ***/
	private void drawPixels(int[][] pixels, float x, float y, float perPixelWidth) {
		pushMatrix();
		for (int i = 0; i < pixels.length; i ++) {
			pushMatrix();
			for (int j = 0; j < pixels.length; j++) {
				if (i == 2 && j == 2) {
					fill(Color.RED.getRGB());
				}else{
					fill(pixels[i][j]);
				}
				rect(x,y,perPixelWidth,perPixelWidth);
				translate(0,perPixelWidth);
			}
			popMatrix();
			translate(perPixelWidth,0);			
		}
		popMatrix();
	}
	
	/*** this draws a function***/
	public void draw4(){
		background(255);
		plotFunction(gear2.getRadii(),gear2.getAngles());
	}
	
	@Override
	public void keyPressed() {
		if (key == 's') {
			 selectOutput("Select a file to write to:", "fileSelected");
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
	
	
	/*** This rotates both gears in sync with one another***/
	public void draw5() {
		background(255);
		translate(width/2, height/2);
		gear2.setColor(Color.BLACK);
		
		pushMatrix();
		rotate(2*PI - gear1.getAngles().get(loop));
		gear1.draw();
		popMatrix();
		
		translate(cj.getGearSeparation(),0);
		rotate(PI - gear2.getAngles().get(loop));
		gear2.draw();
		
		
		loop = (loop + 1) % resolution;
	}
	
	
	private float findYCoordOfLowestColouredPixel(){
		return 0f; //TODO
	}
	
	/*** Float a function specifid by a list of x values against a list of y values.
	 * The plot is scaled such that all the data is visible and fills the entire screen.
	 * @param x
	 * @param y
	 */
	private void plotFunction(List<Float> x, List<Float> y) {
		//we need to scale the x range so that it fits in the width
		float minX = Collections.min(x);
		float maxX = Collections.max(x);
		float range = maxX - minX;
			
		float xScale = width/range;
		float xOffset = -(width/2f + xScale*minX);
		
		List<Float> xCoords = new ArrayList<Float>(x.size());
		for (Float xVal: x) {
			Float xVal2 = xScale*xVal + xOffset;
			xCoords.add(xVal2);
		}
		
		float yMin = Collections.min(y);
		float yMax = Collections.max(y);
		float yRange = yMax - yMin;
		
		float yScale = height/yRange;
		float yOffset = -(height/2f + yScale*yMin);
		
		List<Float> yCoords = new ArrayList<Float>(y.size());
		for (Float yVal: y) {
			Float yVal2  = yScale*yVal + yOffset;
			yCoords.add(yVal2);
		}
		
		Float lastX = xCoords.get(0);
		Float lastY = yCoords.get(0);
		for (int i = 1; i < x.size(); i++) {
			Float xNow = xCoords.get(i);
			Float yNow = yCoords.get(i);
			line(lastX, lastY, xNow, yNow);
			lastX = xNow;
			lastY = yNow;
		}
		
		
	}

	public void output() throws IOException{
		BufferedWriter out = new BufferedWriter(new FileWriter("/home/finn/programming/hacking/non_circ_gears/data.csv"));
		List<Float> col1 = gear1.getAngles();
		List<Float> col2 = gear1.getRadii();
		List<Float> col3 = gear2.getAngles();
		List<Float> col4 = gear2.getRadii();
		
		for (int i = 0; i < resolution; i ++) {
			StringBuilder s = new StringBuilder("");
			s.append(col1.get(i)).append(",").append(col2.get(i)).append(",").append(col3.get(i)).append(",").append(col4.get(i)).append("\n");
			out.write(s.toString());
		}
		out.close();
	}
		
	

}
