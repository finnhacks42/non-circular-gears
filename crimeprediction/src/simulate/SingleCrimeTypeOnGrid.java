package simulate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/*** This class runs simulations of crime occurring on a grid with the option of a self excitation process ***/
public class SingleCrimeTypeOnGrid {
	private int gridWidth;
	private int gridHeight;
	float[] crimeProb;
	private Random r;
	private Map<Integer,ArrayList<Integer>> prevCrime;
	
	public SingleCrimeTypeOnGrid(int gridWidth, int gridHeight) {
		this.gridHeight = gridHeight;
		this.gridWidth = gridWidth;
		crimeProb = new float[gridWidth*gridHeight];
		prevCrime = new HashMap<Integer, ArrayList<Integer>>();
		r.setSeed(37l);
	}
	
	private void setUniformCrimeProb(float probPerTimeStep) {
		for (int i = 0; i < crimeProb.length; i ++) {
			crimeProb[i] = probPerTimeStep;
		}
	}
	
	private void simulate(int numDays, int timeStepHours) {
		int totalSteps = Math.round(numDays*24f/timeStepHours);
		for (int ts = 0; ts < totalSteps; ts ++) {
			int day = ts*timeStepHours/24;
			for (int l = 0; l < crimeProb.length; l ++) {
				if (r.nextFloat() < crimeProb[l]) { // we say a crime has occured at location l
					
				}
			}	
		}
	}
	
	
	
	
	

}
