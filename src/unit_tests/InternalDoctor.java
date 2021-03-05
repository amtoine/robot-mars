package unit_tests;

import lejos.hardware.Button;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import rover.Blinker;
import rover.Rover;

public class InternalDoctor {
	public static void init_peripherals(Rover rover) {
		try {
			Blinker.blink(Blinker.RED, Blinker.FAST, 0);
			rover.init_ultrasonic_sensor();
			Blinker.blink(Blinker.RED, Blinker.SLOW, 0);
			rover.init_color_sensor();
			Blinker.blink(Blinker.ORANGE, Blinker.FAST, 0);
			rover.init_right_motor();
			Blinker.blink(Blinker.ORANGE, Blinker.SLOW, 0);
			rover.init_left_motor();
			Blinker.blink(Blinker.GREEN, Blinker.FAST, 0);
			rover.init_pliers_motor();
			Blinker.blink(Blinker.GREEN, Blinker.SLOW, 0);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("press any button");
			Blinker.blink(Blinker.RED, Blinker.STILL, 0);
			Button.waitForAnyPress();
			System.exit(1);
		}
	}
	
//	public static void check_connect(RegulatedMotor motor) {
//	motor.setSpeed(1);
//	motor.rotate(1);
//	if (!motor.isMoving()) {
//		throw new IllegalArgumentException("motor does not respond.");
//	}
//}
}