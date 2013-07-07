import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*** This class calculates properties of a non-linear pair of gears based on the input of the radial function for an input gear. 
 *  The conjugate gear, transfer function, etc can be obtained through the relevant get methods. 
 *  This class is not thread safe - a single instance should not be shared by multiple threads. ***/
public class ConjugateGear {
	private float gearSeparation;
	private List<Float> movementFunction;
	private List<Float> transferFunction;
	private List<Float> radialFunction;
	

	/*** create a Conjugate gear to the input gear.
	 * @param gear1RadialFunction A list of doubles specifying the radius of the driving gear at evenly spaced intervals from 0 to 2PI
	 * @param tolerance a double indicating the tolerance the gear must be produced to. 
	 * The lower this value the greater the accuracy but the longer the gear will take to create. ***/
	public ConjugateGear(List<Float> gear1RadialFunction, double tolerance) {
		calculate(gear1RadialFunction,tolerance);
	}
	
	/*** This function generates a function representing the integral of the input radial function. 
	 * The results are stored in the input result array. ***/
	private static void calculateMovementFunction(List<Float> transferFunction, List<Float> resultArray){
		int nSteps = transferFunction.size();
		double stepSize = 2*Math.PI/((double)nSteps);
		float total = 0f;
		int indx = 0;
		for (double radius: transferFunction) {
			resultArray.set(indx, total);
			total += radius*stepSize;
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
	public void calculate(List<Float> gear1RadialFunction, double tolerance) {
		int nSteps = gear1RadialFunction.size();
		transferFunction = createZeroedArray(nSteps);
		movementFunction = createZeroedArray(nSteps);
		radialFunction = createZeroedArray(nSteps);
		
		//set the gear seperation to just larger than the maximum radius of the driving gear. This will be too small.
		gearSeparation = Collections.max(gear1RadialFunction) + .000000001f; 
		double difference = tolerance + 1;
		double increment = gearSeparation/2d;
		// variable stores the direction we were last moving the gear separation. Initially this will be up.
		boolean up = true; 
		
		while (Math.abs(difference) > tolerance) {
			calculateTransferFunction(gear1RadialFunction, gearSeparation, transferFunction);
			calculateMovementFunction(transferFunction, movementFunction);
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
				
		calculateRadialFunction(gear1RadialFunction, gearSeparation, radialFunction);
	}
	
	
	
	/*** Calculates the radial function for the driven gear. This must be equal to the separation minus the driving radius.***/
	private void calculateRadialFunction(List<Float> gear1RadialFunction, float gearSeparation, List<Float> resultArray) {
		for (int i = 0; i < gear1RadialFunction.size(); i++) {
			resultArray.set(i, gearSeparation - gear1RadialFunction.get(i));
		}
	}
	
	/*** This method calculates the transfer function of an input gear and gear separation. The results are stored in the input resultArray. ***/
	private void calculateTransferFunction(List<Float> gear1RadialFunction, float gearSeparation, List<Float> resultArray) {
		int indx = 0;
		for (float radius: gear1RadialFunction) {
			resultArray.set(indx, radius/(gearSeparation - radius)); 
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
