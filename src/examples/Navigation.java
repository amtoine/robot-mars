package examples;

import lejos.hardware.Sound;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;

public class Navigation {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		EV3UltrasonicSensor us = new EV3UltrasonicSensor(SensorPort.S3);
		SampleProvider sp_us = us.getDistanceMode();
		us.enable();
		
		Wheel right_w = WheeledChassis.modelWheel(Motor.A, 5.6).offset(-6.3);
		Wheel left_w = WheeledChassis.modelWheel(Motor.B, 5.6).offset(6.3);
		Chassis chassis = new WheeledChassis(new Wheel[] {right_w, left_w}, WheeledChassis.TYPE_DIFFERENTIAL);
		
		MovePilot rover = new MovePilot(chassis);
		
		rover.setLinearSpeed(20);
		
		rover.setAngularSpeed(40);
		rover.travel(50);
		rover.rotate(-90);
		rover.travel(-50, true);
		
		while (rover.isMoving()) {
			Thread.yield();
		}
		
		rover.rotate(-90);
		rover.arc(4.712, 270, true);
		rover.stop();
		
		Sound.beep();
		
	}

}
