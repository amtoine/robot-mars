package rover;

import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.RegulatedMotor;

class Engine extends Peripheral {

	RegulatedMotor device;
	Engine(Port port){
		this.device = null;
		this.port   = port;
	}
	
	boolean connect() {
		try {
			this.device = new NXTRegulatedMotor(this.port);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	Measure read() {
		return null;
	}

	void write(Order order) {
		this.device.setSpeed(order.speed);
		this.device.rotate(order.angle, true);
	}

}
