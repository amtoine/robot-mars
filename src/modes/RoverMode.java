package modes;

import rover.Blinker;

/**
 * Interface to use any rover mode indifferently.
 * 
 * During its life, the rover will be in a few different modes. However they all share the following properties, summed up
 * inside the RoverMode interface
 * 
 * @author Antoine Stevan
 * 
 * @see LandingMode
 * @see DiagnosticMode
 * @see ErrorMode
 * @see ExplorationMode
 * @see HarvestMode
 * @see WaitMode
 * @see SleepMode
 */
public interface RoverMode {
	/**
	 * Code for the landing mode.
	 * When the rover has just landed on Mars, this is the appropriate code to be used in {@link Blinker#blink(int, int)}
	 * or {@link Blinker#blink(int, int, int)}. This will trigger the right sounds and light effects.
	 * 
	 * @see Blinker#blink(int, int)
	 * @see Blinker#blink(int, int, int)
	 */
	public static int LANDING     = 1;
	/**
	 * Code for the diagnostic mode.
	 * When the rover is under internal inspection, this is the appropriate code to be used in
	 * {@link Blinker#blink(int, int)} or {@link Blinker#blink(int, int, int)}. This will trigger the right sounds and
	 * light effects.
	 * 
	 * @see Blinker#blink(int, int)
	 * @see Blinker#blink(int, int, int)
	 */
	public static int DIAGNOSTIC  = 2;
	/**
	 * Code for the error mode.
	 * When the internal inspection of the rover reports any error, this is the appropriate code to be used in
	 * {@link Blinker#blink(int, int)} or {@link Blinker#blink(int, int, int)}. This will trigger the right sounds and
	 * light effects.
	 * 
	 * @see Blinker#blink(int, int)
	 * @see Blinker#blink(int, int, int)
	 */
	public static int ERROR       = 3;
	/**
	 * Code for the exploration mode.
	 * When the rover has to explore its environment to locate samples, this is the appropriate code to be used in
	 * {@link Blinker#blink(int, int)} or {@link Blinker#blink(int, int, int)}. This will trigger the right sounds and
	 * light effects.
	 * 
	 * @see Blinker#blink(int, int)
	 * @see Blinker#blink(int, int, int)
	 */
	public static int EXPLORATION = 4;
	/**
	 * Code for the harvest mode.
	 * When the rover has located a sample and needs to harvest it, this is the appropriate code to be used in
	 * {@link Blinker#blink(int, int)} or {@link Blinker#blink(int, int, int)}. This will trigger the right sounds and
	 * light effects.
	 * 
	 * @see Blinker#blink(int, int)
	 * @see Blinker#blink(int, int, int)
	 */
	public static int HARVEST     = 5;
	/**
	 * Code for the wait mode.
	 * When the rover has to wait until a button is pressed, this is the appropriate code to be used in
	 * {@link Blinker#blink(int, int)} or {@link Blinker#blink(int, int, int)}. This will trigger the right sounds and
	 * light effects.
	 * 
	 * @see Blinker#blink(int, int)
	 * @see Blinker#blink(int, int, int)
	 */
	public static int WAIT        = 6;
	/**
	 * Code for the sleep mode.
	 * When the rover is sleeping after all its work, this is the appropriate code to be used in
	 * {@link Blinker#blink(int, int)} or {@link Blinker#blink(int, int, int)}. This will trigger the right sounds and
	 * light effects.
	 * 
	 * @see Blinker#blink(int, int)
	 * @see Blinker#blink(int, int, int)
	 */
	public static int SLEEP       = 7;
	
	/**
	 * Start a mode for the rover.
	 * Every RoverMode can start a sequence of sound and light effects.
	 * 
	 * @see LandingMode
	 * @see DiagnosticMode
	 * @see ErrorMode
	 * @see ExplorationMode
	 * @see HarvestMode
	 * @see WaitMode
	 * @see SleepMode
	 */
	public void start();
	/**
	 * Stop a mode for the rover.
	 * Every RoverMode can stop a sequence of sound and light effects.
	 * 
	 * @see LandingMode
	 * @see DiagnosticMode
	 * @see ErrorMode
	 * @see ExplorationMode
	 * @see HarvestMode
	 * @see WaitMode
	 * @see SleepMode
	 */
	public void stop();
}
