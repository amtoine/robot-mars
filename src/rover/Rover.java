package rover;

import lejos.hardware.Battery;
import lejos.hardware.Button;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import modes.ExplorationMode;
import modes.HarvestMode;
import modes.LandingMode;
import modes.SleepMode;
import modes.WaitMode;

/**
 * To carry out a mission, the most common way is to build and use a rover.
 * 
 * The Rover class aims at defining everything that a rover is composed of and needs to work properly, e.g. sensors, motors,
 * initializations and so on.
 * 
 * @author Antoine Stevan
 * 
 */
public class Rover {

	EV3UltrasonicSensor ultrasonic_sensor;
	Port us_port;

	EV3ColorSensor color_sensor;
	Port cs_port;
	
	RegulatedMotor pliers_motor;
	Port pm_port;
	
	RegulatedMotor right_motor;
	Port rm_port;
	
	RegulatedMotor left_motor;
	Port lm_port;
	
	// private default constructor.
	private Rover() {
		this.ultrasonic_sensor = null;
		this.color_sensor      = null;
		this.left_motor        = null;
		this.right_motor       = null;
		this.pliers_motor      = null;
		
		// basic setup
		this.us_port = SensorPort.S4;
		this.cs_port = SensorPort.S1;
		this.pm_port = MotorPort.A;
		this.rm_port = MotorPort.B;
		this.lm_port = MotorPort.C;
	}
	// private constructor with parameters.
	private Rover(Port ultrasonic_port, Port color_port,
			     Port pliers_motor_port, Port right_motor_port, Port left_motor_port) {
		this.ultrasonic_sensor = null;
		this.color_sensor      = null;
		this.left_motor        = null;
		this.right_motor       = null;
		this.pliers_motor      = null;
		
		// custom setup
		this.us_port = ultrasonic_port;
		this.cs_port = color_port;
		this.pm_port = pliers_motor_port;
		this.rm_port = right_motor_port;
		this.lm_port = left_motor_port;
	}
	
	/**
	 * Default constructor of a Rover.
	 * Each component is initialized with a default value. Each component is assigned to a default port on the brick : 
	 * ultrasonic (4); color (1); pliers (A); right (B); left (C).
	 * 
	 * @return a newly built default rover.
	 */
	public static Rover build() {
		return new Rover();
	}
	
	/**
	 * Constructor with parameters for a Rover.
	 * User can manually set the ports for his own rover by given a set of ports in the following order : (1) ultrasonic
	 * sensor, (2) color sensor, (3) pliers motor, (4) right motor and (5) left motor.
	 * 
	 * @param ultrasonic_port the port the ultrasonic sensor should be connected to.
	 * @param color_port the port the color sensor should be connected to.
	 * @param pliers_motor_port the port the pliers motor should be connected to.
	 * @param right_motor_port the port the right motor should be connected to.
	 * @param left_motor_port the port the left motor should be connected to.
	 * 
	 * @return a newly built rover with custom port layout.
	 */
	public static Rover build(Port ultrasonic_port, Port color_port,
			     			  Port pliers_motor_port, Port right_motor_port, Port left_motor_port) {
		return new Rover(ultrasonic_port, color_port, pliers_motor_port, right_motor_port, left_motor_port);
	}
	

	public void checkBattery() {
		float bc = Battery.getBatteryCurrent();
		float mc = Battery.getMotorCurrent();
		float bv = Battery.getVoltage();
		System.out.println("bc: "+bc+",mc: "+mc+",bv: "+bv);
	}
	
