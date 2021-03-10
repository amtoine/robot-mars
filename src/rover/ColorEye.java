package rover;

import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import tools.Measure;
import tools.Order;

/**
 * The EV3 lego brick can be connected to a color sensor, here called ColorEye (because it kind of looks like a single eye).
 * 
 * @author Antoine Stevan
 *
 */
class ColorEye extends Peripheral {
	/** Precision about the Device of a ColorEye. Here, it is an EV3ColorSensor. */
	EV3ColorSensor device;
	
	/**
	 * One can initialize a ColorEye by giving it a port.
	 * 
	 * @param port the port of the ColorEye, it is basically a SensorPort, from 1 to 4.
	 */
	ColorEye(Port port){
		this.device = null;
		this.port   = port;
	}
	
	/**
	 * Connects a Color Sensor to the EV3 lego brick.
	 * If any error occurs during the connection, the error is converted into a boolean and returned.
	 */
	boolean connect() {
		try {
			this.device = new EV3ColorSensor(this.port);
//			System.out.println(this.device.setFloodlight(Color.RED));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Reading from a ColorEye object means reading the value of the seen color, in front of the sensor.
	 * The value is stored inside a Measure container, as the 'value' field.
	 */
	Measure read() {
		float [] red = new float[this.device.getRedMode().sampleSize()];
		float [] rgb = new float[this.device.getRGBMode().sampleSize()];
		
		this.device.getRedMode().fetchSample(red, 0);
		this.device.getRGBMode().fetchSample(rgb, 0);
		
		System.out.print("red: " + red[0]);
		System.out.print("rgb: [" + rgb[0] + "," + rgb[1] + "," + rgb[0] + "]");
		return new Measure(this.device.getColorID());
	}

	/**
	 * For now, there is nothing to write to a pair of UltraEyes.
	 */
	void write(Order order) {
	}
}