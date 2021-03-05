package robot;

import lejos.hardware.Sound;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;

public class Mapper {

	public static void main(String[] args) {
		EV3UltrasonicSensor us = new EV3UltrasonicSensor(SensorPort.S3);
		SampleProvider sp_us = us.getDistanceMode();
		us.enable();
		float[] dist = new float[sp_us.sampleSize()];
		
		Wheel right_w = WheeledChassis.modelWheel(Motor.C, 4).offset(6.3);
		Wheel left_w = WheeledChassis.modelWheel(Motor.B, 4).offset(-6.3);
		Chassis chassis = new WheeledChassis(new Wheel[] {right_w, left_w}, WheeledChassis.TYPE_DIFFERENTIAL);
		
		MovePilot rover = new MovePilot(chassis);
		
		//rover.setLinearSpeed(20);
		rover.setAngularSpeed(40);
		
		for(int i=0;i<36;i++) {
			rover.rotate(10);
			sp_us.fetchSample(dist, 0);
			System.out.println("dist " + i + ": " + dist[0]);
		}
		
		us.close();
		
		Sound.beep();

	}

}
