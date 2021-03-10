package rover;

import lejos.robotics.geometry.Point;

public class Map extends MapZone{

	static final Point center = new Point(1.25f-MapZone.initial_pose.getX(),0.75f-MapZone.initial_pose.getY());
	static final double width = 1.5;
	static final double length = 2.5;
	
	public boolean inside(Point p) {		
		return p.x>center.x-(length/2) && p.x<center.x+(length/2) && p.y>center.y-(width/2) && p.y<center.y+(width/2);
	}
}
