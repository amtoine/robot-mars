package modes;

import rover.Beeper;
import rover.Blinker;

public class ExplorationMode implements RoverMode {

	public ExplorationMode() {
		System.out.println("explore the world");
	}

	public void start() {
		Beeper.play(Beeper.EXPLORATION);
		Blinker.blink(Blinker.GREEN, Blinker.SLOW);
	}

	public void stop() {
		Blinker.reset();
	}

}
