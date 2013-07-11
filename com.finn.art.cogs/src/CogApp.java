import geomerative.RG;
import geomerative.RPoint;
import geomerative.RShape;

import java.awt.Color;
import java.io.File;
import java.util.List;

import processing.core.PApplet;



public class CogApp extends PApplet {
	private static final int STATE_CUT = 0;
	private static final int STATE_CONJUGATE = 1;
	private static final int STATE_SIMULATE = 2;
	
	private int state = STATE_CONJUGATE;
	
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
	private Cutter cutter;
	private int numCutterAngles = 1000;
	private int loop = 0;
	
	private ConjugateGear conjugate = null;
	private List<Float> movementFunc;
	private float gear2Angle = 0;
	
	private RShape xAxis;
	

	@Override
	public void setup() {	
		size(1000,1000, P3D);
		RG.init(this);
		gear1 = new GearShape(this);
		//gear1.setSinusoidalProfile(200, 80, 5, 1000);
		gear1.setNonSymetricProfile(1000, 100, 30);
		//gear1.setProfile(new File("/home/finn/programming/hacking/non_circ_gears/start1.svg"), 100);
		radii = gear1.getRadii();
		points = gear1.getPoints();
		numAngles = radii.size();
		cutter = new Cutter(gear1.getShape().getCurveLength(), 30, 3, 15, 1000, this);
		angles = gear1.getAngles();
		gear1.cutTeeth();
		
		xAxis = new RShape();
		xAxis.addMoveTo(-1000,0);
		xAxis.addLineTo(1000,0);

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
		cutter.getShape().translate(trans);
		
		line(0,0,cutterOrigin.x,cutterOrigin.y);

		if (prevPoint != null) {
			float dist = p.dist(prevPoint);
			//actually rotate the cutter here ...	
			cutter.getShape().rotate(dist/(float)cutter.getRadius(),cutterOrigin);
		}

		for (int j = 0; j < numCutterAngles; j ++) {
			float adv = j/(float)numCutterAngles;
			RPoint cutterPoint = cutter.getShape().getPoint(adv);
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
		
		switch (state) {
		case STATE_CUT:
			animateCut(loop);
			loop ++;
			if (loop == numAngles - 1) {
				state = STATE_CONJUGATE;
			}
			break;
		case STATE_CONJUGATE:
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
			loop = 0;
			state = STATE_SIMULATE;
			break;
		case STATE_SIMULATE:
			
//			float dtheta = 2*PI/numAngles;//angles.get(loop) - gear1Angle;
//			float dphi = movementFunc.get(loop) - gear2Angle;
			//System.out.println(loop+","+dtheta+","+dphi);
			
//			gear1.rotate(dtheta);
//			gear2.rotate(-dphi);
//			loop = (loop+1)%numAngles;
//			gear1Angle += dtheta;
//			gear2Angle += dphi;
			
			 if (record) {
				    beginRaw(DXF, "/home/finn/programming/hacking/non_circ_gears/output.dxf");
				    gear1.draw();
					gear2.translate(30, 0);
					gear2.draw();
					ellipse(0,300,20,20);
					ellipse(conjugate.getGearSeparation(),300,20,20);
				    endRaw();
				    record = false;
				    System.out.println("DXF written");
				    noLoop();
			 }
			 
			 gear1.draw();
			 gear2.draw();
			
			if (record) {
				
			}

			
			break;
		default:
			throw new IllegalStateException("Unexpected fall through to default state");
		}
			
		
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
