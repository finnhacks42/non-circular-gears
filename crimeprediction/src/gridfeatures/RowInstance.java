package gridfeatures;

import java.io.BufferedWriter;
import java.io.IOException;

import utilities.SU;

/*** This class represents an instance in standard full row format (non-sparse). ***/
public class RowInstance implements Instance {
	private float[] features;
	private String[] header;
	private String namespace;
	private boolean includeArea;
	private BufferedWriter writer;
	
	public RowInstance(String[] header, boolean includeArea) {
		this.header = header;
		this.features = new float[header.length];
		this.includeArea = includeArea;
	}
	
	public void writeHeader(BufferedWriter writer) throws IOException {
		writer.write(SU.join(",",header));
		writer.newLine();
	}
	

	@Override
	public void setTarget(float target) {
		features[0] = target;
		
	}

	@Override
	public void endInstanceAndclear() throws IOException {
		String row = SU.join(",",features);
		writer.write(row);	
		writer.newLine();
		for (int i = 0; i < features.length; i++) {
			features[i] = 0f; 
		}
		
	}

	@Override
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	private int getIndx(int featureID, String namespace) {
		if (includeArea && namespace.equals(DataLoader.AREA)) {
			return 1;
		}
		if (includeArea) {
			return featureID +1;
		}
		return featureID;
	}

	@Override
	public void addFeature(int featureID, float featureValue) {
		int indx = getIndx(featureID, namespace);
		features[indx] = featureValue;
	}

	@Override
	public void setWriter(BufferedWriter writer, int ncols) throws IOException {
		this.writer = writer;
		writeHeader(writer);
	}

	@Override
	public void finishWriter() throws IOException {
		// TODO Auto-generated method stub
		
	}

	

}
