package examples;

import lejos.hardware.Sound;
import lejos.hardware.motor.Motor;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;

public class Navigation {

	public static void main(String[] args) {
		Wheel right_w = WheeledChassis.modelWheel(Motor.B, 4).offset(-6.3);
		Wheel left_w = WheeledChassis.modelWheel(Motor.C, 4).offset(6.3);
		Chassis chassis = new WheeledChassis(new Wheel[] {right_w, left_w}, WheeledChassis.TYPE_DIFFERENTIAL);
		
		MovePilot rover = new MovePilot(chassis);
		
		rover.setLinearSpeed(10);
		
		rover.setAngularSpeed(40);
		rover.travel(-10);
		Sound.beep();
		rover.rotate(-90);
		Sound.beep();
		rover.travel(-10);
		Sound.beep();
		
		while (rover.isMoving()) {
			Thread.yield();
		}
		
		rover.rotate(90);
		Sound.beep();
		rover.arc(40, -270);
		Sound.beep();
		rover.stop();
		
		Sound.beep();
		
	}

}
