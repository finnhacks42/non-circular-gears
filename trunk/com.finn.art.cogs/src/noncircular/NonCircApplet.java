package noncircular;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import processing.core.PApplet;

public class NonCircApplet extends PApplet {
	
	Gear gear1; 
	Gear gear2;
	Conjugate cj;
	int nSteps = 100;
	float dtheta = 2*PI/nSteps; //this is the change in angle per frame of the driving gear.
	int loop = 0;
	
	public void setup() {  
		  size(1000,650,P2D);
		  gear1 = new Gear(this);
		  gear2 = new Gear(this);
		  gear1.setSinousoidalProfile(100, 50, 2, 100);
		  cj = new Conjugate(gear1.getRadii(), gear1.getAngles(), .000001f);
		  gear2.setProfile(cj.getRadialFunction(), cj.getMovementFunction());
		  
		  translate(width/2,height/2);
		  gear1.draw();
	}
	
	
	
	@Override
	public void draw() {
		translate(width/2, height/2);
		translate(cj.getGearSeparation(),0);
		gear2.draw();
		//plotFunction(gear1.getAngles(), gear2.getAngles());
		//System.out.println(gear1.getAngles());
		//System.out.println(gear2.getAngles());
		noLoop();
		
		//rotate(gear1.getAngles().get(loop));
		//pushMatrix();
		//translate(cj.getGearSeparation(),0);
		//gear2.draw();
		
		
		
		//gear2.draw();
		loop++;
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
