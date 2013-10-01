package model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class DateTime implements Comparable<DateTime> {
	

	private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
	private GregorianCalendar calendar;
	private DateTime(){
		this.calendar = new GregorianCalendar();
	}
	
	/*** parse a datetime from the format MM/DD/YYYY 
	 *  ***/
	public static DateTime parse(String s) {
		try {
	    	DateTime dt = new DateTime();
	    	dt.calendar.setTime(sdf.parse(s));
	    	return dt;
		} catch (ParseException ex) {return  null;}
		
	}

	@Override
	public int compareTo(DateTime o) {
		return this.calendar.compareTo(o.calendar);
	}
	
	public String toString() {
		return sdf.format(calendar.getTime());
	}
	
	@Override
	public int hashCode() {
		return calendar.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {return true;}
		if (obj == null){return false;}
		if (getClass() != obj.getClass()){return false;}
		DateTime other = (DateTime) obj;
		return calendar.equals(other.calendar);
	}
	

}
