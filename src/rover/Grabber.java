package rover;

import lejos.hardware.port.Port;
import lejos.robotics.RegulatedMotor;
import tools.Order;

/**
 * A wrapper class to make the use of the pliers of the rover easier.
 * It essentially takes care of the movements of the motor needed to open and close the pliers.
 * 
 * @author Antoine Stevan
 *
 */
class Grabber {
	/** The motor controlled by the rover to actuate the pliers. */
	Engine motor;
	/** The given speed of the motor controlling the pliers, in degrees per second. */
	final int motor_speed          = 10;
	/** The total opening of the motor controlling the pliers, in degrees. */
	final int pliers_opening_angle = 40;
	
	/**
	 * Constructor for any Grabber instance.
	 * A Grabber only needs a port to connect to the brick.
	 * 
	 * @param port the port needed to connect the brick and the motor allocated to the pliers.
	 */
	Grabber(Port port){
		this.motor = new Engine(port);
	}
	
	/**
	 * A wrapper method to connect the motor allocated to the pliers and the brick.
	 * Simply calls the {@link Engine#connect()} method.
	 * 
	 * @return a boolean telling whether the connection has been successful or not.
	 */
	boolean connect() {
		return this.motor.connect();
	}

	/**
	 * A tool wrapper of the {@link RegulatedMotor#getTachoCount()} method to get the tacho count of the motor allocated to the pliers.
	 * 
	 * @return the integer tacho count of the motor, in degrees.
	 */
	int getTachoCount() {
		return this.motor.device.getTachoCount();
	}

	/**
	 * A tool wrapper of the {@link RegulatedMotor#isMoving()} method to know if the pliers are currently moving.
	 * 
	 * @return a boolean telling whether the pliers are currently moving or not.
	 */
	boolean isMoving() {
		return this.motor.device.isMoving();
	}

	/**
	 * Makes the pliers close to grab anything in front of the rover.
	 */
	public void grab() {
		this.motor.device.forward();
		this.motor.write(new Order(this.motor_speed, this.pliers_opening_angle));
		this.motor.device.stop();
	}

	/**
	 * Makes the pliers open to release anything that was inside the pliers.
	 */
	public void release() {
		this.motor.device.backward();
		this.motor.write(new Order(this.motor_speed, -this.pliers_opening_angle));
		this.motor.device.stop();
	}
}
