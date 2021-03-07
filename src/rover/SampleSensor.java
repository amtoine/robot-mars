package rover;

import lejos.hardware.Button;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.geometry.Point;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.MovePilot;
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
	Chassis chassis;
	MovePilot pilot;

	public SampleSensor(int precision,UltraEyes us,MovePilot pilot,Chassis chassis) {
		this.precision = precision;
		this.us = us;
		this.pilot = pilot;
		this.chassis = chassis;
	}
	
	/**
	 * Searches for a sample by doing the specified rotation and stops when detects a sample 
	 * @param rotation the rotation to do to scan the area, in degrees
	 */
	public Point searchSamples(double rotation) {		
		pilot.setLinearSpeed(20);
		pilot.setAngularSpeed(40);
		
		PoseProvider odometer = chassis.getPoseProvider();
		Pose rover_pose;
				
		Point sample_pose = new Point(-50,0);
		Point detected_pose = new Point(-50,0);
		Measure dist = us.read();
		double rotated = 0;
		boolean sample_detected = false;

		while(!sample_detected && rotated<rotation) {
			pilot.rotate(precision);
			rotation = rotation + precision;
			rover_pose = odometer.getPose();
			dist = us.read();
			
			detected_pose = rover_pose.pointAt(dist.value,rover_pose.getHeading());
			detected_pose.x = (sample_pose.x+rover_pose.getX());
			detected_pose.y = (sample_pose.y+rover_pose.getY());
			
			if(map.inside(detected_pose) && !recup_zone.inside(detected_pose)) {
				sample_detected = true;
				sample_pose = detected_pose;
			}
		}

		return sample_pose;

	}
	
	public static void main(String[] args) {
		Rover rover = Rover.build();
		Wheel right_w = WheeledChassis.modelWheel(rover.rm.device, 4).offset(6.3);
		Wheel left_w = WheeledChassis.modelWheel(rover.lm.device, 4).offset(-6.3);
		Chassis chassis = new WheeledChassis(new Wheel[] {right_w, left_w}, WheeledChassis.TYPE_DIFFERENTIAL);
		
		MovePilot pilot = new MovePilot(chassis);
		
		SampleSensor sp_sensor = new SampleSensor(2,rover.us,pilot,chassis); //to be integrated in rover class as attribute?
		Point sp_pose = sp_sensor.searchSamples(360);
		
		if (map.inside(sp_pose)) {
			System.out.println("sample detected");
			System.out.println("x,y: " + sp_pose.x + "," + sp_pose.y);
		} else {
			System.out.println("no sample detected");
		}
		
		Button.waitForAnyPress();
	}

}
