package noncircular;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import processing.core.PApplet;
import processing.core.PFont;

public class NonCircApplet extends PApplet {
	
	Gear gear1; 
	Gear gear2;
	Conjugate cj;
	int resolution = 200;
	int loop = 0;
	
	public void setup() {  
		  size(1000,650,P2D);
		  background(Color.WHITE.getRGB());
		  gear1 = new Gear(this);
		  gear2 = new Gear(this);
		  gear1.setSinousoidalProfile(100, 50, 2, resolution);
		  cj = new Conjugate(gear1.getRadii(), gear1.getAngles(), .000001f);
		  gear2.setProfile(cj.getRadialFunction(), cj.getMovementFunction(),true);
		  gear2.setColor(Color.WHITE.getRGB());
		  //frameRate(.5f);
		  translate(width/2,height/2);
		  gear1.draw();
		 
		
		  
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
	
	/*** This rotates the 2nd gear around the first which stays motionless. ***/
	public void draw(){
		translate(width/2, height/2);
		gear1.draw();
		
		rotate(gear1.getAngles().get(loop));
		translate(cj.getGearSeparation(),0);
		rotate(PI - gear2.getAngles().get(loop));
		gear2.draw();
		
		
		loop = (loop + 1) % resolution;
	}
	
	
	/*** This rotates both gears in sync with one another***/
	public void draw2() {
		background(255);
		translate(width/2, height/2);
		
		pushMatrix();
		rotate(2*PI - gear1.getAngles().get(loop));
		gear1.draw();
		popMatrix();
		
		translate(cj.getGearSeparation(),0);
		rotate(PI - gear2.getAngles().get(loop));
		gear2.draw();
		
		
		loop = (loop + 1) % resolution;
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

		
	

}
