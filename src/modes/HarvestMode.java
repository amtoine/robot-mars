package modes;

import rover.Beeper;
import rover.Blinker;

/**
 * Implements the harvest mode, when the rover has located a sample and needs to harvest it.
 * 
 * @author Antoine Stevan
 *
 */
public class HarvestMode implements RoverMode {
	/**
	 * Starts the harvest mode.
	 * When the rover has located a sample and needs to harvest it, it enters the harvest mode and do what it has to do to
	 * retrieve the sample. This is the proper method to use to trigger 'harvest' sound and light effects.
	 */
	public void start() {
		System.out.println("sample localised");
		System.out.println("harvest it");
		
		Beeper.play(HarvestMode.HARVEST);
		Blinker.blink(Blinker.GREEN, Blinker.FAST);
	}

	/**
	 * Stops the harvest mode.
	 * When the rover has brought back the sample, it exits the harvest mode. This is the proper method to use to discard
	 * 'harvest' sound and light effects.
	 */
	public void stop() {
		Blinker.reset();
	}

}
