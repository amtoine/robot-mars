package rover;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.Delay;

public class Colors {

    MovePilot rover;
    
    void CheckColor(MovePilot rover) {
        
        EV3ColorSensor color = new EV3ColorSensor(SensorPort.S1);
        float[] colors = new float[1];
        SampleProvider colorProvider = color.getColorIDMode();
        
        while(Button.ESCAPE.isUp()) {
            colorProvider.fetchSample(colors, 0);
            LCD.clear(4); // delete line 4 in LCD
            float c = color.getColorID();
            LCD.drawString("Color: " + c, 0, 4);
            Delay.msDelay(100);
            
        if (c < 0.5) { // if the rover rolls on a black line, it backs off and rotates (180°)
            rover.setAngularSpeed(40);
            rover.travel(-50);
            rover.rotate(180);
        }
        
        else {
            LCD.drawString("No black line detected ", 0, 4);
        }
        
        color.close();
        
        }
    }
}