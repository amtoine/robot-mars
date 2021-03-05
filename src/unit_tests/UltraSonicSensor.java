package unit_tests;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class UltraSonicSensor {
	
	public static int BLANK  = 0;
	public static int GREEN  = 1;
	public static int RED    = 2;
	public static int ORANGE = 3;
	
	public static int STILL = 0;
	public static int SLOW = 1;
	public static int FAST = 2;
	
//	0 blank
//	1 green
//	2 red
//	3 orange
//	4 green
//	5 red
//	6 orange
//	7 green
//	8 red
//	9 orange
	
	public static void blink(int color, int mode, int ms_duration) {
		int pattern = 0;
		if (color == BLANK) {
			pattern = 0;
		} else if (color == GREEN) {
			pattern = 1+3*mode;
		} else if (color == RED) {
			pattern = 2+3*mode;
		} else if (color == ORANGE) {
			pattern = 3+3*mode;
		}
		
		Button.LEDPattern(pattern);
		
		if (ms_duration > 0) {
			Delay.msDelay(ms_duration);
		}
	}

	public static void main(String[] args) {

		EV3UltrasonicSensor US;
		try {
			US = new EV3UltrasonicSensor(SensorPort.S4);
			US.enable();
			SampleProvider sp_us = US.getDistanceMode();
			float[] dist = new float[sp_us.sampleSize()];
			
			while (Button.readButtons() != Button.ID_ENTER) {
				sp_us.fetchSample(dist, 0);
				System.out.println(dist[0]);
			}
			
//			Sound.beep();
//			Sound.playTone(440, 1000);
//			Delay.msDelay(1000);
//			Sound.beepSequence();
//			Delay.msDelay(1000);
//			Sound.beepSequenceUp();
			
			UltraSonicSensor.blink(GREEN, SLOW, 5000);
			UltraSonicSensor.blink(ORANGE, FAST, 5000);
			UltraSonicSensor.blink(RED, STILL, 5000);
		} catch (IllegalArgumentException iae) {
			System.out.println("woopsi...");
			System.out.println("press any button");
			Button.waitForAnyPress();
			System.exit(1);
		}
		
	}

}
