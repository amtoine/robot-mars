package examples;

import lejos.hardware.Battery;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class sensors_motors {

	public static void main(String[] args) {
		int dist_min = 30;
		int angle_middle = 0;
		
		EV3UltrasonicSensor US = new EV3UltrasonicSensor(SensorPort.S3);
		EV3TouchSensor TC = new EV3TouchSensor(SensorPort.S1);
		
		//Battery pow_bat = new Battery();
		
		RegulatedMotor Motor1 = Motor.B;
		RegulatedMotor Motor2 = Motor.C;
		
		float power = Battery.getVoltage();
		float p_useful;
		
		US.enable();
		SampleProvider sp_us = US.getDistanceMode();
		float[] dist = new float[sp_us.sampleSize()];
		
		SampleProvider sp_tc = TC.getTouchMode();
		float[] touch = new float[sp_tc.sampleSize()];
		int value = 255;
		
		p_useful = (power/9)*100;
		System.out.println("Val1: " + power);
		System.out.println("P_useful: " + p_useful);
		Button.waitForAnyPress();
		
		Motor1.resetTachoCount();
		Motor2.resetTachoCount();
		Motor1.rotateTo(0);
		Motor2.rotateTo(0);
		Motor1.setSpeed(400);
		Motor2.setSpeed(400);
		Motor1.setAcceleration(800);
		Motor2.setAcceleration(800);
		
		while(value>60) {
			LCD.clear();
			Button.waitForAnyPress();
			sp_us.fetchSample(dist, 0);
			Delay.msDelay(10);
			value = (int)(dist[0]*100);
			System.out.println("Distance: " + value);
		}
		
		if(value>30 && value<60){
			LCD.clear();
			System.out.println("Scan");
			Button.waitForAnyPress();
			int scan_dist[] = {0,0,0,0,0,0};
			int angles[] = {0,0,0,0,0,0};
			int angle_rotation = 30;
			
			for(int i=0; i<6; i++) {
				sp_us.fetchSample(dist, 0);
				Delay.msDelay(10);
				int value2 = (int)(dist[0]*100);
				scan_dist[i] = value2;
				angles[i] = angle_rotation*i;
				if(value2<dist_min) {
					dist_min=value2;
					angle_middle=angles[i];
				}
				Button.waitForAnyPress();
			}
		}
		
		US.close();
		LCD.clear();
		System.out.println("Angle: " + angle_middle);
		Button.waitForAnyPress();
		int value2 = 0;
		System.out.println("Wheels: ");
		Button.waitForAnyPress();
		LCD.clear();
		while(value2==0) {
			Button.LEDPattern(6);
			Motor1.rotateTo(-360);
			System.out.println("1: " + Motor.B.getTachoCount());
			Motor1.stop();
			Motor2.rotateTo(-360);
			System.out.println("2: " + Motor.C.getTachoCount());
			Motor2.stop();
			
			sp_tc.fetchSample(touch, 0);
			value2 = (int)touch[0];
			LCD.clear();
			Button.waitForAnyPress();
		}
		TC.close();
	}

}
