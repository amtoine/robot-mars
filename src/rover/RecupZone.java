package rover;

import lejos.robotics.geometry.Point;

/**
 * Extended version of an abstract MapZone.
 * It represents the circular recovery zone inside the intervention zone of the project.
 * 
 * @author Antoine Stevan
 *
 */
class RecupZone extends MapZone{
	/** The center of the recovery zone is given in the specifications. */
	static final Point center = new Point(0.71f,1.16f);
	/** The diameter of the recovery zone is given in the specifications. */
	static final float diameter = 0.20f;
	
	/**
	 * Tells if a point is inside the recovery zone.
	 * 
	 * @param p the point that one wants to know the belonging to the inside of the recovery zone.
	 * @return true if the point is inside the recovery zone, false otherwise.
	 */
	public boolean inside(Point p) {		
		double dist2 = Math.pow(p.x-center.x,2) + Math.pow(p.y-center.y,2);
		return dist2 < Math.pow(diameter/2,2);
	}
}
