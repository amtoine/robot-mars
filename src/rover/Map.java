package rover;

import lejos.robotics.geometry.Point;

/**
 * Extended version of an abstract MapZone.
 * It represents the whole intervention zone of the project.
 * 
 * @author Antoine Stevan
 *
 */
public class Map extends MapZone{
	/** The center of the intervention zone is given in the specifications. */
	static final Point center = new Point(1.25f,0.75f);
	/** The width of the intervention zone is given in the specifications. */
	static final float width = 1.5f;
	/** The length of the intervention zone is given in the specifications. */
	static final float length = 2.5f;
	
	/**
	 * Tells if a point is inside the intervention zone.
	 * 
	 * @param p the point that one wants to know the belonging to the inside of the intervention zone.
	 * @return true if the point is inside the intevention zone, false otherwise.
	 */
	public boolean inside(Point p) {		
		return p.x>center.x-(length/2) && p.x<center.x+(length/2) && p.y>center.y-(width/2) && p.y<center.y+(width/2);
	}
}
