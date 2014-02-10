package gridfeatures;

/*** This class is an immutable represents the way in which crimes are broken down into groups (ie by category, by area, etc) with the exception of time. ***/
public class CrimeKey {
	private String areaNameSpace;
	private int area;
	private String category;
	private String categoryLevel;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + area;
		result = prime * result
				+ ((areaNameSpace == null) ? 0 : areaNameSpace.hashCode());
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
		if (areaNameSpace == null) {
			if (other.areaNameSpace != null)
				return false;
		} else if (!areaNameSpace.equals(other.areaNameSpace))
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

	
	
	/***
	 * 
	 * @param areaNameSpace used to separate out areas belong to different name-spaces (ie 200m grid, 1000m grid, reporting area) which might have the same id number)
	 * @param area
	 * @param category
	 * @param categoryLevel
	 */
	public CrimeKey(String areaNameSpace, int area, String category, String categoryLevel) {
		this.areaNameSpace = areaNameSpace;
		this.area = area;
		this.category = category;
		this.categoryLevel = categoryLevel;
	}
	
	

	
}
