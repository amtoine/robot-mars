package rover;

import tools.Beeper;
import tools.Blinker;

/**
 * Interface to use any rover mode indifferently.
 * 
 * During its life, the rover will be in a few different modes. However they all share the following properties, summed up
 * inside the RoverMode interface
 * 
 * @author Antoine Stevan
 */
class RoverMode {
	/**
	 * Code for the landing mode.
	 * When the rover has just landed on Mars, this is the appropriate code to be used in {@link Blinker#blink(int, int)}
	 * or {@link Blinker#blink(int, int, int)}. This will trigger the right sounds and light effects.
	 * 
	 * @see Blinker#blink(int, int)
	 * @see Blinker#blink(int, int, int)
	 */
	static int LANDING     = 1;
	/**
	 * Code for the diagnostic mode.
	 * When the rover is under internal inspection, this is the appropriate code to be used in
	 * {@link Blinker#blink(int, int)} or {@link Blinker#blink(int, int, int)}. This will trigger the right sounds and
	 * light effects.
	 * 
	 * @see Blinker#blink(int, int)
	 * @see Blinker#blink(int, int, int)
	 */
	static int DIAGNOSTIC  = 2;
	/**
	 * Code for the error mode.
	 * When the internal inspection of the rover reports any error, this is the appropriate code to be used in
	 * {@link Blinker#blink(int, int)} or {@link Blinker#blink(int, int, int)}. This will trigger the right sounds and
	 * light effects.
	 * 
	 * @see Blinker#blink(int, int)
	 * @see Blinker#blink(int, int, int)
	 */
	static int ERROR       = 3;
	/**
	 * Code for the exploration mode.
	 * When the rover has to explore its environment to locate samples, this is the appropriate code to be used in
	 * {@link Blinker#blink(int, int)} or {@link Blinker#blink(int, int, int)}. This will trigger the right sounds and
	 * light effects.
	 * 
	 * @see Blinker#blink(int, int)
	 * @see Blinker#blink(int, int, int)
	 */
	static int EXPLORATION = 4;
	/**
	 * Code for the harvest mode.
	 * When the rover has located a sample and needs to harvest it, this is the appropriate code to be used in
	 * {@link Blinker#blink(int, int)} or {@link Blinker#blink(int, int, int)}. This will trigger the right sounds and
	 * light effects.
	 * 
	 * @see Blinker#blink(int, int)
	 * @see Blinker#blink(int, int, int)
	 */
	static int HARVEST     = 5;
	/**
	 * Code for the wait mode.
	 * When the rover has to wait until a button is pressed, this is the appropriate code to be used in
	 * {@link Blinker#blink(int, int)} or {@link Blinker#blink(int, int, int)}. This will trigger the right sounds and
	 * light effects.
	 * 
	 * @see Blinker#blink(int, int)
	 * @see Blinker#blink(int, int, int)
	 */
	static int WAIT        = 6;
	/**
	 * Code for the sleep mode.
	 * When the rover is sleeping after all its work, this is the appropriate code to be used in
	 * {@link Blinker#blink(int, int)} or {@link Blinker#blink(int, int, int)}. This will trigger the right sounds and
	 * light effects.
	 * 
	 * @see Blinker#blink(int, int)
	 * @see Blinker#blink(int, int, int)
	 */
	static int SLEEP       = 7;
	
	/**
	 * Starts the diagnostic mode.
	 * When the rover is under internal inspection, it enters the diagnostic mode and checks every sub system. This is the
	 * proper method to use to trigger 'diagnostic' sound and light effects.
	 */
	void enter_diagnostic_mode() {
		Beeper.play(DIAGNOSTIC);
		Blinker.blink(Blinker.ORANGE, Blinker.FAST);
	}
	
	/**
	 * Starts the error mode.
	 * When the internal inspection of the rover reports any error, the rover enters the error mode and go to sleep because
	 * the mission is compromised. This is the proper method to use to trigger 'error' sound and light effects.
	 */
	void enter_error_mode() {
		Beeper.play(ERROR);
		Blinker.blink(Blinker.RED, Blinker.FAST);		
	}

	/**
	 * Starts the landing mode.
	 * When the rover lands on the surface of Mars, it enters the landing mode and waits for any button press. This is the
	 * proper method to use to trigger 'landing' sound and light effects.
	 */
	void enter_landind_mode() {
		Beeper.play(LANDING);
		Blinker.blink(Blinker.GREEN, Blinker.SLOW);		
	}

	/**
	 * Starts the exploration mode.
	 * When the rover has to explore its environment ro locate samples, it enters the exploration mode and explores its 
	 * environment. This is the proper method to use to trigger 'exploration' sound and light effects.
	 */
	void enter_exploration_mode() {
		Beeper.play(EXPLORATION);
		Blinker.blink(Blinker.GREEN, Blinker.SLOW);		
	}

	/**
	 * Starts the harvest mode.
	 * When the rover has located a sample and needs to harvest it, it enters the harvest mode and do what it has to do to
	 * retrieve the sample. This is the proper method to use to trigger 'harvest' sound and light effects.
	 */
	void enter_harvest_mode() {
		Beeper.play(HARVEST);
		Blinker.blink(Blinker.GREEN, Blinker.FAST);		
	}

	/**
	 * Starts the wait mode.
	 * When the rover needs to wait for a button to be pressed, it enters the wait mode. This is the proper method to use
	 * to trigger 'wait' sound and light effects.
	 */
	void enter_wait_mode() {
		Beeper.play(WAIT);
		Blinker.blink(Blinker.ORANGE, Blinker.SLOW);		
	}

	/**
	 * Starts the sleep mode.
	 * When the rover needs to sleep after completing its tasks, it enters the sleep mode. This is the proper method to
	 * use to trigger 'sleep' sound and light effects.
	 */
	void enter_sleep_mode() {
		Beeper.play(SLEEP);
		Blinker.blink(Blinker.ORANGE, Blinker.STILL);		
	}
	
	/**
	 * Stops any mode for the rover.
	 * Every RoverMode can stop a sequence of sound and light effects.
	 */
	void stop() {
		Blinker.reset();
	}
}
