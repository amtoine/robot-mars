package examples;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.navigation.Pose;
import lejos.utility.Delay;

/**
 * Example of movements for an ev3 robot.
 */
public class Deplacement {
	
	/**
	 * Factoring method to make the code clearer
	 * 
	 * @param pose the current pose of the robot.
	 * @param i LCD drawing parameter.
	 */
	private static void screen(Pose pose, int i) {
		LCD.clear();
		LCD.drawString("a xpose: " + pose.getX(), 0, i);
		LCD.drawString("b ypose: " + pose.getY(), 0, i+1);
		LCD.drawString("c apose: " + pose.getHeading(), 0, i+2);
		LCD.refresh();
		Delay.msDelay(50);
		
		Button.LEDPattern(3);
		Button.waitForAnyPress();
		Button.LEDPattern(0);
	}

	/**
	 * Shows some movements examples for an ev3 robot.
	 * @param args java basic arguments for main methods.
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Wheel right_w = WheeledChassis.modelWheel(Motor.B, 5.6).offset(-6.3);
		Wheel left_w = WheeledChassis.modelWheel(Motor.C, 5.6).offset(6.3);
		Chassis chassis = new WheeledChassis(new Wheel[] {right_w, left_w}, WheeledChassis.TYPE_DIFFERENTIAL);
		
		MovePilot rover = new MovePilot(chassis);
		
		PoseProvider odometer = chassis.getPoseProvider();
		
		int i = 2;
		
		Sound.beepSequenceUp();
		
		LCD.clear();
		
		System.out.println("Init gyro... ");
		Button.LEDPattern(3);
		Button.waitForAnyEvent();
		Button.LEDPattern(0);
		
		rover.setLinearSpeed(30);
		
		rover.travel(50);
		screen(odometer.getPose(), i);
		
		rover.rotate(-90); // positive value rotates left 
		screen(odometer.getPose(), i);
		
		rover.travel(-50, true);
		screen(odometer.getPose(), i);
		
		rover.rotate(270); 
		screen(odometer.getPose(), i);
		
		rover.arc(4.712, 270, true); // if radius is zero, the robot rotates in place 
		screen(odometer.getPose(), i);
		
		Motor.A.setSpeed(20);
		Motor.A.setAcceleration(200);
		Motor.A.resetTachoCount();
		Motor.A.forward();
		Motor.A.stop();
		Motor.A.rotate(720, true);
		
		while (Motor.A.isMoving()) {
//			rover.rotate(360);
			Button.LEDPattern(4);
			Delay.msDelay(50);
		}
		
		rover.stop();
		Motor.A.stop();
		Motor.A.close();
		Button.LEDPattern(0);
		
		Sound.beep();
		
		
	}

}
