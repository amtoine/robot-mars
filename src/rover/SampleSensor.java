package rover;

import lejos.hardware.Button;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.geometry.Point;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;

/**
 * During the exploration phase, the rover rotates to detect potential obstacles/objects
 * @author Claire Ky
 *
 */
public class SampleSensor {
	static final MapZone map = new Map();
	static final MapZone recup_zone = new RecupZone();
	static final double min_dist = 1;
	int precision;
	UltraEyes us;
	Navigator nav;
	PoseProvider odometer;

	public SampleSensor(int precision,UltraEyes us,PoseProvider odometer, Navigator nav) {
		this.precision = precision;
		this.us = us;
		this.nav = nav;
		this.odometer = odometer;
	}
	
	/**
	 * Searches for a sample by doing the specified rotation and stops when detects a sample 
	 * @param rotation the rotation to do to scan the area, in degrees
	 */
	public Point[] scan(double rotation,boolean relative) {	//TODO	
		Pose rover_pose = odometer.getPose();
				
		Point[] samples = {new Point(-1000,0),new Point(-1000,0)};
		Point detected_point = new Point(-1000,0);
		Measure dist = us.read();
		Measure last_dist;
		double rotated = 0;
		int i = 0;

		while(rotated<rotation) {
			nav.rotateTo(2+rover_pose.getHeading());
			rotation = rotation + precision;
			rover_pose = odometer.getPose();
			last_dist = dist;
			dist = us.read();
			
			detected_point = rover_pose.pointAt(dist.value,rover_pose.getHeading());
			
			if(map.inside(detected_point) && !recup_zone.inside(detected_point)) {
				if(Math.abs(last_dist.value-dist.value)<min_dist) {
					if(last_dist.value>dist.value) {
						samples[i-1] = Rover.convertPose(relative,detected_point,rover_pose);
					}
				} else {
					samples[i] = Rover.convertPose(relative,detected_point,rover_pose);
					i=1;
				}
			}
		}
		return samples;
	}
	
	public static void main(String[] args) {
		Rover rover = Rover.build();
		
		SampleSensor sp_sensor = new SampleSensor(2,rover.us,rover.nav.getPoseProvider(),rover.nav); //to be integrated in rover class as attribute?
		Point[] sp_pose = sp_sensor.scan(360,false);
		
		if (sp_pose[0].x==-50) {
			System.out.println("sample detected");
			System.out.println("x,y: " + sp_pose[0].x + "," + sp_pose[0].y);
		} else {
			System.out.println("no sample detected");
		}
		Button.waitForAnyPress();
	}

}
