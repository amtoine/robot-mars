package modes;

import lejos.hardware.Button;
import rover.Beeper;
import rover.Blinker;

/**
 * Implements the landing mode, when the rover lands on the surface of Mars.
 * 
 * @author Antoine Stevan
 *
 */
public class LandingMode implements RoverMode {
	/**
	 * Starts the landing mode. (blocking method)
	 * When the rover lands on the surface of Mars, it enters the landing mode and waits for any button press. This is the
	 * proper method to use to trigger 'landing' sound and light effects.
	 */
	public void start() {
		System.out.println("rover has just landed");
		System.out.println("stand by");
		
		Beeper.play(LandingMode.LANDING);
		Blinker.blink(Blinker.GREEN, Blinker.SLOW);
		Button.waitForAnyPress();
	}

	/**
	 * Stops the landing mode.
	 * When the rover begins its tasks, it exits the landing mode. This is the proper method to use to discard 'landing'
	 * sound and light effects.
	 */
	public void stop() {
		Blinker.reset();
	}

}
