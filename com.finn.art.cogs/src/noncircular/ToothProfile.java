package noncircular;

import geomerative.RPoint;

import java.util.List;

public interface ToothProfile {
	public List<RPoint> getProfile(RPoint start, RPoint guide1, RPoint guide2, RPoint end);
}
