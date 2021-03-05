package modes;

import lejos.hardware.Button;
import rover.Beeper;
import rover.Blinker;

public class ErrorMode implements RoverMode {
	
	public ErrorMode() {
		System.out.println("cannot continue");
		System.out.println("abort mission");
	}

	public void start() {
		Beeper.play(Beeper.ERROR);
		Blinker.blink(Blinker.RED, Blinker.FAST);
		Button.waitForAnyPress();
	}

	public void stop() {
		Blinker.reset();
	}

}
