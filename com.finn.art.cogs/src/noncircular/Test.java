package noncircular;

import static processing.core.PConstants.PI;
import geomerative.RG;
import geomerative.RPoint;
import geomerative.RShape;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;

public class Test extends PApplet {
	
//	RShape s;
//	int resolution = 200;
//	RPoint center;
//	RPoint p1;
//	RPoint pPrev;
//	int indx = 1;
//	
//	boolean isClockwise;
	
	List<Float> angles = new ArrayList<Float>();
	List<Float> radii = new ArrayList<Float>();
	RPoint center;
	
	private void loadShape2(File shapefile, int resolution) {
		List<Float> angles = new ArrayList<Float>();
		List<Float> radii = new ArrayList<Float>();
		RShape s = RG.loadShape(shapefile.getAbsolutePath());
		RPoint center = s.getCenter();
		RPoint pPrev = s.getPoint(0);
		pPrev.sub(center);
		RPoint p1 = s.getPoint(1/(float)resolution);
		p1.sub(center);
		RPoint axis = new RPoint(10,0);
		boolean isClockwise = clockwise(p1, pPrev);
		for (int i = 1; i < resolution; i++) {
			RPoint point = s.getPoint(i/(float)resolution);
			point.sub(center);
			if (clockwise(point, pPrev) == isClockwise) {
				//add a point
				float radius = point.norm();
				float angle = angle(axis,point);
				angles.add(angle);
				radii.add(radius);
				pPrev = point;
			}
		}
	}
	
//	private void loadShape(){
//		s = RG.loadShape("/home/finn/programming/hacking/non_circ_gears/blob2.svg");
//		center = s.getCenter();
//		p1 = s.getPoint(0);
//		p1.sub(center);
//		pPrev = p1;
//		RPoint p2 = s.getPoint(1/(float)resolution);
//		p2.sub(center);
//		isClockwise = clockwise(p2, p1);
//			
//	}
	
	@Override
	public void setup() {
		size(1000,800,P2D);
		RG.init(this);
		File file = new File("/home/finn/programming/hacking/non_circ_gears/blob2.svg");
		//frameRate(1);
		loadShape2(file, 200);	
	}
	
	/*** returns true if p1 is clockwise from p2 otherwise false. ***/
	private static boolean clockwise(RPoint p1, RPoint p2) {
		float theta1 = (float) Math.atan2(p1.y, p1.x); //a number between -PI and PI
		float theta2 = (float) Math.atan2(p2.y, p2.x);
		float diff = theta2 - theta1;
		float diffMag = Math.abs(diff);
		if (diffMag >= PI) {
			diff = - diff;
		}
		if (diff < 0) {
			return true;
		} else {
			return false;
		}
	}
	
	private static float angle(RPoint p1, RPoint p2) {
		float theta1 = (float) Math.atan2(p1.y, p1.x); //a number between -PI and PI
		float theta2 = (float) Math.atan2(p2.y, p2.x);
		float diff = theta2 - theta1;
		if (diff <0) {diff += 2*PI;} 
		return diff;
	}
	
	public void draw() {
		for (int indx  = 0; indx < angles.size(); indx ++) {
			float angle = angles.get(indx);
			float radius = radii.get(indx);
			line(center.x,center.y,center.x+radius*cos(angle),center.y+radius*sin(angle));
		}
		
	}
	
//	@Override
//	public void draw() {
//		ellipse(center.x,center.y,10,10);
//		if (indx < resolution) {
//			RPoint p = s.getPoint(indx/(float)resolution);
//			RPoint pSaved = new RPoint(p);
//			p.sub(center);
//			float r = p.norm();
//			boolean clockwise = clockwise(p, pPrev);
//			System.out.println(clockwise);
//			if (clockwise == isClockwise) {
//				float theta = angle(axis,p);
//				line(center.x,center.y,center.x+r*cos(theta),center.y+ r*sin(theta));
//				pPrev = p;
//			}
//	
//			
//			//}
//			
//			ellipse(pSaved.x,pSaved.y,10,10);
//			
//			indx ++;
//		} else {
//			noLoop();
//		}	
//	}

}
