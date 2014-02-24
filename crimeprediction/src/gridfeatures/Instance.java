package gridfeatures;

import java.io.BufferedWriter;
import java.io.IOException;

public interface Instance {

	/*** sets the target variable for this instance. 
	 * @throws IOException ***/
	public abstract void setTarget(float target) throws IOException;

	/*** Terminate the current instance (ie add line terminator if required) and clear the current instance of data. 
	 * @throws IOException ***/
	public abstract void endInstanceAndclear() throws IOException;
	
	public abstract void setNamespace(String namespace);

	/*** Add a feature id, value pair. The featureID must be globally unique (ie not just unique within a namespace and must range from 1 to number of columns). 
	 * @throws IOException ***/
	public abstract void addFeature(int featureID, float featureValue) throws IOException;

	/*** write this instance to the specified writer.***/
	//public abstract void write(BufferedWriter writer) throws IOException;
	
	/*** Specify where you want output to go. 
	 * @throws IOException ***/
	public abstract void setWriter(BufferedWriter writer, int ncols) throws IOException ;
	
	/*** should be called when writing to this writer is complete. 
	 * @throws IOException ***/
	public abstract void finishWriter() throws IOException;
		
		
	
	

}