package rover;

import lejos.robotics.geometry.Point;

public class Map extends MapZone{
	static final Point center = new Point((float) 1.25-initial_pose.getX(),(float) 0.75-initial_pose.getY());
	static final double width = 1.50;
	static final double length = 2.50;
	
	public boolean inside(Point p) {		
		return p.x>center.x-(length/2) && p.x<center.x+(length/2) && p.y>center.y-(width/2) && p.y<center.y+(width/2);
	}
}
