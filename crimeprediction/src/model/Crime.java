package model;

import java.util.Comparator;

/*** Describes the occurance of a crime. ***/
public class Crime {

	// attributes in the raw data
	private String offenceId;
	private DateTime date;
	private Time dispatchTime;
	private Time startTime;
	private Time endTime;
	private String ucr;
	private String reportingArea;
	private String beat;
	private String premises;
	private String method;
	private String description;

	// calculated attributes
	private Category category;
	private GeoPoint point;
	private String ucrCat; // the first two digits of the ucr



	public Crime(String offenceId, DateTime date) {
		super();
		this.offenceId = offenceId;
		this.date = date;
	}
	
	/*** returns the category of the crime as represented by the first two digits of the ucr code. ***/
	public String getUcrCat(){
		return ucrCat;
	}

	public String toString() {
		return date.toString();
	}

	/*** set the geographical attributes by parsing them from a string. ***/
	public void parseGeo(String lat, String lon, String conf){
		try {
			this.point = new GeoPoint(Float.parseFloat(lat),Float.parseFloat(lon),Float.parseFloat(conf));
		} catch (NumberFormatException ex) {} //do nothing, null values are allowed	
	}


	public GeoPoint getPoint() {
		return this.point;
	}

	public String getOffenceId() {
		return offenceId;
	}

	public DateTime getDate() {
		return date;
	}

	public Time getDispatchTime() {
		return dispatchTime;
	}

	public Time getStartTime() {
		return startTime;
	}

	public Time getEndTime() {
		return endTime;
	}

	public String getUcr() {
		return ucr;
	}

	public String getReportingArea() {
		return reportingArea;
	}

	public String getBeat() {
		return beat;
	}

	public String getPremises() {
		return premises;
	}

	public String getMethod() {
		return method;
	}

	public String getDescription() {
		return description;
	}

	public Category getCategory() {
		return category;
	}

	public void setDate(DateTime time) {
		this.date = time;
	}

	public void setResonceTimes(Time dispatch, Time start, Time end) {
		this.dispatchTime = dispatch;
		this.startTime = start;
		this.endTime = end;
		//TODO calculate onScene and responceTime
	}


	public void setUcr(String ucr) {
		this.ucr = ucr;
		this.ucrCat = ucr.substring(0, 2);
		//TODO create category
	}

	public void setReportingArea(String reportingArea) {
		this.reportingArea = reportingArea;
	}

	public void setBeat(String beat) {
		this.beat = beat;
	}

	public void setPremises(String premises) {
		this.premises = premises;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	/*** compares two crimes by the dates on which they occured. ***/
	public static class DateComparator implements Comparator<Crime> {
		@Override
		public int compare(Crime o1, Crime o2) {
			return o1.getDate().compareTo(o2.getDate());
		}
	}

	/*** compares two crimes by their latitudes. ***/
	public static class LatComparator implements Comparator<Crime> {
		@Override
		public int compare(Crime o1, Crime o2) {
			float diff = o1.point.getLat() - o2.point.getLat();
			if (diff < 0f) {return -1;}
			else if (diff == 0f) {
				return 0;
			} else {
				return 1;
			}
		}
	}

	/*** compares two crimes by their latitudes. ***/
	public static class LonComparator implements Comparator<Crime> {
		@Override
		public int compare(Crime o1, Crime o2) {
			float diff = o1.point.getLon() - o2.point.getLon();
			if (diff < 0f) {return -1;}
			else if (diff == 0f) {
				return 0;
			} else {
				return 1;
			}
		}

	}





}
