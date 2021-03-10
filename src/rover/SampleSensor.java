package rover;

import java.util.Arrays;

import lejos.robotics.geometry.Point;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import tools.Beeper;

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
	Point[] samples;

	public SampleSensor(int precision,UltraEyes us,PoseProvider odometer, Navigator nav) {
		this.precision = precision;
		this.us = us;
		this.nav = nav;
		this.odometer = odometer;
		this.samples = new Point[] {new Point(-1000,0),new Point(-1000,0)};
		System.out.println(SampleSensor.map.center);;
	}
	
	/**
	 * Searches for a sample by doing the specified rotation and stops when detects a sample 
	 * @param rotation the rotation to do to scan the area, in degrees
	 */
	public void scan(double rotation,boolean relative, Rover rover) {	//TODO	
		Pose rover_pose = odometer.getPose();

		Point detected_point = new Point(-1000,0);
		Measure dist = this.us.read();
		Measure last_dist;
		double rotated = 0;
		int i = 0;
		int last_index = 0;

		while(rotated<rotation) {
			this.nav.rotateTo(this.precision+rover_pose.getHeading());
			rotated += this.precision;
			rover_pose = this.odometer.getPose();
			rover.logger.println("rover pose: (x:" + rover_pose.getX() + ", y:" + rover_pose.getY() + ", h: " + rover_pose.getHeading());
			last_dist = dist;
			dist = this.us.read();
			rover.logger.println("d: " + dist.value);

			rover_pose.getLocation().pointAt(i, i);
			detected_point = rover_pose.pointAt(dist.value,rover_pose.getHeading());

			rover.logger.println("point: " + detected_point.toString());
			
			boolean m = map.inside(detected_point);
			boolean r = recup_zone.inside(detected_point);
			rover.logger.println("map: " + ((m)? "in":"out") + ", rec: " + ((r)? "in":"out"));
			if(m && !r) {
				if(map.inside(detected_point) && !recup_zone.inside(detected_point)) {
					if(detected_point.subtract(samples[last_index]).length()<min_dist && last_dist.value>dist.value) {
						this.samples[last_index] = Rover.convertPose(relative,detected_point,rover_pose);
					} else {
						this.samples[i] = Rover.convertPose(relative,detected_point,rover_pose);
						last_index = i;
					i=1;
					}
				}
			}
		rover.logger.println(Arrays.deepToString(this.samples));
		}
	}
	
//	public static void main(String[] args) {
//		Rover rover = Rover.build();
//		
//		SampleSensor sp_sensor = new SampleSensor(2,rover.us,rover.nav.getPoseProvider(),rover.nav);
//		sp_sensor.scan(360,false);
//		
//		if (sp_sensor.samples[0].x==-1000) {
//			System.out.println("sample detected");
//			System.out.println("x,y: " + sp_sensor.samples[0].x + "," + sp_sensor.samples[0].y);
//		} else {
//			System.out.println("no sample detected");
//		}
//		Button.waitForAnyPress();
//	}

}
