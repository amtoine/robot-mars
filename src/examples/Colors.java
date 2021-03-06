package examples;

import java.io.IOException;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class Colors {
	
	public static void main(String[] args) throws IOException{
		
		EV3ColorSensor color = new EV3ColorSensor(SensorPort.S1);
		float[] colors = new float[1];
		SampleProvider colorProvider = color.getColorIDMode(); // measures the color of the surface
		
		while(Button.ESCAPE.isUp()) {
			
			colorProvider.fetchSample(colors, 0);
			LCD.clear(4); // delete line 4 in LCD
			LCD.drawString("Color: " + color.getColorID(), 0, 4);
			
			Delay.msDelay(100);
			
		}
		
		color.close();
		
	}
	
}
