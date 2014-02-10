package utilities;
/*** This class contains static array utilities functions. ***/
public class AU {
	/*** inserts the elements in insertion in the array target starting from the specified indx. 
	 * Returns the index directly after the last value inserted into the target array.***/
	public static int arrayInsert(int[] insertion ,int[] target, int indx) {
		int insertionIndx = indx;
		for (int value: insertion) {
			target[insertionIndx] = value;
			insertionIndx ++;
		}
		return insertionIndx;
	}
	
	/*** inserts the elements in insertion in the array target starting from the specified indx. 
	* Returns the index directly after the last value inserted into the target array.***/
	public static int arrayInsert(float[] insertion, float[] target, int indx) {
		int insertionIndx = indx;
		for (float value: insertion) {
			target[insertionIndx] = value;
			insertionIndx ++;
		}
		return insertionIndx;
	}

}
