import geomerative.RG;
import geomerative.RPoint;
import geomerative.RShape;

import java.awt.Color;
import java.util.List;

import processing.core.PApplet;



public class CogApp extends PApplet {
	private GearShape gear1; 
	private GearShape gear2;
	private boolean record;
	
	private List<Float> radii;
	private List<RPoint> points;
	private List<Float> angles;
	private int numAngles;
	private float gear1Angle = 0;

	private RPoint prevPoint = null;
	private RPoint prevCutterOrigin = new RPoint(0,0);
	//private float cutterRadius = 0f;

	private Cutter cutter;
	private RShape cutShape;
	private int numCutterAngles = 1000;
	private int loop = 0;
	
	private ConjugateGear conjugate = null;
	private List<Float> movementFunc;
	private float gear2Angle = 0;
	
	

	
	
	

	@Override
	public void setup() {	
		size(1000,1000, P3D);
		RG.init(this);
		gear1 = new GearShape(this);
		gear1.setSinusoidalProfile(200, 80, 2, 500);
		//gear1.setNonSymetricProfile(1000, 100, 30);
		radii = gear1.getRadii();
		points = gear1.getPoints();
		numAngles = radii.size();
		cutter = new Cutter(gear1.getShape().getCurveLength(), 10, 3, 7, 100, this);
		cutShape = cutter.getShape();
		angles = gear1.getAngles();

	}
	
	
	private void animateCut(int loop) {
		RPoint p = points.get(loop);
		RPoint norm = gear1.getNorms().get(loop);
		RPoint scaledNorm = new RPoint(norm);
		scaledNorm.scale(cutter.getRadius());

		
		//translate the cutter.
		RPoint cutterOrigin = new RPoint(p);
		cutterOrigin.add(scaledNorm);
		RPoint trans = new RPoint(cutterOrigin);
		trans.sub(prevCutterOrigin);
		cutShape.translate(trans);
		
		line(0,0,cutterOrigin.x,cutterOrigin.y);

		if (prevPoint != null) {
			float dist = p.dist(prevPoint);
			
			//actually rotate the cutter here ...	
			cutShape.rotate(dist/(float)cutter.getRadius(),cutterOrigin);
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
		gear1.drawRadiiFunction();
	}
	
	public void draw() {
		background(Color.WHITE.getRGB());
		translate(width/4,height/2);
		
		if (loop < numAngles) {
			animateCut(loop);
			loop ++;
		} else if (conjugate == null){
			System.out.println("Calculating conjugate");
			gear1.setProfile(gear1.getRadii(),gear1.getAngles()); //reset the state of gear1 using its updated radii.
			conjugate = new ConjugateGear(gear1.getRadii(),gear1.getAngles(), 0.00001);
			movementFunc = conjugate.getMovementFunction();
			gear2 = new GearShape(this);
			gear2.setProfile(conjugate.getRadialFunction(),conjugate.getMovementFunction());
			gear2.flip();
			gear2.translate(conjugate.getGearSeparation(), 0);
			//gear1.allignWithAxis();
			gear2.draw();
			System.out.println("Conjugate calculated!");
			
		} else {
			gear1.draw();
			gear2.draw();
			
			
			//gear1.rotate(PI/600f);
			//gear2.rotate(-PI/600f);
			
			
		}
		

		
		
		
	}
	
	/*** record the data to a dxf file if r is pressed. ***/
	public void keyPressed() {
		  if (key == 'r') {
		  	record = true;
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
	
	


	 public static void main(String args[]) {
		    PApplet.main(new String[] { "--present", "CogsApplet" });
	 }
	

}
