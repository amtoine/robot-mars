package unit_tests;

import modes.DiagnosticMode;
import modes.ErrorMode;
import modes.RoverMode;
import rover.Beeper;
import rover.Rover;

/**
 * A wrapper to initialize all the peripherals of the rover.
 * 
 * @author Antoine Stevan
 * 
 */
public class InternalDoctor {
	/**
	 * Used to initialize all the peripherals of the rover.
	 * Initializes all the used peripherals of the rover, i.e. the two motors for the tracks, the motor that controls the
	 * pliers, the color sensor and the ultrasonic sensor. When one or more of the above listed peripherals does not respond
	 * the error is handled and eventually stops the execution of the mission. It is sad, but the rover cannot accomplish its
	 * mission with event one motor down.
	 * 
	 * @param rover the rover that needs some initialization and checks.
	 */
	public static void init_peripherals(Rover rover) {
		// first the rover enters the diagnostic mode.
		RoverMode mode = new DiagnosticMode();
		mode.start();
		
		// initialize and check every peripheral. if an error occurs whilst trying to talk to a given peripheral, put it in
		// the 'error' variable.
		int error = 0;
		if (rover.init_ultrasonic_sensor()) { Beeper.beep(); } else { Beeper.twoBeeps(); error +=  1; }
		if (rover.init_color_sensor())      { Beeper.beep(); } else { Beeper.twoBeeps(); error +=  2; }
		if (rover.init_pliers_motor())      { Beeper.beep(); } else { Beeper.twoBeeps(); error +=  4; }
		if (rover.init_right_motor())       { Beeper.beep(); } else { Beeper.twoBeeps(); error +=  8; }
		if (rover.init_left_motor())        { Beeper.beep(); } else { Beeper.twoBeeps(); error += 16; }
		
		// diagnostic is now done.
		mode.stop();
		
		// if an error occurred, 'error' is non zero.
		if (error != 0) {
			// the rover enters the error mode...
			mode = new ErrorMode();
			mode.start();
			// and program halts when a button is pressed.
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