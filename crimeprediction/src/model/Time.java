package model;

/*** basic class for representing times without dates. ***/
public class Time {
	private long nseconds = 0;
	
	private Time(long nseconds) {
		this.nseconds = nseconds;
	}
	
	/*** Parse a time from a string assumed to be either hh:mm:ss or hh:mm. Returns null if the string is not a valid time. ***/
	public static Time parseTime(String s) {
		if (s.length() != 5 && s.length() != 8) {return null;}
		int hours = 0;
		int mins = 0;
		int secs = 0;
	
		if (s.length() == 8) {
			hours = Integer.valueOf(s.substring(0, 2));
			mins = Integer.valueOf(s.substring(3, 5));
			secs = Integer.valueOf(s.substring(6));
				
		} else if (s.length() == 5) {
			hours = Integer.valueOf(s.substring(0, 2));
			mins = Integer.valueOf(s.substring(3));	
		}
		
		long nseconds = 0;
		nseconds += secs;
		nseconds += mins*60l;
		nseconds += hours*60l*60l;
		return new Time(nseconds);
	}
	
	public long getSeconds(){
		return nseconds;
	}

}
