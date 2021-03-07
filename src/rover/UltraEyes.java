package rover;

import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;

class UltraEyes extends Peripheral {

	EV3UltrasonicSensor device;
	private SampleProvider sampler;
	private float[] distance;
	UltraEyes(Port port){
		this.device   = null;
		this.sampler  = null;
		this.distance = null;
		this.port     = port;
	}
	
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

	Measure read() {
		this.sampler.fetchSample(this.distance, 0);
		return new Measure(this.distance[0]);
	}

	void write(Order order) {
		
	}

}