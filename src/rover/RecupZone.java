package rover;

import lejos.robotics.geometry.Point;

class RecupZone extends MapZone{
	static final Point center = new Point((float) 0.71-initial_pose.getX(),(float) 0.116-initial_pose.getY());
	static final double diameter = 0.20;
	
	public boolean inside(Point p) {		
		double dist2 = Math.pow(p.x-center.x,2) + Math.pow(p.y-center.y,2);
		return dist2 < Math.pow(diameter/2,2);
	}
}
