package drm.taskworker.workers.geo;

import java.util.Comparator;

public class RegionSort implements Comparator<Region>{

	@Override
	public int compare(Region o1, Region o2) {
		RasterPoint p1 = o1.getUpperleft();
		RasterPoint p2 = o2.getUpperleft();
		
		int yd = p1.getY() - p2.getY();
		if(yd!=0)
			return yd;
		return p1.getX() - p2.getX();
			
	}

}
