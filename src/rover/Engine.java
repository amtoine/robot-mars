package rover;

import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.RegulatedMotor;
import tools.Measure;
import tools.Order;

/**
 * The EV3 lego brick can be connected to motors, here called Engine.
 * 
 * @author Antoine Stevan
 *
 */
class Engine extends Peripheral {
	private static final int speed = 10;
	private static final int acceleration = 10;
	
	/** Precision about the Device of an Engine. Here, it is a RegulatedMotor. */
	RegulatedMotor device;
	
	/**
	 * One can initialize an Engine by giving it a port.
	 * 
	 * @param port the port of the Engine, it is basically a MotorPort, from A to D.
	 */
	Engine(Port port){
		this.device = null;
		this.port   = port;
	}
	
	/**
	 * Connects a Motor to the EV3 lego brick.
	 * If any error occurs during the connection, the error is converted into a boolean and returned.
	 */
	boolean connect() {
		try {
			this.device = new NXTRegulatedMotor(this.port);
			this.device.resetTachoCount();
			this.device.setSpeed(Engine.speed);
			this.device.setAcceleration(Engine.acceleration);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * For now, there is nothing to read from an Engine.
	 */
	Measure read() {
		return null;
	}

	/**
	 * It is possible to talk to an Engine by giving it an order, containing both the speed and the relative angle of
	 * rotation.
	 */
	void write(Order order) {
		this.device.setSpeed(order.getSpeed());
		this.device.rotate(order.getAngle());
	}
}
