package gridfeatures;

import java.io.BufferedWriter;
import java.io.IOException;

/*** Outputs rows in matrix market format. This is a sparse format that can be read by R, GraphLab and more. ***/
public class MatrixMarketInstance implements Instance {
	private static final String HEADER = "%%MatrixMarket matrix coordinate real general";
	private static final String NEWLINE = "\n";
	private StringBuilder b;
	private int row = 1;
	private BufferedWriter writer;
	private int numEntries = 0;
	private int ncols;
	
	@Override
	public void setTarget(float target) throws IOException {
		addFeature(1,target);
	}

	@Override
	public void endInstanceAndclear() throws IOException {
		row ++;
	}

	@Override
	public void setNamespace(String namespace) {}
		

	@Override
	public void addFeature(int featureID, float featureValue) throws IOException {
		if (featureValue != 0) {
			b.append(row).append(" ").append(featureID+1).append(" ").append(featureValue).append(NEWLINE);
			//writer.write(row+" "+(featureID + 1)+" "+featureValue);
			//writer.newLine();
			numEntries ++;
		}
	}

	@Override
	public void setWriter(BufferedWriter writer, int numColumns) throws IOException {
		b = new StringBuilder();
		b.append(HEADER).append(NEWLINE);
		
		this.writer = writer;
		this.row = 1;
		this.numEntries = 0;
		this.ncols = numColumns;
	}

	@Override
	public void finishWriter() throws IOException {
		if (this.writer != null) { //do what we need to do to terminate the file for the current writer.
			int offset = HEADER.length()+NEWLINE.length();
			b.insert(offset,row+" "+ncols+" "+numEntries+NEWLINE);
			writer.write(b.toString());
		}
	}
	
	

	
	
	
	
}