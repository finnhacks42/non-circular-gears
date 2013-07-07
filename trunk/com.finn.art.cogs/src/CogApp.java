import geomerative.RG;
import geomerative.RPoint;
import geomerative.RShape;

import java.awt.Color;
import java.util.List;

import processing.core.PApplet;



public class CogApp extends PApplet {
	GearShape gear1; 
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
	ConjugateGear conjugate = null;
	
	
	

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
		} else if (conjugate == null){
			conjugate = new ConjugateGear(gear1.getRadii(), 0.00001);
			gear2 = new GearShape(this);
			gear2.setProfile(conjugate.getRadialFunction(),conjugate.getMovementFunction());
			
			
			
		} else {
			gear2.draw(conjugate.getGearSeparation(), 0);
			noLoop(); //here we want to simulate the gears moving ...
			
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
