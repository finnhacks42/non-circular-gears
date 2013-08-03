package noncircular;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import organic.LineTracer;

import processing.core.PApplet;

public class InvoluteToothApplet extends PApplet {
	
	float pitchRadius = 100;
	float baseRadius;
	float maxRadius;
	float pressureAngle = 20;
	float module = 10;
	float theta = 0;
	float dtheta = 2*PI/100f;
	
	List<Float> xCoords = new ArrayList<Float>();
	List<Float> yCoords = new ArrayList<Float>();
	
	@Override
	public void setup() {
		size(500,500);
		translate(width/2,height/2);
		baseRadius = pitchRadius*cos(pressureAngle);
		maxRadius = pitchRadius + module;
		calculateCurve();
		
		stroke(Color.GREEN.getRGB());
		ellipse(0, 0, maxRadius*2, maxRadius*2);
		
		stroke(Color.red.getRGB());
		ellipse(0,0,pitchRadius*2,pitchRadius*2);
		stroke(Color.BLACK.getRGB());
		ellipse(0,0,baseRadius*2,baseRadius*2);
		
		drawCurve();
		
	}
	
	public void drawCurve(){
		float x1 = xCoords.get(0);
		float y1 = yCoords.get(0);
		
		for (int i = 1; i < xCoords.size(); i++) {
			float x2 = xCoords.get(i);
			float y2 = yCoords.get(i);
			line(x1, y1, x2, y2);
			x1 = x2;
			y1 = y2;
		}
	}
	
	public void calculateCurve(){	
		float r = maxRadius+1;
		while (true) {
			
			float x = baseRadius * (cos(theta) + theta*sin(theta));
			float y = baseRadius * (sin(theta) - theta*cos(theta));
			r = sqrt(pow(x,2)+pow(y,2));
			xCoords.add(x);
			yCoords.add(y);
			theta +=dtheta;
			if (r > maxRadius) {
				break;
			}
		}
		System.out.println("Curve calculated");
	}
	

	

}
