package modes;

import lejos.hardware.Button;
import rover.Beeper;
import rover.Blinker;

public class SleepMode implements RoverMode {

	public SleepMode() {
		System.out.println("job done");
		System.out.println("go to sleep");
	}

	public void start() {
		Beeper.play(Beeper.SLEEP);
		Blinker.blink(Blinker.ORANGE, Blinker.STILL);
		Button.waitForAnyPress();
	}

	public void stop() {
		Blinker.reset();
	}

}
