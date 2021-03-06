package rover;

import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;

class UltraEyes extends Peripheral {

	EV3UltrasonicSensor device;
	UltraEyes(Port port){
		this.device = null;
		this.port   = port;
	}
	
	boolean connect() {
		try {
			this.device = new EV3UltrasonicSensor(this.port);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}