package modes;

import lejos.hardware.Button;
import rover.Beeper;
import rover.Blinker;

/**
 * Implements the sleep mode, when the rover needs to sleep after completing its tasks.
 * 
 * @author Antoine Stevan
 *
 */
public class SleepMode implements RoverMode {
	/**
	 * Starts the sleep mode. (blocking method)
	 * When the rover needs to sleep after completing its tasks, it enters the sleep mode. This is the proper method to
	 * use to trigger 'sleep' sound and light effects.
	 */
	public void start() {
		System.out.println("job done");
		System.out.println("go to sleep");
		
		Beeper.play(SleepMode.SLEEP);
		Blinker.blink(Blinker.ORANGE, Blinker.STILL);
		Button.waitForAnyPress();
	}

	/**
	 * Stops the sleep mode.
	 * When the rover sleeps no more, it exits the sleep mode. This is the proper method to use to discard 'sleep' sound
	 * and light effects.
	 */
	public void stop() {
		Blinker.reset();
	}

}
