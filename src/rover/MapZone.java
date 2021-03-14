package rover;

import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;

/**
 * Absolute frame: origin left down (starting zone side is down) corner of the map; X axis is defined along the length of the map
 * Relative frame: origin is the initial position of the rover, center of the starting zone; X axis is defined by initial rover heading, along the length of the map (2.5m)
 * All MapZones will be defined in relative frame.
 * Distances will be in cm.
 * 
 * @author Claire Ky
 *
 */
abstract class MapZone {
	/** The initial pose of the rover in the zone. */
	static final Pose initial_pose = new Pose(0.25f, 0.75f, 0); //in absolute frame
	/** The center of any extended version of a MapZone. */
	Point center;
	
	/**
	 * Any map extending MapZone should be able to tell whether is inside or outside the border defining them. 
	 * 
	 * @param p the point that one wants to know the belonging to the inside of the MapZone.
	 * @return true if the point is inside the MapZone, false otherwise.
	 */
	abstract boolean inside(Point p);

}
