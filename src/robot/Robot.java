package robot;

import lejos.hardware.Battery;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.utility.Delay;

public class Robot {

	public static void main(String[] args) throws InterruptedException{
		System.out.println("Niveau batterie : ");
		System.out.print(Battery.getVoltage());
		
		LCD.refresh();
		Delay.msDelay(1000);
		Button.LEDPattern(5);
		Delay.msDelay(500);
		
		Motor.B.setSpeed(720);
		Motor.B.forward();
		
		Motor.C.setSpeed(720);
		Motor.C.forward();
		
		Button.LEDPattern(1);
		Motor.B.stop();
		Motor.C.stop();
		
		Motor.B.rotateTo(360);
		Motor.C.rotateTo(360);
		Motor.C.rotate(-720, true);
		Motor.B.rotate(-720, true);
		
		while (Motor.B.isMoving() && Motor.C.isMoving()) {
			Button.LEDPattern(2);
		}
		
		Button.LEDPattern(1);
		int angle = Motor.B.getTachoCount();
		LCD.drawInt(angle, 5, 5);
		
		Button.waitForAnyPress();
	}

}
