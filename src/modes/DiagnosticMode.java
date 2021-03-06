package modes;

import rover.Beeper;
import rover.Blinker;

public class DiagnosticMode implements RoverMode {

	public DiagnosticMode() {
		System.out.println("under internal diagnostic");
		System.out.println("complete checkup:");
	}

	public void start() {
		Beeper.play(Beeper.DIAGNOSTIC);
		Blinker.blink(Blinker.ORANGE, Blinker.FAST);
	}

	public void stop() {
		Blinker.reset();
	}

}
