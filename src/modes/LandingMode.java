package modes;

import lejos.hardware.Button;
import rover.Beeper;
import rover.Blinker;

public class LandingMode implements RoverMode {

	public LandingMode() {
		System.out.println("rover has just landed");
		System.out.println("stand by");
	}

	public void start() {
		Beeper.play(Beeper.LANDING);
		Blinker.blink(Blinker.GREEN, Blinker.SLOW);
		Button.waitForAnyPress();
	}

	public void stop() {
		Blinker.reset();
	}

}
