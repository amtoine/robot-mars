package rover;

import lejos.hardware.Device;
import lejos.hardware.port.Port;

abstract class Peripheral {
	static final int ULTRA = 0;
	static final int COLOR = 1;
	static final int MOTOR = 2;
	
	Port port;
	Device device;
	
	/**
	 * Initialization of the right motor.
	 * Tries to connect the brick and the right motor through the NXTRegulatedMotor class. If the sensor is not connected,
	 * an error is thrown, handled by the method, converted to boolean and returned.
	 * 
	 * @return true if the right motor is properly connected to the right port, false if any error occurs.
	 */
	abstract boolean connect();
}