	//######################################################################################################################
	//### Peripherals initializations ######################################################################################
	//######################################################################################################################
	/**
	 * Initialization of the ulrasonic sensor.
	 * Tries to connect the brick and the ultrasonic sensor through the EV3UltrasonicSensor class. If the sensor is not
	 * connected, an error is thrown, handled by the method, converted to boolean and returned.
	 * 
	 * @return true if the ultrasonic sensor is properly connected to the right port, false if any error occurs.
	 */
	public boolean init_ultrasonic_sensor() {
		try {
			this.ultrasonic_sensor = new EV3UltrasonicSensor(this.us_port);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * Initialization of the color sensor.
	 * Tries to connect the brick and the color sensor through the EV3ColorSensor class. If the sensor is not connected, an
	 * error is thrown, handled by the method, converted to boolean and returned.
	 * 
	 * @return true if the color sensor is properly connected to the right port, false if any error occurs.
	 */
	public boolean init_color_sensor() {
		try {
			this.color_sensor = new EV3ColorSensor(this.cs_port);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * Initialization of the pliers motor.
	 * Tries to connect the brick and the pliers motor through the NXTRegulatedMotor class. If the sensor is not connected,
	 * an error is thrown, handled by the method, converted to boolean and returned.
	 * 
	 * @return true if the pliers motor is properly connected to the right port, false if any error occurs.
	 */
	public boolean init_pliers_motor() {
		try {
			this.pliers_motor = new NXTRegulatedMotor(this.pm_port);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * Initialization of the right motor.
	 * Tries to connect the brick and the right motor through the NXTRegulatedMotor class. If the sensor is not connected,
	 * an error is thrown, handled by the method, converted to boolean and returned.
	 * 
	 * @return true if the right motor is properly connected to the right port, false if any error occurs.
	 */
	public boolean init_right_motor() {
		try {
			this.right_motor = new NXTRegulatedMotor(this.rm_port);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * Initialization of the left motor.
	 * Tries to connect the brick and the left motor through the NXTRegulatedMotor class. If the sensor is not connected,
	 * an error is thrown, handled by the method, converted to boolean and returned.
	 * 
	 * @return true if the left motor is properly connected to the right port, false if any error occurs.
	 */
	public boolean init_left_motor() {
		try {
			this.left_motor = new NXTRegulatedMotor(this.lm_port);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * Initializes all the peripherals that should be connected to the rover.
	 * Call every initialization methods through the internal doctor.
	 * 
	 * @see #init_ultrasonic_sensor()
	 * @see #init_color_sensor()
	 * @see #init_pliers_motor()
	 * @see #init_right_motor()
	 * @see #init_left_motor()
	 */
	public void init_peripherals() {
		InternalDoctor.init_peripherals(this);	
	}
	
	//######################################################################################################################
	//### Rover Modes ######################################################################################################
	//######################################################################################################################
	
	/**
	 *  _____________________________________________TODO_____________________________________________ (blocking method).
	 */
	public void land() {
		LandingMode mode = new LandingMode();
		mode.start();
		System.out.println("  -> press any key to end landing");
		Button.waitForAnyPress();
		mode.stop();		
	}
	/**
	 *  _____________________________________________TODO_____________________________________________.
	 */
	public void explore() {
		ExplorationMode mode = new ExplorationMode();
		mode.start();
		System.out.println("  -> press any key to end exploration");
		Button.waitForAnyPress();
		mode.stop();		
	}
	/**
	 *  _____________________________________________TODO_____________________________________________.
	 */
	public void harvest() {
		HarvestMode mode = new HarvestMode();
		mode.start();
		System.out.println("  -> press any key to end harvest");
		Button.waitForAnyPress();
		mode.stop();		
	}
	/**
	 *  _____________________________________________TODO_____________________________________________ (blocking method).
	 */
	public void await() {
		WaitMode mode = new WaitMode();
		mode.start();
		System.out.println("  -> press any key to end wait");
		Button.waitForAnyPress();
		mode.stop();		
	}
	/**
	 *  _____________________________________________TODO_____________________________________________ (blocking method).
	 */
	public void sleep() {
		SleepMode mode = new SleepMode();
		mode.start();
		System.out.println("  -> press any key to end sleep");
		Button.waitForAnyPress();
		mode.stop();		
	}

	//######################################################################################################################
	//### Setters and Getters ##############################################################################################
	//######################################################################################################################
//	/**
//	 * Setter for the ultrasonic sensor.
//	 * 
//	 * @param ultrasonic_sensor the new ultrasonic sensor.
//	 */
//	public void set_ultrasonic_sensor(EV3UltrasonicSensor ultrasonic_sensor) {
//		this.ultrasonic_sensor = ultrasonic_sensor;
//	}
//	/**
//	 * Setter for the color sensor.
//	 * 
//	 * @param color_sensor the new color sensor.
//	 */
//	public void set_color_sensor(EV3ColorSensor color_sensor) {
//		this.color_sensor = color_sensor;
//	}
//	/**
//	 * Setter for the pliers motor.
//	 * 
//	 * @param motor the new pliers motor.
//	 */
//	public void set_right_motor(RegulatedMotor motor) {
//		this.right_motor = motor;
//	}
//	/**
//	 * Setter for the right motor.
//	 * 
//	 * @param motor the new right motor.
//	 */
//	public void set_left_motor(RegulatedMotor motor) {
//		this.left_motor = motor;
//	}
//	/**
//	 * Setter for the left motor.
//	 * 
//	 * @param motor the new left motor.
//	 */
//	public void set_pliers_motor(RegulatedMotor motor) {
//		this.pliers_motor = motor;
//	}
	
//	/**
//	 * Getter for the ultrasonic sensor.
//	 * 
//	 * @return the current ultrasonic sensor.
//	 */
//	public EV3UltrasonicSensor get_ultrasonic_sensor() {
//		return this.ultrasonic_sensor;
//	}
//	/**
//	 * Getter for the color sensor.
//	 * 
//	 * @return the current color sensor.
//	 */
//	public EV3ColorSensor get_color_sensor() {
//		return this.color_sensor;
//	}
	/**
	 * Getter for the pliers motor.
	 * 
	 * @return the current pliers motor.
	 */
	public RegulatedMotor get_right_motor() {
		return this.right_motor;
	}
	/**
	 * Getter for the right motor.
	 * 
	 * @return the current right motor.
	 */
	public RegulatedMotor get_left_motor() {
		return this.left_motor;
	}
	/**
	 * Getter for the left motor.
	 * 
	 * @return the current left motor.
	 */
	public RegulatedMotor get_pliers_motor() {
		return this.pliers_motor;
	}
	
	public float take_us_measure() {
		this.ultrasonic_sensor.enable();
		SampleProvider sp_us = this.ultrasonic_sensor.getDistanceMode();
		float[] dist = new float[sp_us.sampleSize()];
		sp_us.fetchSample(dist, 0);
		return dist[0];
	}
	
	public int take_cs_measure() {
		System.out.println("red: " + this.color_sensor.getRedMode().getName());
		System.out.println("rgb: " + this.color_sensor.getRGBMode().getName());
		return this.color_sensor.getColorID();
	}
}
