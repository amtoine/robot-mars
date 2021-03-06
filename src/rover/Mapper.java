package rover;

import lejos.hardware.Sound;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.geometry.Point;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.mapping.OccupancyGridMap;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.navigation.Pose;

/**
 * _____________________________________________TODO_____________________________________________
 * @author _____________________________________________TODO_____________________________________________
 *
 */
public class Mapper {

	/**
	 * _____________________________________________TODO_____________________________________________
	 * @param args _____________________________________________TODO_____________________________________________
	 */
	public static void main(String[] args) {
		EV3UltrasonicSensor us = new EV3UltrasonicSensor(SensorPort.S3);
		SampleProvider sp_us = us.getDistanceMode();
		us.enable();
		float[] dist = new float[sp_us.sampleSize()];
		
		Wheel right_w = WheeledChassis.modelWheel(Motor.C, 4).offset(6.3);
		Wheel left_w = WheeledChassis.modelWheel(Motor.B, 4).offset(-6.3);
		Chassis chassis = new WheeledChassis(new Wheel[] {right_w, left_w}, WheeledChassis.TYPE_DIFFERENTIAL);
		
		MovePilot rover = new MovePilot(chassis);
		rover.setAngularSpeed(40);

		PoseProvider odometer = chassis.getPoseProvider();
		Pose rover_pose = odometer.getPose();
		
		double res = 2.5;
		double freeThreshold = 2;
		double occupiedThreshold = 2;
		OccupancyGridMap map = new OccupancyGridMap(150,250,freeThreshold,occupiedThreshold,res); //in cm
		
		rover.rotate(-180);
		
		Point[] obstacles = new Point[1];

		for(int i=0;i<36;i++) {
			rover.rotate(10);
			sp_us.fetchSample(dist, i);
			System.out.println("dist " + i + ": " + dist[i]);
			Point obst = rover_pose.pointAt(dist[i],rover_pose.getHeading());
			obstacles[0] = new Point(obst.x+rover_pose.getX(),obst.y+rover_pose.getY()); //in meters
			obst.x = (obst.x+rover_pose.getX())*100;
			obst.y = (obst.y+rover_pose.getY())*100;
			map.setOccupied((int) (obst.x+rover_pose.getX())*100,(int) (obst.y+rover_pose.getY())*100, 1); 
			System.out.println("x: " + obstacles[0].x);
			System.out.println("y: " + obstacles[0].y);
		}
		
		us.close();
		
		/**for(int i=0;i<36;i++) {
			System.out.println(dist[i] + " ");
		}*/
		
		Sound.beep();

	}
}
