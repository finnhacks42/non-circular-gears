package noncircular;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static processing.core.PConstants.PI;

/*** This class calculates properties of a non-linear pair of gears based on the input of the radial function for an input gear. 
 *  The conjugate gear, transfer function, etc can be obtained through the relevant get methods. 
 *  This class is not thread safe - a single instance should not be shared by multiple threads. ***/
public class Conjugate {
	private float gearSeparation;
	private List<Float> movementFunction; //this array contains the angles phi of the second gear at corresponding indexes to the angles of the first gear.
	private List<Float> transferFunction; 
	private List<Float> radialFunction; //this array contains the radius of the second gear an indexes corresponding to the angles it is at (movement function)
	

	/*** create a Conjugate gear to the input gear.
	 * @param gear1RadialFunction A list of doubles specifying the radius of the driving gear at angles specified by @param angles
	 * @param tolerance a double indicating the tolerance the gear must be produced to. 
	 * The lower this value the greater the accuracy but the longer the gear will take to create. ***/
	public Conjugate(List<Float> gear1RadialFunction, List<Float> angles ,double tolerance) {
		if (gear1RadialFunction.size() != angles.size()) {
			throw new IllegalArgumentException("Number of radial points must match number of angles");
		}
		if (Collections.min(gear1RadialFunction) <= 0) {
			throw new IllegalArgumentException("The radius must always be greater than 0");
		}
		calculate(gear1RadialFunction,angles, tolerance);
		System.out.println("Conjugate calculated");
	}
	
	
	
	/*** This function generates a function representing the integral of the input radial function. 
	 * The results are stored in the input result array. ***/
	private static void calculateMovementFunction(List<Float> transferFunction, List<Float> angles,List<Float> resultArray){
		float total = 0f;
		int indx = 0;
		float dtheta = 0;
		float thetaPrev = angles.get(0);
		for (float radius: transferFunction) {
			float theta = angles.get(indx);
			dtheta = (theta - thetaPrev); //there is probably an issue here if the values of theta do not run from 0 - 2PI...
			if (dtheta < 0) {
				dtheta = dtheta + 2*PI;
			}
			total += radius*dtheta;
			resultArray.set(indx, total);
			thetaPrev = theta;
			indx ++;
		}	
	}
	
	
	private static List<Float> createZeroedArray(int size) {
		List<Float> result = new ArrayList<Float>(size);
		for (int i = 0; i < size; i++) {
			result.add(0f);
		}
		return result;
	}

	/*** This function calculates the gear separation, movement function and conjugate gear radial function for the input radial gear function. ***/
	public void calculate(List<Float> gear1RadialFunction, List<Float> angles, double tolerance) {
		int nSteps = gear1RadialFunction.size();
		transferFunction = createZeroedArray(nSteps);
		movementFunction = createZeroedArray(nSteps);
		radialFunction = createZeroedArray(nSteps);
		
		//set the gear seperation to just larger than the maximum radius of the driving gear. This will be too small.
		gearSeparation = Collections.max(gear1RadialFunction) + 1f; 
		double difference = tolerance + 1;
		double increment = gearSeparation/2d;
		// variable stores the direction we were last moving the gear separation. Initially this will be up.
		boolean up = true; 
		
		while (Math.abs(difference) > tolerance) {
			calculateTransferFunction(gear1RadialFunction, gearSeparation, transferFunction);
			calculateMovementFunction(transferFunction, angles, movementFunction);	
			double phiMax = movementFunction.get(movementFunction.size() - 1);
			difference = phiMax - Math.PI*2;
			if (difference > 0) { //phiMax is too large -> gear separation is too small
				if (!up) { //if we were previously going down then we are about to change direction, so we will halve the increment.
					increment = increment/2d; 
				}
				gearSeparation += increment;
				up = true;
			} else {
				if (up) { //about to change direction, halve the increment.
					increment = increment/2d;
				}
				gearSeparation -= increment;	
				up = false;
			}
		}
		//System.out.println(Collections.min(transferFunction));
		//System.out.println(Collections.min(movementFunction));
		calculateRadialFunction(gear1RadialFunction, gearSeparation, radialFunction);
		shiftMovementFunctionToCoordinateSystemOfGear1();
	}
	
	
	private void shiftMovementFunctionToCoordinateSystemOfGear1(){
//		Collections.reverse(movementFunction);
//		Collections.reverse(radialFunction);
//		int indx = 0;
//		for (Float phi:movementFunction) {
//			phi = PI - phi;
//			if (phi < 0) {
//				phi = phi + 2*PI;
//			}
//			
//			movementFunction.set(indx, phi);
//			indx ++;
//		}
	}
	
	/*** Calculates the radial function for the driven gear. This must be equal to the separation minus the driving radius.***/
	private void calculateRadialFunction(List<Float> gear1RadialFunction, float gearSeparation, List<Float> resultArray) {
		for (int i = 0; i < gear1RadialFunction.size(); i++) {
			resultArray.set(i, (gearSeparation - gear1RadialFunction.get(i)));
		}
	}
	
	/*** This method calculates the transfer function of an input gear and gear separation. The results are stored in the input resultArray. ***/
	private void calculateTransferFunction(List<Float> gear1RadialFunction, float gearSeparation, List<Float> resultArray) {
		int indx = 0;
		for (float radius: gear1RadialFunction) {
			float denominator = gearSeparation - radius;
			if (denominator <= 0) {
				throw new IllegalArgumentException("Gear separation cannot be smaller than or equal to any radius in the radial function.");
			}
			
			float value = radius/denominator;
			resultArray.set(indx, value); 
			indx ++;
		}
	}
	
	/*** Return the gear separation. ***/
	public float getGearSeparation(){
		return gearSeparation;
	}
	
	/*** Return the movement function.  ***/
	public List<Float> getMovementFunction(){
		return movementFunction;
	}
	
	/*** Return the transfer function. ***/
	public List<Float> getTransferFunction(){
		return transferFunction;
	}
	
	public List<Float> getRadialFunction(){
		return radialFunction;
	}

}
