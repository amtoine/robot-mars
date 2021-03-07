package rover;

import lejos.robotics.geometry.Point;

public class Map extends MapZone{
	static final Point center = new Point(125-initial_pose.getX(),75-initial_pose.getY());
	static final double width = 150;
	static final double length = 250;
	
	public boolean inside(Point p) {		
		return p.x>center.x-(length/2) && p.x<center.x+(length/2) && p.y>center.y-(width/2) && p.y<center.y+(width/2);
	}
}
