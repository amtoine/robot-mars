package rover;

import lejos.robotics.geometry.Point;

class RecupZone extends MapZone{
	static final Point center = new Point(0.71f,1.16f);
	static final float diameter = 0.20f;
	
	public boolean inside(Point p) {		
		double dist2 = Math.pow(p.x-center.x,2) + Math.pow(p.y-center.y,2);
		return dist2 < Math.pow(diameter/2,2);
	}
}
