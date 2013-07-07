import geomerative.RG;
import geomerative.RPoint;
import geomerative.RShape;

import java.awt.Color;
import java.util.List;

import processing.core.PApplet;



public class CogApp extends PApplet {
	GearShape gear1; 
	ConjugateGear conjugate; 
	GearShape gear2;
	boolean record;
	
	List<Float> radii;
	List<RPoint> points;
	List<Float> angles;
	int numAngles;
	RPoint origin = new RPoint(0,0);
	RPoint prevPoint = null;
	RPoint prevCutterOrigin = new RPoint(0,0);
	float cutterAngle = 0;
	float cutterRadius = 0f;
	Cutter cutter;
	RShape cutShape;
	int numCutterAngles = 1000;
	int loop = 0;
	RPoint xAxis = new RPoint(1,0);
	
	

	@Override
	public void setup() {	
		size(1000,1000, P3D);
		RG.init(this);
		gear1 = new GearShape(this);
		gear1.setSinusoidalProfile(200, 50, 2, 1000);
		
		radii = gear1.getRadii();
		points = gear1.getPoints();
		numAngles = radii.size();
		cutter = new Cutter(gear1.getShape().getCurveLength(), 10, 7, 7, 100, this);
		cutShape = cutter.getShape();
		cutterRadius = cutter.getRadius();
		angles = gear1.getAngles();

	}
	
	
	private void animateCut(int loop) {
		RPoint p = points.get(loop);
		RPoint norm = gear1.getNorms().get(loop);
		RPoint scaledNorm = new RPoint(norm);
		scaledNorm.scale(cutterRadius);

		
		//translate the cutter.
		RPoint cutterOrigin = new RPoint(p);
		cutterOrigin.add(scaledNorm);
		RPoint trans = new RPoint(cutterOrigin);
		trans.sub(prevCutterOrigin);
		cutShape.translate(trans);
		
		line(0,0,cutterOrigin.x,cutterOrigin.y);

		if (prevPoint != null) {
			float dist = p.dist(prevPoint);
			cutterAngle += dist/(float)cutterRadius;
			//actually rotate the cutter here ...	
			cutShape.rotate(dist/(float)cutterRadius,cutterOrigin);
		}

		for (int j = 0; j < numCutterAngles; j ++) {
			float adv = j/(float)numCutterAngles;
			RPoint cutterPoint = cutShape.getPoint(adv);
			float angleFromOrigin = atan2(cutterPoint.y,cutterPoint.x);
			if (angleFromOrigin < 0) {
				angleFromOrigin += 2*PI;
			}
			
			float distFromOrigin = cutterPoint.norm();
			int angleIndx = gear1.getRadialIndx(angleFromOrigin);
			radii.set(angleIndx, Math.min(radii.get(angleIndx), distFromOrigin));
		}
		
		prevPoint = p;
		prevCutterOrigin = cutterOrigin;
		cutter.draw();
	}
	
	public void draw() {
		background(Color.WHITE.getRGB());
		translate(width/2,height/2);
		gear1.draw(0, 0);
		if (loop < numAngles) {
			animateCut(loop);
			loop ++;
		} 

		
		
		
	}
	
	
//	public void draw3() {
//		
//		 if (record) {
//			    beginRaw(DXF, "/home/finn/programming/hacking/non_circ_gears/output.dxf");
//		 }
//		background(Color.WHITE.getRGB());
//		translate(width/2,height/2);
//		
//		distAlongCurve = currentPoint/(float)npoints;
//		RPoint p = gear1.getShape().getPoint(distAlongCurve);
//		ds = p.dist(pLast);
//		RPoint t = gear1.getShape().getTangent(distAlongCurve);
//		
//		RPoint norm = Vector.getUnitNorm(t);
//		norm.scale(r);
//		
//		//shift the cutter origin to the new point.
//		RPoint cutterOriginPrev = cutterOrigin; //this is where the cutter was
//		
//		cutterOrigin = new RPoint(p);
//		cutterOrigin.add(norm); //this is where the cutter is now
//		
//		RPoint trans = new RPoint(cutterOrigin);
//		trans.sub(cutterOriginPrev); //this is the difference
//		
//		cutter.translate(trans);
//		
//		cutter.rotate(ds/r, cutterOrigin);//
//		
//		
//		gear1.draw(0, 0);
//		
//		stroke(Color.RED.getRGB());
//		RG.shape(cut);
//		stroke(Color.BLACK.getRGB());
//		line(0,0,p.x,p.y);
//		line(p.x,p.y,p.x+norm.x,p.y+norm.y);
//		line(0,0,cutterOrigin.x,cutterOrigin.y);
//		//ellipse(cutterOrigin.x,cutterOrigin.y,2*r,2*r);
//		
//		cut = subtract(cutter,cut);
//		RG.shape(cutter);
//		
//			
//		
//		
//		currentPoint ++;
//		pLast = p;
//			
//		
//		 if (record) {
//			    endRaw();
//			    record = false;
//			    System.out.println("DXF written");
//		 }
//		 
//		
//		//noLoop();
//	}
	
