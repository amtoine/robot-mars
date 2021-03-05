package unit_tests;

import modes.DiagnosticMode;
import modes.ErrorMode;
import modes.RoverMode;
import rover.Beeper;
import rover.Rover;

public class InternalDoctor {
	public static void init_peripherals(Rover rover) {
		RoverMode mode = new DiagnosticMode();
		mode.start();
		int error = 0;
		if (rover.init_ultrasonic_sensor()) { Beeper.beep(); } else { Beeper.twoBeeps(); error +=  1; }
		if (rover.init_color_sensor())      { Beeper.beep(); } else { Beeper.twoBeeps(); error +=  2; }
		if (rover.init_pliers_motor())      { Beeper.beep(); } else { Beeper.twoBeeps(); error +=  4; }
		if (rover.init_right_motor())       { Beeper.beep(); } else { Beeper.twoBeeps(); error +=  8; }
		if (rover.init_left_motor())        { Beeper.beep(); } else { Beeper.twoBeeps(); error += 16; }
		mode.stop();
		
		if (error != 0) {
			mode = new ErrorMode();
			mode.start();
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