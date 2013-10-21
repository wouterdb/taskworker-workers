package drm.taskworker.workers.geo;

import java.io.Serializable;

public class RasterPoint implements Serializable{

	private final int x,y;

	public RasterPoint(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
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
		RasterPoint other = (RasterPoint) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("RasterPoint [x=%s, y=%s]", x, y);
	}

	public RasterPoint upperLeftOf(RasterPoint r) {
		if(r==null)
			return this;
		return new RasterPoint(Math.min(r.getX(), getX()), Math.min(r.getY(), getY()));
	}

	public RasterPoint lowerRightOf(RasterPoint r) {
		if(r==null)
			return this;
		return new RasterPoint(Math.max(r.getX(), getX()), Math.max(r.getY(), getY()));
	}
	
	
	
}