	/*** subtract any bits of shape1 that go into shape2. ***/
	private static RShape subtract(RShape shape1, RShape shape2) {
		float smallStep = 0.0001f;
		float bigStep = 0.01f;
		
		RPoint[] intersects = shape2.getIntersections(shape1);
		RShape result = new RShape();
		result.addMoveTo(shape2.getPoint(0));
		
		float theta = atan(intersects[0].y/intersects[0].x);
		float startAdv = theta/(2*PI);
		float theta2 = atan(intersects[1].y/intersects[1].x);
		float endAdv = theta2/(2*PI);
		
		float deltaAdv2 = endAdv - startAdv;
		
		if (deltaAdv2 <  smallStep) {
			
			return shape2;
		}
		
		float adv1 = findAdv(shape1, intersects[1], 1000);
		float adv2 = findAdv(shape1, intersects[0], 1000);
		float deltaAdv1 = adv2 - adv1;
		
		//System.out.println(deltaAdv2+","+deltaAdv1);
		

		float adv = 0;
		while (adv < startAdv - bigStep ) {
			RPoint p = shape2.getPoint(adv);
			result.addLineTo(p);
			adv += bigStep;
		}
		adv = startAdv - bigStep;
		while (adv < startAdv) {
			RPoint p = shape2.getPoint(adv);
			result.addLineTo(p);
			adv += smallStep;
		}
		
		adv = adv1;
		while (adv < adv2) {
			//System.out.println("Adv,"+adv+" start,"+adv1+" end,"+adv2);
			RPoint p = shape1.getPoint(adv);
			result.addLineTo(p);
			adv += smallStep;
		}
		
		adv = endAdv;
		while (adv < .9999f) {
			
			RPoint p = shape2.getPoint(adv);
			result.addLineTo(p);
			adv += bigStep;
		}
		
		
		return result;
	    
	}
	
	/*** find the advancement that yeilds a point as close as possible to p. ***/
	private static float findAdv(RShape s, RPoint p, int resolution) {
		float minDist = Float.MAX_VALUE;
		float closestAdv = 0f;
		for (int i = 0;  i < resolution; i ++) {
			float adv = i/(float)resolution;
			RPoint shapePoint = s.getPoint(adv);
			float dist = shapePoint.dist(p);
			if (dist < minDist) {
				minDist = dist;
				closestAdv = adv;
			}
		}
		
		return closestAdv;
	}
	
	
	
	
	
	private RShape test(){
		RShape circle = new RShape();
		float r = 30;
		float theta = 0;
		
		int points = 100;
		float dtheta = 2*PI/(float)points;
		for (int i = 0; i < points; i ++) {
			circle.addLineTo(r*cos(theta) - 30, r*sin(theta));
			theta += dtheta;
		}
		circle.addClose();
		RShape t = new RShape();
		t.addLineTo(-30, -5);
		t.addLineTo(-30,5);
		t.addClose();
		circle.addShape(t);
		
		circle.translate(30, 0);
		return circle;
	}
	
	private RShape circle(float radius, int npoints) {
		RShape s = new RShape();
		s.addMoveTo(radius, 0);
		float theta = 0;
		float dtheta = 2*PI/(float)npoints;
		for (int i = 0; i < npoints; i++) {
			s.addLineTo(radius * cos(theta), radius*sin(theta));
			theta += dtheta;
		}
		s.addClose();
		return s;
	}
	
	
	/*** record the data to a dxf file if r is pressed. ***/
	public void keyPressed() {
		  if (key == 'r') {
		  	record = true;
		  }
	}
	

	 public static void main(String args[]) {
		    PApplet.main(new String[] { "--present", "CogsApplet" });
	 }
	

}
