package modes;

import lejos.hardware.Button;
import rover.Beeper;
import rover.Blinker;

/**
 * Implements the wait mode, when the rover needs to wait for a button press.
 * 
 * @author Antoine Stevan
 *
 */
public class WaitMode implements RoverMode {

	/**
	 * Starts the wait mode. (blocking method)
	 * When the rover needs to wait for a button to be pressed, it enters the wait mode. This is the proper method to use
	 * to trigger 'wait' sound and light effects.
	 */
	public void start() {
		System.out.println("waiting for operator");
		System.out.println("standing by");
		
		Beeper.play(WaitMode.WAIT);
		Blinker.blink(Blinker.ORANGE, Blinker.SLOW);
		Button.waitForAnyPress();
	}

	/**
	 * Stops the wait mode.
	 * When a button is finally pressed, the rover exits the wait mode. This is the proper method to use to discard 'wait'
	 * sound and light effects.
	 */
	public void stop() {
		Blinker.reset();
	}

}
