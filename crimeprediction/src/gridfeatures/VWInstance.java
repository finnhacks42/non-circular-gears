package gridfeatures;

import java.io.BufferedWriter;
import java.io.IOException;

public class VWInstance {
	
	private StringBuilder builder;
	
	public VWInstance(){
		builder = new StringBuilder();
	}
	
	/*** sets the target variable for this instance. ***/
	public void setTarget(float target) {
		builder.insert(0, target+" ");
	}
	
	/*** clear the current instance of data. ***/
	public void clear(){
		builder.delete(0, builder.length());
	}
	
	/*** set the namespace that all following features (until the next call to this function belong to. 
	 * A namespace must be set before adding features to prduce valid VW output.***/
	public void setNamespace(String namespace) {
		builder.append("|").append(namespace).append(" ");
	}
	
	/*** Add a feature id, value pair. The representation is sparce so only non-zero valued features will be included in the output. ***/
	public void addFeature(int featureID, float featureValue) {
		if (featureValue != 0) {
			builder.append(featureID).append(":").append(featureValue).append(" ");
		}
	}
	
	/*** write this instance to the specified writer as a row in VW format. ***/
	public void write(BufferedWriter writer) throws IOException {
		if (builder.charAt(builder.length() - 1) == ' ') {
			writer.write(builder.substring(0, builder.length() - 1));
		} else {
			writer.write(builder.toString());
		}
	}
	
}