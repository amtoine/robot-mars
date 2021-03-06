package modes;

import rover.Beeper;
import rover.Blinker;

public class HarvestMode implements RoverMode {

	public HarvestMode() {
		System.out.println("sample localised");
		System.out.println("harvest it");
	}

	public void start() {
		Beeper.play(Beeper.HARVEST);
		Blinker.blink(Blinker.GREEN, Blinker.FAST);
	}

	public void stop() {
		Blinker.reset();
	}

}
