package drm.taskworker.workers.geo;

import java.io.Serializable;

public class Region implements Serializable{

	private final RasterPoint upperleft;
	private final RasterPoint lowerRight;
	
	public Region(RasterPoint upperleft, RasterPoint lowerRight) {
		super();
		this.upperleft = upperleft;
		this.lowerRight = lowerRight;
		
		int width = lowerRight.getX() - upperleft.getX();
		if(width<0)
			throw new IllegalArgumentException("width is negative");
		
		
		int height = lowerRight.getY() - upperleft.getY();
		if(height<0)
			throw new IllegalArgumentException("height is negative");
			
	}

	public Region(RasterPoint point) {
		super();
		this.upperleft = point;
		this.lowerRight = new RasterPoint(point.getX()+1, point.getY()+1);
	}

	public Region(int i, int j, int k, int l) {
		this(new RasterPoint(i, j),new RasterPoint(k, l));
	}

	public Region(Region[] regs) {
		RasterPoint ul = null,lr = null; 
		for(Region r: regs){
			if(r==null)
				continue;
			ul = r.getUpperleft().upperLeftOf(ul);
			lr = r.getLowerRight().lowerRightOf(lr);
		}
		this.upperleft=ul;
		this.lowerRight=lr;
			
	}

	public RasterPoint getUpperleft() {
		return upperleft;
	}

	public RasterPoint getLowerRight() {
		return lowerRight;
	}
	
	public Region[] split(){
		int width = lowerRight.getX() - upperleft.getX();
		int height = lowerRight.getY() - upperleft.getY();
		
		int halfwayx = (width+1)/2;
		int halfwayy = (height+1)/2;
		
		RasterPoint middle = new RasterPoint(upperleft.getX()+halfwayx, upperleft.getY()+halfwayy);
		
		Region[] out = new Region[4];
		
		
		out[0]= new Region(upperleft, middle);
		out[1]= new Region(new RasterPoint(upperleft.getX()+halfwayx, upperleft.getY()), new RasterPoint(lowerRight.getX(), upperleft.getY()+halfwayy));
		out[2]= new Region(new RasterPoint(upperleft.getX(), upperleft.getY()+halfwayy), new RasterPoint(upperleft.getX()+halfwayx, lowerRight.getY()));
		out[3]= new Region(middle, lowerRight);
		
		return out;
	}
	
	public boolean isVoid(){
		return upperleft.getX() == lowerRight.getX() || upperleft.getY() == lowerRight.getY();
	}
	
	public boolean isPoint(){
		return upperleft.getX()+1==lowerRight.getX() && upperleft.getY()+1==lowerRight.getY();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((lowerRight == null) ? 0 : lowerRight.hashCode());
		result = prime * result
				+ ((upperleft == null) ? 0 : upperleft.hashCode());
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
		Region other = (Region) obj;
		if (lowerRight == null) {
			if (other.lowerRight != null)
				return false;
		} else if (!lowerRight.equals(other.lowerRight))
			return false;
		if (upperleft == null) {
			if (other.upperleft != null)
				return false;
		} else if (!upperleft.equals(other.upperleft))
			return false;
		return true;
	}

	public static Region parse(String in) {
		String[] parts = in.split(" ");
		int[] corners = new int[4];
		for (int i = 0; i < 4; i++) {
			corners[i]=Integer.parseInt(parts[i]);
		}
		return new Region(corners[0],corners[1],corners[2],corners[3]);
	}

	@Override
	public String toString() {
		return String.format("Region [%d %d %d %d]",getUpperleft().getX(),getUpperleft().getY(),getLowerRight().getX(),getLowerRight().getY());
	}

	public int getHeight() {
		return lowerRight.getY() - upperleft.getY();
	}

	public int getWidth() {
		return lowerRight.getX() - upperleft.getX();
	}
	

	
	
}
