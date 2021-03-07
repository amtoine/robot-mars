package rover;

import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;

/**
 * The EV3 lego brick can be connected to an ultrasonic sensor, here called UltraEyes (because it looks like a pair of eyes).
 * 
 * @author Antoine Stevan
 *
 */
class UltraEyes extends Peripheral {
	/** Precision about the Device of a pair of UltraEyes. Here, it is an EV3UltrasonicSensor. */
	EV3UltrasonicSensor device;
	/** To make measures easier a pair of UltraEyes uses a SamplerProvider. */
	private SampleProvider sampler;
	/** Ultrasonic measures are stored inside an array. */
	private float[] distance;
	
	/**
	 * One can initialize a pair of UltraEyes by giving it a port.
	 * 
	 * @param port the port of the pair of UltraEyes, it is basically a SensorPort, from 1 to 4.
	 */
	UltraEyes(Port port){
		this.device   = null;
		this.sampler  = null;
		this.distance = null;
		this.port     = port;
	}
	
	/**
	 * Connects an Ultrasonic Sensor to the EV3 lego brick.
	 * If any error occurs during the connection, the error is converted into a boolean and returned. During this process,
	 * the sensor is enabled by default and everything for easier measures are created for the user.
	 */
	boolean connect() {
		try {
			this.device = new EV3UltrasonicSensor(this.port);
			this.device.enable();
			this.sampler = this.device.getDistanceMode();
			this.distance = new float[this.sampler.sampleSize()];
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Reading from a UltraEyes object means reading the distance to the closest physical object in front of the emitter.
	 * The value is stored inside a Measure container, as the 'value' field.
	 */
	Measure read() {
		this.sampler.fetchSample(this.distance, 0);
		return new Measure(this.distance[0]);
	}

	/**
	 * For now, there is nothing to write to a pair of UltraEyes.
	 */
	void write(Order order) {	
	}
}