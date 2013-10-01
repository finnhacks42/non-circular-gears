package model;

/*** This class represents a point in latitude and longitude. ***/
public class GeoPoint {
	private static final double EARTH_RADIUS = 6371000; //earths radius in m
	private float lat;
	private float lon;
	private float conf;
	
	
	public GeoPoint(float lat, float lon, float conf) {
		super();
		this.lat = lat;
		this.lon = lon;
		this.conf = conf;
	}
	
	
	public String toString(){
		return "("+lat+","+lon+","+conf+")";
	}
	
	public float getLat() {
		return lat;
	}
	
	public float getLon() {
		return lon;
	}
	public float getConf() {
		return conf;
	}
	
	/*** returns the distance from this point to another in meters. 
	 * This is an approximation based on the haversign formula: http://www.movable-type.co.uk/scripts/latlong.html ***/
	public double distance(GeoPoint other) {
		double dLat = Math.toRadians(lat-other.lat);
		double dLon = Math.toRadians(lon-other.lon);
		double lat1 = Math.toRadians(lat);
		double lat2 = Math.toRadians(other.lat);
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double d = EARTH_RADIUS * c;
		return d;
	}
	
}
