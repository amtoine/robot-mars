package modes;

import rover.Beeper;
import rover.Blinker;

/**
 * Implements the exploration mode, when the rover has to explore its environment to locate samples.
 * 
 * @author Antoine Stevan
 *
 */
public class ExplorationMode implements RoverMode {
	/**
	 * Starts the exploration mode.
	 * When the rover has to explore its environment ro locate samples, it enters the exploration mode and explores its 
	 * environment. This is the proper method to use to trigger 'exploration' sound and light effects.
	 */
	public void start() {
		System.out.println("explore the world");
		
		Beeper.play(ExplorationMode.EXPLORATION);
		Blinker.blink(Blinker.GREEN, Blinker.SLOW);
	}

	/**
	 * Stops the exploration mode.
	 * When the rover has explored enough of its surroundings, it exits the exploration mode. This is the proper method to
	 * use to discard 'exploration' sound and light effects.
	 */
	public void stop() {
		Blinker.reset();
	}

}
