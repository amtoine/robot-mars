package rover;

import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;

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
		System.out.println("red: " + this.device.getRedMode().getName());
		System.out.println("rgb: " + this.device.getRGBMode().getName());
		return new Measure(this.device.getColorID());
	}

	/**
	 * For now, there is nothing to write to a pair of UltraEyes.
	 */
	void write(Order order) {
	}
}