package rover;

import lejos.hardware.Sound;
import lejos.utility.Delay;

public class Beeper {
	
	public static void alarm(int frequency, int duration) {
		int period = 1000/frequency;
		
		float t = 0;
		while (t < duration) {
			Sound.beep();
			Delay.msDelay(period);
			t += period;
		}
	}
	
	public static void beep() {
		Sound.beep();
	}
	
	public static void twoBeeps() {
		Sound.twoBeeps();
	}
}