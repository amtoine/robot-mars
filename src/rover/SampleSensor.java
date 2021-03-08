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
				
		Point[] sample_pose = {new Point(-1000,0),new Point(-1000,0)};
		Point detected_pose = new Point(-1000,0);
		Measure dist = us.read();
		double rotated = 0;
		int i = 0;

		while(rotated<rotation) {
			nav.rotateTo(2+rover_pose.getHeading());
			rotation = rotation + precision;
			rover_pose = odometer.getPose();
			dist = us.read();
			
			detected_pose = rover_pose.pointAt(dist.value,rover_pose.getHeading());
			
			if(map.inside(detected_pose) && !recup_zone.inside(detected_pose)) {
				if(relative) {
					detected_pose.x = (detected_pose.x-rover_pose.getX());
					detected_pose.y = (detected_pose.y-rover_pose.getY());
					sample_pose[i] = detected_pose;
				} else {
					sample_pose[i] = detected_pose;
				}
				i = 1;
			}
		}
		return sample_pose;
	}
	
//	public Point searchSample() {
//		pilot.setLinearSpeed(20);
//		pilot.setAngularSpeed(40);
//		
//		pilot.rotate(180);
//		Point sp_pose = scan(360);
//		if(map.inside(sp_pose)) {
//			return sp_pose;
//		} else {
//			PoseProvider odometer = chassis.getPoseProvider();
//			while(Math.abs(odometer.getPose().getHeading())>2) {
//				pilot.rotate(2);
//			}
//			pilot.forward();
//			pilot.travel(200);
//			pilot.rotate(180);
//			return scan(360);
//			
//		}
//		
//	}
	
	public static void main(String[] args) {
		Rover rover = Rover.build();
//		Wheel right_w = WheeledChassis.modelWheel(rover.rm.device, 4).offset(6.3);
//		Wheel left_w = WheeledChassis.modelWheel(rover.lm.device, 4).offset(-6.3);
//		Chassis chassis = new WheeledChassis(new Wheel[] {right_w, left_w}, WheeledChassis.TYPE_DIFFERENTIAL);
//		
//		MovePilot pilot = new MovePilot(chassis);
		
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
