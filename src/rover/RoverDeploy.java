package rover;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.SampleProvider;
import modes.LandingMode;
import modes.RoverMode;
import unit_tests.InternalDoctor;

/**
 * A simple sequential main class to test the rover in its environment.
 * 
 * @author Antoine Stevan
 *
 */
public class RoverDeploy {

	/**
	 * Main method to test the rover in its environment.
	 * 
	 * @param args unused java arguments for main methods.
	 */
	public static void main(String[] args) {
		Rover rover = new Rover(SensorPort.S4, SensorPort.S1,
				                MotorPort.A, MotorPort.B, MotorPort.C);
		
		RoverMode mode = new LandingMode();
		mode.start(); mode.stop();
		
		InternalDoctor.init_peripherals(rover);
		
		// ultrasonic sensor
		rover.get_ultrasonic_sensor().enable();
		SampleProvider sp_us = rover.get_ultrasonic_sensor().getDistanceMode();
		float[] dist = new float[sp_us.sampleSize()];
		
		while (Button.readButtons() != Button.ID_ENTER) {
			sp_us.fetchSample(dist, 0);
			System.out.println(dist[0]);
		}
		
		// color sensor
		int id = -1;
		SensorMode red, rgb;
		while (Button.readButtons() != Button.ID_ENTER) {
			LCD.clear();
			id = rover.get_color_sensor().getColorID();
			red = rover.get_color_sensor().getRedMode();
			rgb = rover.get_color_sensor().getRGBMode();
			System.out.println("id: " + id);
			System.out.println("red: " + red.getName());
			System.out.println("rgb: " + rgb.getName());
		}
		
		// motor tests
		Blinker.blink(Blinker.ORANGE, Blinker.FAST, 0);
		Button.waitForAnyPress();
		Beeper.beep();
		
		rover.get_left_motor().setSpeed(90);
		rover.get_left_motor().rotate(360);
		Blinker.blink(Blinker.ORANGE, Blinker.SLOW, 0);
		Button.waitForAnyPress();
		Beeper.beep();

		rover.get_right_motor().setSpeed(90);
		rover.get_right_motor().rotate(360);
		Blinker.blink(Blinker.GREEN, Blinker.SLOW, 0);
		Button.waitForAnyPress();
		Beeper.beep();
		
		rover.get_pliers_motor().setSpeed(90);
		rover.get_pliers_motor().rotate(360);
		Blinker.blink(Blinker.GREEN, Blinker.STILL, 0);
		Button.waitForAnyPress();
		Beeper.twoBeeps();
	}

}
