package rover;

import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RegulatedMotor;

public class Rover {
	
	private EV3UltrasonicSensor ultrasonic_sensor;
	private Port ultrasonic_port;
	
	private EV3ColorSensor color_sensor;
	private Port color_port;
	
	private RegulatedMotor pliers_motor;
	private Port pliers_motor_port;
	
	private RegulatedMotor right_motor;
	private Port right_motor_port;
	
	private RegulatedMotor left_motor;
	private Port left_motor_port;
	
	public Rover() {
		this.ultrasonic_sensor = null;
		this.color_sensor       = null;
		this.left_motor         = null;
		this.right_motor        = null;
		this.pliers_motor       = null;
		
		// basic setup
		this.ultrasonic_port   = SensorPort.S4;
		this.color_port        = SensorPort.S1;
		this.pliers_motor_port = MotorPort.A;
		this.right_motor_port  = MotorPort.B;
		this.left_motor_port   = MotorPort.C;
	}
	
	public Rover(Port ultrasonic_port, Port color_port,
			     Port pliers_motor_port, Port right_motor_port, Port left_motor_port) {
		this.ultrasonic_sensor = null;
		this.color_sensor      = null;
		this.left_motor        = null;
		this.right_motor       = null;
		this.pliers_motor      = null;
		
		// custom setup
		this.ultrasonic_port   = ultrasonic_port;
		this.color_port        = color_port;
		this.pliers_motor_port = pliers_motor_port;
		this.right_motor_port  = right_motor_port;
		this.left_motor_port   = left_motor_port;
	}
	
	public void init_ultrasonic_sensor() throws Exception {
		try {
			this.ultrasonic_sensor = new EV3UltrasonicSensor(this.ultrasonic_port);
		} catch (Exception e) {
			throw new Exception("ultrasonic sensor not connected to port " + this.ultrasonic_port.getName());
		}
	}
	public void init_color_sensor() throws Exception {
		try {
			this.color_sensor = new EV3ColorSensor(this.color_port);
		} catch (Exception e) {
			throw new Exception("color sensor not connected to port " + this.color_port.getName());
		}
	}
	public void init_pliers_motor() throws Exception {
		try {
			this.pliers_motor = new NXTRegulatedMotor(this.pliers_motor_port);
		} catch (Exception e) {
			throw new Exception("pliers motor not connected to port " + this.pliers_motor_port.getName());
		}
	}
	public void init_right_motor() throws Exception {
		try {
			this.right_motor = new NXTRegulatedMotor(this.right_motor_port);
		} catch (Exception e) {
			throw new Exception("right motor not connected to port " + this.right_motor_port.getName());
		}
	}
	public void init_left_motor() throws Exception {
		try {
			this.left_motor = new NXTRegulatedMotor(this.left_motor_port);
		} catch (Exception e) {
			throw new Exception("left motor not connected to port " + this.left_motor_port.getName());
		}
	}
	
	
	public void set_ultrasonic_sensor(EV3UltrasonicSensor ultrasonic_sensor) {
		this.ultrasonic_sensor = ultrasonic_sensor;
	}
	public void set_color_sensor(EV3ColorSensor color_sensor) {
		this.color_sensor = color_sensor;
	}
	public void set_right_motor(RegulatedMotor motor) {
		this.right_motor = motor;
	}
	public void set_left_motor(RegulatedMotor motor) {
		this.left_motor = motor;
	}
	public void set_pliers_motor(RegulatedMotor motor) {
		this.pliers_motor = motor;
	}
	
	public EV3UltrasonicSensor get_ultrasonic_sensor() {
		return this.ultrasonic_sensor;
	}
	public EV3ColorSensor get_color_sensor() {
		return this.color_sensor;
	}
	public RegulatedMotor get_right_motor() {
		return this.right_motor;
	}
	public RegulatedMotor get_left_motor() {
		return this.left_motor;
	}
	public RegulatedMotor get_pliers_motor() {
		return this.pliers_motor;
	}
}
