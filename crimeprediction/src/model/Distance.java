package model;

/*** This class stores the distance from an area to a reference point.
 * Comparing two instances will order them based on their distance.***/
public class Distance implements Comparable<Distance>{

	private String area;
	private double distance;
	
	public Distance(String area, double distance) {
		this.area = area;
		this.distance = distance;
	}
	
	public String toString() {
		return area+":"+distance;
	}
	
	public String getArea() {
		return area;
	}

	public double getDistance() {
		return distance;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((area == null) ? 0 : area.hashCode());
		long temp;
		temp = Double.doubleToLongBits(distance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		Distance other = (Distance) obj;
		if (area == null) {
			if (other.area != null)
				return false;
		} else if (!area.equals(other.area))
			return false;
		if (Double.doubleToLongBits(distance) != Double
				.doubleToLongBits(other.distance))
			return false;
		return true;
	}

	@Override
	public int compareTo(Distance o) {
		double diff = distance - o.distance;
		if (diff < 0) {return -1;}
		if (diff == 0) {return 0;}
		return 1;
	}

	
	
	

}
