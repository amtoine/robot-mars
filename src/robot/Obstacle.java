package robot;

import lejos.hardware.Button;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.geometry.Point;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.navigation.Pose;

public class Obstacle {
	public static boolean inMap(Point p, Pose init_pose) {
		return p.x >= 0-init_pose.getX() && p.x <= 250-init_pose.getX() && p.y >= -75-init_pose.getY() && p.y <= 75-init_pose.getY(); //suppose initial position of rover is at center of starting zone
	}

	public static void main(String[] args) {
		EV3UltrasonicSensor us = new EV3UltrasonicSensor(SensorPort.S4);
		SampleProvider sp_us = us.getDistanceMode();
		us.enable();
		float[] dist = new float[sp_us.sampleSize()];
		
		Wheel right_w = WheeledChassis.modelWheel(Motor.C, 4).offset(6.3);
		Wheel left_w = WheeledChassis.modelWheel(Motor.B, 4).offset(-6.3);
		Chassis chassis = new WheeledChassis(new Wheel[] {right_w, left_w}, WheeledChassis.TYPE_DIFFERENTIAL);
		
		MovePilot rover = new MovePilot(chassis);
		rover.setLinearSpeed(20);
		rover.setAngularSpeed(40);

		PoseProvider odometer = chassis.getPoseProvider();
		Pose rover_pose;
		Pose init_pose_rover = odometer.getPose();
		
		//rover.rotate(-180);
		
		Point obst = new Point(-1,1);

		while(!Obstacle.inMap(obst,init_pose_rover)) {
			rover.rotate(2);
			rover_pose = odometer.getPose();
			sp_us.fetchSample(dist, 0);
			obst = rover_pose.pointAt(dist[0],rover_pose.getHeading());
			
			//obst.x = (obst.x+rover_pose.getX());
			//obst.y = (obst.y+rover_pose.getY());
		}
		
		System.out.println("dist: " + dist[0]);
		System.out.println("x,y: " + obst.x + "," + obst.y);
		Button.waitForAnyPress();
		
		//rover.forward();
		//rover.travel(dist[0]-5);

		us.close();

	}

}
