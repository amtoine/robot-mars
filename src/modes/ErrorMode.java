package modes;

import lejos.hardware.Button;
import rover.Beeper;
import rover.Blinker;

/**
 * Implements the error mode, when the internal inspection of the rover reports any error.
 * 
 * @author Antoine Stevan
 *
 */
public class ErrorMode implements RoverMode {
	/**
	 * Starts the error mode. (blocking method)
	 * When the internal inspection of the rover reports any error, the rover enters the error mode and go to sleep because
	 * the mission is compromised. This is the proper method to use to trigger 'error' sound and light effects.
	 */
	public void start() {
		System.out.println("cannot continue");
		System.out.println("abort mission");
		
		Beeper.play(ErrorMode.ERROR);
		Blinker.blink(Blinker.RED, Blinker.FAST);
		Button.waitForAnyPress();
	}

	/**
	 * Stops the error mode.
	 * When the rover has resolved its internal errors (which never happens in this project), it exits the error mode. This
	 * is the proper method to use to discard 'error' sound and light effects.
	 */
	public void stop() {
		Blinker.reset();
	}

}
