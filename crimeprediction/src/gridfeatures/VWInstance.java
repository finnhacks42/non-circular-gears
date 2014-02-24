package gridfeatures;

import java.io.BufferedWriter;
import java.io.IOException;

public class VWInstance implements Instance {
	
	private StringBuilder builder;
	private BufferedWriter writer;
	
	public VWInstance(){
		builder = new StringBuilder();
	}
	
	/* (non-Javadoc)
	 * @see gridfeatures.Instance#setTarget(float)
	 */
	@Override
	public void setTarget(float target) {
		builder.insert(0, target+" ");
	}
	
	/* (non-Javadoc)
	 * @see gridfeatures.Instance#clear()
	 */
	@Override
	public void endInstanceAndclear() throws IOException {
		if (builder.charAt(builder.length() - 1) == ' ') {
			writer.write(builder.substring(0, builder.length() - 1));
		} else {
			writer.write(builder.toString());
		}
		writer.newLine();
		builder.delete(0, builder.length());
	}
	
	/*** set the namespace that all following features (until the next call to this function belong to. 
	 * A namespace must be set before adding features to produce valid VW output.***/
	@Override
	public void setNamespace(String namespace) {
		builder.append("|").append(namespace).append(" ");
	}
	
	/* (non-Javadoc)
	 * @see gridfeatures.Instance#addFeature(int, float)
	 */
	@Override
	public void addFeature(int featureID, float featureValue) {
		if (featureValue != 0) {
			builder.append(featureID).append(":").append(featureValue).append(" ");
		}
	}

	@Override
	public void setWriter(BufferedWriter writer, int ncols) {
		this.writer = writer;
	}

	@Override
	public void finishWriter() throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	
}