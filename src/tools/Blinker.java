package tools;

import lejos.hardware.Button;
import lejos.utility.Delay;

/**
 * The Blinker static class is designed to help developers to use the leJOS' Button class more easily. 
 * 
 * @author Antoine Stevan
 *
 */
public class Blinker {
	/**
	 * Blank color code.
	 * When used as a parameter in {@link #blink(int, int)} or {@link #blink(int, int, int)}, controls the color of the 
	 * LEDs in the center of the lego brick and set it to no color.
	 * 
	 * @see #blink(int, int)
	 * @see #blink(int, int, int)
	 */
	public static int BLANK  = 0;
	/**
	 * Green color code.
	 * When used as a parameter in {@link #blink(int, int)} or {@link #blink(int, int, int)}, controls the color of the 
	 * LEDs in the center of the lego brick and set it to green.
	 * 
	 * @see #blink(int, int)
	 * @see #blink(int, int, int)
	 */
	public static int GREEN  = 1;
	/**
	 * Red color code.
	 * When used as a parameter in {@link #blink(int, int)} or {@link #blink(int, int, int)}, controls the color of the 
	 * LEDs in the center of the lego brick and set it to red.
	 * 
	 * @see #blink(int, int)
	 * @see #blink(int, int, int)
	 */
	public static int RED    = 2;
	/**
	 * Orange color code.
	 * When used as a parameter in {@link #blink(int, int)} or {@link #blink(int, int, int)}, controls the color of the 
	 * LEDs in the center of the lego brick and set it to orange.
	 * 
	 * @see #blink(int, int)
	 * @see #blink(int, int, int)
	 */
	public static int ORANGE = 3;
	
	/**
	 * Still LEDs speed mode.
	 *  When used as a parameter in {@link #blink(int, int)} or {@link #blink(int, int, int)}, controls the speed of the 
	 * LEDs in the center of the lego brick and set it 0, i.e. the LEDs do not blink and the light stays still.
	 * 
	 * @see #blink(int, int)
	 * @see #blink(int, int, int) 
	 */
	public static int STILL = 0;
	/**
	 * Slow LEDs speed mode.
	 *  When used as a parameter in {@link #blink(int, int)} or {@link #blink(int, int, int)}, controls the speed of the 
	 * LEDs in the center of the lego brick and set it slow, i.e. the LEDs blink at a pretty slow pace like 'blink...
	 * blink... blink...'.
	 * 
	 * @see #blink(int, int)
	 * @see #blink(int, int, int) 
	 */
	public static int SLOW = 1;
	/**
	 * Fast LEDs speed mode.
	 *  When used as a parameter in {@link #blink(int, int)} or {@link #blink(int, int, int)}, controls the speed of the 
	 * LEDs in the center of the lego brick and set it fast, i.e. the LEDs blink at a pretty fast pace like
	 * 'blink blink... blink blink...'.
	 * 
	 * @see #blink(int, int)
	 * @see #blink(int, int, int) 
	 */
	public static int FAST = 2;
	
	/**
	 * Starts a blink sequence (can be a blocking method).
	 * To indicate modes or make the rover more friendly, one could want to have the LEDs blinking. Simply give blink()
	 * a color, a blink mode and a total duration.
	 * To use a simplified version of blink, see {@link #blink(int, int)}.
	 * 
	 * @param color the color of the LEDs.
	 * @param mode the blink/speed mode for the LEDs.
	 * @param ms_duration the total time length of the blink sequence -> if positive, blocks following instructions
	 * until completion.
	 * 
	 * @see #blink(int, int)
	 */
	public static void blink(int color, int mode, int ms_duration) {
		int pattern = 0;
		if (color == BLANK) {
			pattern = 0;
		} else if (color == GREEN) {
			pattern = 1+3*mode;
		} else if (color == RED) {
			pattern = 2+3*mode;
		} else if (color == ORANGE) {
			pattern = 3+3*mode;
		}
		
		Button.LEDPattern(pattern);
		
		if (ms_duration > 0) {
			Delay.msDelay(ms_duration);
			Button.LEDPattern(0);
		}
	}
	
	/**
	 * Starts a blink sequence.
	 * To indicate modes or make the rover more friendly, one could want to have the LEDs blinking. Simply give blink()
	 * a color and a blink mode. Here, no total duration is recquired, hence {@link #blink(int, int)} sets a blink mode
	 * which will run in the background until either {@link #reset()}, a new blink sequence or Button.LEDPattern(0) is
	 * called.
	 * 
	 * @param color the color of the LEDs.
	 * @param mode the blink/speed mode for the LEDs.
	 */
	public static void blink(int color, int mode) {
		int pattern = 0;
		if (color == BLANK) {
			pattern = 0;
		} else if (color == GREEN) {
			pattern = 1+3*mode;
		} else if (color == RED) {
			pattern = 2+3*mode;
		} else if (color == ORANGE) {
			pattern = 3+3*mode;
		}
		
		Button.LEDPattern(pattern);
	}
	
	/**
	 * Resets the LEDs in the middle of the brick.
	 */
	public static void reset() {
		Button.LEDPattern(0);
	}
}