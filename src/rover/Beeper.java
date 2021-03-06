package rover;

import lejos.hardware.Sound;
import lejos.utility.Delay;

public class Beeper {
	
	public static int LANDING     = 1;
	public static int DIAGNOSTIC  = 2;
	public static int ERROR       = 3;
	public static int EXPLORATION = 4;
	public static int HARVEST     = 5;
	public static int WAIT        = 6;
	public static int SLEEP       = 7;
	
	public static int LOW_PICTH  = 440;
	public static int HIGH_PITCH = 880;
	
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
	
	public static void beep(int beeps, int period) {
		for (int i = 0; i < beeps; i++) {
			Sound.beep();
			Delay.msDelay(period);
		}
	}
	
	public static void twoBeeps() {
		Sound.twoBeeps();
	}
	
	public static void play(int sequence) {
		int[] inst = Sound.PIANO;
		int delay = 50;
		
		Sound.playNote(inst, ((sequence&4) == 1)? HIGH_PITCH : LOW_PICTH, delay);
		Delay.msDelay(delay);
		Sound.playNote(inst, ((sequence&2) == 1)? HIGH_PITCH : LOW_PICTH, delay);
		Delay.msDelay(delay);
		Sound.playNote(inst, ((sequence&1) == 1)? HIGH_PITCH : LOW_PICTH, delay);
	}
}