package rover;

import lejos.hardware.Device;
import lejos.hardware.port.Port;

/**
 * An abstraction of any peripheral that could be connected to the EV3 lego brick.
 * 
 * @author antoine
 *
 * @see Engine
 * @see UltraEyes
 * @see ColorEye
 */
abstract class Peripheral {
	/** Any peripheral is connected to the EV3 through a port. */
	Port port;
	/** Any wire is in fact connecting a port with a device. */
	Device device;
	
	/**
	 * Initialization of a peripheral.
	 * Tries to connect the brick and the peripheral through the appropriate leJOS.hardware class. If the sensor is not
	 * connected, an error is thrown, handled by the method, converted to boolean and returned.
	 * 
	 * @return true if the peripheral is properly connected to the right port, false if any error occurs.
	 */
	abstract boolean connect();
	
	/**
	 * Reads data from the device connected through a port.
	 * 
	 * @return a measure given by the connected device, inside a Measure which is a data container.
	 */
	abstract Measure read();
	/**
	 * Sends data to a device connected through a port.
	 * 
	 * @param order data is sent to a device inside packets of data called Order.
	 */
	abstract void write(Order order);
}