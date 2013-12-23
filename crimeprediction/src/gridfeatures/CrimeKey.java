package gridfeatures;

/*** This class is an immutable represents the way in which crimes are broken down into groups (ie by category, by area, etc) with the exception of time. ***/
public class CrimeKey {
	private int area;
	private String category;
	private String categoryLevel;
	
	public CrimeKey(int area, String category, String categoryLevel) {
		this.area = area;
		this.category = category;
		this.categoryLevel = categoryLevel;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + area;
		result = prime * result
				+ ((category == null) ? 0 : category.hashCode());
		result = prime * result
				+ ((categoryLevel == null) ? 0 : categoryLevel.hashCode());
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
		CrimeKey other = (CrimeKey) obj;
		if (area != other.area)
			return false;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (categoryLevel == null) {
			if (other.categoryLevel != null)
				return false;
		} else if (!categoryLevel.equals(other.categoryLevel))
			return false;
		return true;
	}

}
