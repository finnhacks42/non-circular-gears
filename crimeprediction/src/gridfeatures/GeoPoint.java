package gridfeatures;

/*** This class represents a point in latitude and longitude. ***/
public class GeoPoint {
	private static final double EARTH_RADIUS = 6371000; //earths radius in m
	private float lat;
	private float lon;

	public GeoPoint(float lat, float lon) {
		super();
		this.lat = lat;
		this.lon = lon;	
	}
	
	public String toString(){
		return "("+lat+","+lon+")";
	}
	
	public float getLat() {
		return lat;
	}
	
	public float getLon() {
		return lon;
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(lat);
		result = prime * result + Float.floatToIntBits(lon);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GeoPoint other = (GeoPoint) obj;
		if (Float.floatToIntBits(lat) != Float.floatToIntBits(other.lat))
			return false;
		if (Float.floatToIntBits(lon) != Float.floatToIntBits(other.lon))
			return false;
		return true;
	}
	
}
