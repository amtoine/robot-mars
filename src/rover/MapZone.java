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
	static final Pose initial_pose = new Pose((float) 0.25,(float) 0.75,0); //in absolute frame
	Point center;
	
	abstract boolean inside(Point p);

}
