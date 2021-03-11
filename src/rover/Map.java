package rover;

import lejos.robotics.geometry.Point;

public class Map extends MapZone{

	static final Point center = new Point(1.25f-MapZone.initial_pose.getX(),0.75f-MapZone.initial_pose.getY());
	static final float width = 1.5f;
	static final float length = 2.5f;
	
	public boolean inside(Point p) {		
		return p.x>center.x-(length/2) && p.x<center.x+(length/2) && p.y>center.y-(width/2) && p.y<center.y+(width/2);
	}
}
