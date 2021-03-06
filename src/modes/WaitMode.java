package modes;

import lejos.hardware.Button;
import rover.Beeper;
import rover.Blinker;

public class WaitMode implements RoverMode {

	public WaitMode() {
		System.out.println("waiting for operator");
		System.out.println("standing by");
	}

	public void start() {
		Beeper.play(Beeper.WAIT);
		Blinker.blink(Blinker.ORANGE, Blinker.SLOW);
		Button.waitForAnyPress();
	}

	public void stop() {
		Blinker.reset();
	}

}
