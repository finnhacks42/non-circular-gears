package feature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CSVRead {
	private BufferedReader reader;
	private String sep;
	
	/*** A dumb CSV reader. Assumes no instances of the separator within fields, and assums the quote char is a " ***/
	public CSVRead(File f, String sep) throws FileNotFoundException {
		reader = new BufferedReader(new FileReader(f));
		this.sep = sep;
	}
	
	public String[] readNext() throws IOException {
		String line = reader.readLine();
		if (line == null) {return null;}
		String[] fields = line.split(sep);
		String[] fields2 = new String[fields.length];
		for (int i = 0; i < fields.length; i ++) {
			String s = fields[i];
			fields2[i] = s.replaceFirst("^\"+", "").replaceFirst("\"+$", "");
		}
		return fields2;
	}
	
	public void close() throws IOException{
		reader.close();
	}

}
