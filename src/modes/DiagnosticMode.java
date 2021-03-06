package modes;

import rover.Beeper;
import rover.Blinker;

/**
 * Implements the diagnostic mode, when the rover is under internal inspection.
 * 
 * @author Antoine Stevan
 *
 */
public class DiagnosticMode implements RoverMode {
	/**
	 * Starts the diagnostic mode.
	 * When the rover is under internal inspection, it enters the diagnostic mode and checks every sub system. This is the
	 * proper method to use to trigger 'diagnostic' sound and light effects.
	 */
	public void start() {
		System.out.println("under internal diagnostic");
		System.out.println("complete checkup:");
		
		Beeper.play(DiagnosticMode.DIAGNOSTIC);
		Blinker.blink(Blinker.ORANGE, Blinker.FAST);
	}

	/**
	 * Stops the diagnostic mode.
	 * When the internal check-up is done, the rover exits the error mode. This is the proper method to use to discard
	 * 'diagnostic' sound and light effects.
	 */
	public void stop() {
		Blinker.reset();
	}

}
