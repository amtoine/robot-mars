package deployment;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import rover.Beeper;
import rover.Blinker;
import rover.Rover;

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
	 * @param args java arguments for main methods.
	 */
	public static void main(String[] args) {
		//###################################################################################################################
		//### full mission simulation #######################################################################################
		//###################################################################################################################
		Rover rover = Rover.build(SensorPort.S4, SensorPort.S1,
				                  MotorPort.A, MotorPort.B, MotorPort.C);
		rover.land();
		rover.checkBattery();
		rover.init_peripherals();
		
		rover.explore(); rover.harvest(); rover.checkBattery(); rover.await();
		rover.explore(); rover.harvest(); rover.checkBattery(); rover.await();
		rover.explore(); rover.harvest(); rover.checkBattery(); rover.sleep();
		
		//###################################################################################################################
		//### color sensor tests ############################################################################################
		//###################################################################################################################
		float dist;
		while (Button.readButtons() != Button.ID_ENTER) {
			dist = rover.take_us_measure();
			System.out.println(dist);
		}
		
		//###################################################################################################################
		//### color sensor tests ############################################################################################
		//###################################################################################################################
		int id = -1;
		while (Button.readButtons() != Button.ID_ENTER) {
			LCD.clear();
			id = rover.take_cs_measure();
			System.out.println("id: " + id);
		}
		
		//###################################################################################################################
		//### motor tests ###################################################################################################
		//###################################################################################################################
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
