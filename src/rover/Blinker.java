package rover;

import lejos.hardware.Button;
import lejos.utility.Delay;

public class Blinker {
	public static int BLANK  = 0;
	public static int GREEN  = 1;
	public static int RED    = 2;
	public static int ORANGE = 3;
	
	public static int STILL = 0;
	public static int SLOW = 1;
	public static int FAST = 2;
	
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
}