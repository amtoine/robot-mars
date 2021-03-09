package rover;

import lejos.hardware.port.Port;

class Grabber {
	Engine motor;
	int motor_speed;
	int pliers_opening_angle;
	
	Grabber(Port port){
		this.motor = new Engine(port);
		this.motor_speed = 90;
		this.pliers_opening_angle = 100;
	}
	
	boolean connect() {
		return this.motor.connect();
	}

	int getTachoCount() {
		return this.motor.device.getTachoCount();
	}

	boolean isMoving() {
		return this.motor.device.isMoving();
	}

	public void grab() {
		this.motor.device.forward();
		this.motor.write(new Order(this.motor_speed, this.pliers_opening_angle));
		this.motor.device.stop();
	}

	public void release() {
		this.motor.device.backward();
		this.motor.write(new Order(this.motor_speed, -this.pliers_opening_angle));
		this.motor.device.stop();
	}
}
