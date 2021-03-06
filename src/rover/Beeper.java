package rover;

import lejos.hardware.Sound;
import lejos.utility.Delay;
import modes.RoverMode;

/**
 * The Beeper static class is designed to help developers to use the leJOS' Sound class more easily.  
 * 
 * @author Antoine Stevan
 *
 */
public class Beeper {
	/**
	 * A low pitch used to play simple music.
	 * For example, when one calls the {@link #play(int)} method with the wanted sequence,
	 * a low note is played whenever a bit of the sequence is 0.
	 * 
	 * @see Beeper#play(int)
	 */
	public static int LOW_PICTH  = 440;
	/**
	 * A high pitch used to play simple music.
	 * For example, when one calls the {@link #play(int)} method with the wanted sequence,
	 * a high note is played whenever a bit of the sequence is 1.
	 * 
	 * @see Beeper#play(int)
	 */
	public static int HIGH_PITCH = 880;
	
	/**
	 * Plays an alarm for some amount of time (blocking method).
	 * When given a frequency and a total duration, {@link #alarm(int, int)} computes the period between two beeps and play
	 * the right amount of beeps so that the alarm plays during duration.
	 *  
	 * @param frequency the frequency of the alarm (in Hz)
	 * @param duration the length of the alarm (in ms)
	 */
	public static void alarm(int frequency, int duration) {
		int period = 1000/frequency;
		
		float t = 0;
		while (t < duration) {
			Sound.beep();
			Delay.msDelay(period);
			t += period;
		}
	}
	
	/**
	 * A wrapper method of the {@link Sound#beep()} method.
	 * Plays a single beep and let the program continue.
	 * 
	 * @see Sound#beep()
	 */
	public static void beep() {
		Sound.beep();
	}
	
	/**
	 * Overload of the {@link Sound#beep()} wrapper.
	 * One gives a number of beeps to be heard and the period between two consecutive beeps.
	 * 
	 * @param beeps the number of beeps to be heard.
	 * @param period the period (in ms) between two beeps.
	 */
	public static void beep(int beeps, int period) {
		for (int i = 0; i < beeps; i++) {
			Sound.beep();
			Delay.msDelay(period);
		}
	}
	
	/**
	 * A wrapper method of the {@link Sound#twoBeeps()} method.
	 * Plays two beeps and let the program continue.
	 * 
	 * @see Sound#twoBeeps()
	 */
	public static void twoBeeps() {
		Sound.twoBeeps();
	}
	
	/**
	 * Plays a little music sequence to introduce a mode for the rover (see interface {@link RoverMode}) (blocking method).
	 * Throughout its life, the rover will be in different states, called modes. To help the developers and the audience
	 * to debug and understand what is going on without looking at the console -one can find more informations on the
	 * console, but the sounds are meant to be a summary- when the rover enters a new mode, a unique sequence of notes will
	 * be played. As there are less than 8 modes, the integer code of a mode is coded with three bits. When decoded, a 0
	 * correspond to a low note (see {@link #LOW_PICTH}) and a 1 is a high note (see {@link #HIGH_PITCH}). With this
	 * method, the instrument and the delays can be precised, see {@link #play(int)} for a default use.
	 * 
	 * @param mode the integer code of the mode
	 * @param inst the instrument to be used during the sequence of notes
	 * (see {@link lejos.hardware.Sound#playNote(int[], int, int)} for more details about instruments).
	 * @param delay the length of each note and the time between two notes.
	 * 
	 * @see RoverMode
	 * @see Beeper#LOW_PICTH
	 * @see Beeper#HIGH_PITCH
	 * @see #play(int)
	 */
	public static void play(int mode, int[] inst, int delay) {
		Sound.playNote(inst, ((mode&4) == 1)? HIGH_PITCH : LOW_PICTH, delay);
		Delay.msDelay(delay);
		Sound.playNote(inst, ((mode&2) == 1)? HIGH_PITCH : LOW_PICTH, delay);
		Delay.msDelay(delay);
		Sound.playNote(inst, ((mode&1) == 1)? HIGH_PITCH : LOW_PICTH, delay);
	}
	
	/**
	 * Plays a little music sequence to introduce a mode for the rover (see interface {@link RoverMode}) (blocking method).
	 * 
	 * Throughout its life, the rover will be in different states, called modes. To help the developers and the audience
	 * to debug and understand what is going on without looking at the console -one can find more informations on the
	 * console, but the sounds are meant to be a summary- when the rover enters a new mode, a unique sequence of notes will 
	 * be played. As there are less than 8 modes, the integer code of a mode is coded with three bits. When decoded, a 0
	 * correspond to a low note (see {@link #LOW_PICTH}) and a 1 is a high note (see {@link #HIGH_PITCH}).
	 * 
	 * default values:
	 *    - instrument = Sound.PIANO
	 *    - delay = 50 ms.
	 * 
	 * @param mode the integer code of the mode
	 * 
	 * @see RoverMode
	 * @see Beeper#LOW_PICTH
	 * @see Beeper#HIGH_PITCH
	 */
	public static void play(int mode) {
		int[] inst = Sound.PIANO;
		int delay = 50;
		
		Sound.playNote(inst, ((mode&4) == 1)? HIGH_PITCH : LOW_PICTH, delay);
		Delay.msDelay(delay);
		Sound.playNote(inst, ((mode&2) == 1)? HIGH_PITCH : LOW_PICTH, delay);
		Delay.msDelay(delay);
		Sound.playNote(inst, ((mode&1) == 1)? HIGH_PITCH : LOW_PICTH, delay);
	}
}