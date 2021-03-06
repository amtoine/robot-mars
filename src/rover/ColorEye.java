package rover;

import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;

class ColorEye extends Peripheral {

	EV3ColorSensor device;
	ColorEye(Port port){
		this.device = null;
		this.port   = port;
	}
	
	boolean connect() {
		try {
			this.device = new EV3ColorSensor(this.port);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}