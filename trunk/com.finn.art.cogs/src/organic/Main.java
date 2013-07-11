package organic;

import java.awt.Color;

import processing.core.PApplet;
import processing.core.PShape;


public class Main extends PApplet {
	
	PShape s;
	int loop = 0;
	int loopMax = 200;
	float smallRad = 30;
	int periodRatio = 3;
	float largeRad;
	float wlarge;
	float wsmall;
	PShape gear2;
	
	
	
	
	public void setup() {
		size(500,500,P2D);
		s = sinusoidalShape(smallRad, 8, 4, 100);
		largeRad = periodRatio * smallRad;
		wlarge = 2*PI/loopMax;
		wsmall = periodRatio * wlarge;
		background(0);
		translate(width/2, height/2);
		ellipse(0,0,2*(largeRad+smallRad),2*(largeRad+smallRad));
	}
	
	
	public void draw() {
		translate(width/2,height/2);
		rotate(loop*wlarge);
		
		pushMatrix();
		translate(smallRad+largeRad,0);	
		rotate(loop*wsmall);
		shape(s);
		popMatrix();
		
		loop ++;
		
		if (loop == loopMax) {
			LineTracer l = new LineTracer(this);
			gear2 = l.trace();
			background(255);
			//translate(-width/2,-height/2);
			gear2.translate(-width/2, -height/2);
			shape(gear2);
			s.fill(255);
			s.translate(smallRad+largeRad, 0);
			shape(s);
			noLoop();
		}
		
	}

	private PShape sinusoidalShape(float radius, float rad2, int freq, int numPoints) {
		PShape result = createShape();
		
		float theta = 0;
		float dtheta = 2*PI/numPoints;
		result.stroke(Color.BLACK.getRGB());
		result.fill(Color.BLACK.getRGB());
		
		for (int i = 0; i < numPoints; i ++) {
			float r = radius + rad2*cos(freq*theta);
			theta += dtheta;
			result.vertex(r*cos(theta), r*sin(theta));
		}
		
		result.end();
		return result;
	}
	
}
