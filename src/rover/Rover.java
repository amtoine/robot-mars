package rover;


import lejos.hardware.Battery;
import lejos.hardware.Button;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import modes.DiagnosticMode;
import modes.ErrorMode;
import modes.ExplorationMode;
import modes.HarvestMode;
import modes.LandingMode;
import modes.RoverMode;
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
	UltraEyes us;
	ColorEye cs;
	Engine pm;
	Engine rm;
	Engine lm;
	
	// private default constructor.
	private Rover() {
		this.us = new UltraEyes(SensorPort.S4);
		this.cs = new ColorEye(SensorPort.S1);
		this.pm = new Engine(MotorPort.A);
		this.rm = new Engine(MotorPort.B);
		this.lm = new Engine(MotorPort.C);
	}
	// private constructor with parameters.
	private Rover(Port ultrasonic_port, Port color_port,
			     Port pliers_motor_port, Port right_motor_port, Port left_motor_port) {
		this.us = new UltraEyes(ultrasonic_port);
		this.cs = new ColorEye(color_port);
		this.pm = new Engine(pliers_motor_port);
		this.rm = new Engine(right_motor_port);
		this.lm = new Engine(left_motor_port);
	}
	
	/**
	 * Default constructor of a Rover.
	 * Each component is initialized with a default value. Each component is assigned to a default port on the brick : 
	 * ultrasonic (4); color (1); pliers (A); right (B); left (C).
	 * 
	 * @return a newly built default rover.
	 */
	public static Rover build() {
		Logger.open("log.log");
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
		Logger.open("log.log");
		return new Rover(ultrasonic_port, color_port, pliers_motor_port, right_motor_port, left_motor_port);
	}
	

	public void checkBattery() {
		float bc = Battery.getBatteryCurrent();
		float mc = Battery.getMotorCurrent();
		float bv = Battery.getVoltage();
		Logger.println("bc: "+bc+",mc: "+mc+",bv: "+bv);
	}
	
	//######################################################################################################################
	//### Connecting peripherals through wires #############################################################################
	//######################################################################################################################
	/**
	 * Used to initialize all the peripherals of the rover.
	 * Initializes all the used peripherals of the rover, i.e. the two motors for the tracks, the motor that controls the
	 * pliers, the color sensor and the ultrasonic sensor. When one or more of the above listed peripherals does not respond
	 * the error is handled and eventually stops the execution of the mission. It is sad, but the rover cannot accomplish
	 * its mission with event one motor down.
	 */
	public void connect_peripherals() {
		// first the rover enters the diagnostic mode.
		RoverMode mode = new DiagnosticMode();
		mode.start();
		
		// initialize and check every peripheral. if an error occurs whilst trying to talk to a given peripheral, put it in
		// the 'error' variable.
		int error = 0;
		if (this.us.connect()) 
				{ Beeper.beep();     Logger.println("con. us: ok"); }
		else 	{ Beeper.twoBeeps(); Logger.println("con. us: ko (" + this.us.port.getName() + ")"); error +=  1; }
		if (this.cs.connect()) 
				{ Beeper.beep();     Logger.println("con. cs: ok"); }
		else 	{ Beeper.twoBeeps(); Logger.println("con. cs: ko (" + this.cs.port.getName() + ")"); error +=  2; }
		if (this.pm.connect()) 
				{ Beeper.beep();     Logger.println("con. pm: ok"); }
		else 	{ Beeper.twoBeeps(); Logger.println("con. pm: ko (" + this.pm.port.getName() + ")"); error +=  4; }
		if (this.rm.connect()) 
				{ Beeper.beep();     Logger.println("con. rm: ok"); }
		else 	{ Beeper.twoBeeps(); Logger.println("con. rm: ko (" + this.rm.port.getName() + ")"); error +=  8; }
		if (this.lm.connect()) 
				{ Beeper.beep();     Logger.println("con. lm: ok"); }
		else 	{ Beeper.twoBeeps(); Logger.println("con. lm: ko (" + this.lm.port.getName() + ")"); error += 16; }
		
		// diagnostic is now done.
		mode.stop();
		
		// if an error occurred, 'error' is non zero.
		if (error != 0) {
			// the rover enters the error mode...
			mode = new ErrorMode();
			Logger.println("starting error mode");
			mode.start();
			Button.waitForAnyPress();
			Logger.println("ending error mode -> exit program");
			// and program halts when a button is pressed.
			System.exit(1);
		}	
	}
	
	//######################################################################################################################
	//### Rover Modes ######################################################################################################
	//######################################################################################################################
	
	/**
	 *  _____________________________________________TODO_____________________________________________ (blocking method).
	 */
	public void land() {
		LandingMode mode = new LandingMode();
		Logger.println("starting landing mode");
		mode.start();
		System.out.println("  -> press any key to end landing");
		Button.waitForAnyPress();
		Logger.println("ending landing mode");
		mode.stop();		
	}
	/**
	 *  _____________________________________________TODO_____________________________________________.
	 */
	public void explore() {
		ExplorationMode mode = new ExplorationMode();
		Logger.println("starting exploration mode");
		mode.start();
		System.out.println("  -> press any key to end exploration");
		Button.waitForAnyPress();
		Logger.println("ending exploration mode");
		mode.stop();
	}
	/**
	 *  _____________________________________________TODO_____________________________________________.
	 */
	public void harvest() {
		HarvestMode mode = new HarvestMode();
		Logger.println("starting harvest mode");
		mode.start();
		System.out.println("  -> press any key to end harvest");
		Button.waitForAnyPress();
		Logger.println("ending harvest mode");
		mode.stop();		
	}
	/**
	 *  _____________________________________________TODO_____________________________________________ (blocking method).
	 */
	public void await() {
		WaitMode mode = new WaitMode();
		Logger.println("starting wait mode");
		mode.start();
		System.out.println("  -> press any key to end wait");
		Button.waitForAnyPress();
		Logger.println("ending wait mode");
		mode.stop();		
	}
	/**
	 *  _____________________________________________TODO_____________________________________________ (blocking method).
	 */
	public void sleep() {
		SleepMode mode = new SleepMode();
		Logger.println("starting sleep mode");
		mode.start();
		System.out.println("  -> press any key to end sleep");
		Button.waitForAnyPress();
		Logger.println("ending sleep mode");
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
	 * Getter for the left motor.
	 * 
	 * @return the current left motor.
	 */
	public RegulatedMotor get_pliers_motor() {
		return this.pm.device;
//		return this.pliers_motor;
	}
	/**
	 * Getter for the pliers motor.
	 * 
	 * @return the current pliers motor.
	 */
	public RegulatedMotor get_right_motor() {
		return this.rm.device;
//		return this.right_motor;
	}
	/**
	 * Getter for the right motor.
	 * 
	 * @return the current right motor.
	 */
	public RegulatedMotor get_left_motor() {
		return this.lm.device;
//		return this.left_motor;
	}
	
	//######################################################################################################################
	//### Take Measures ####################################################################################################
	//######################################################################################################################
	public float take_us_measure() {
		this.us.device.enable();
		SampleProvider sp_us = this.us.device.getDistanceMode();
		float[] dist = new float[sp_us.sampleSize()];
		sp_us.fetchSample(dist, 0);
		return dist[0];
	}
	
	public int take_cs_measure() {
		System.out.println("red: " + this.cs.device.getRedMode().getName());
		System.out.println("rgb: " + this.cs.device.getRGBMode().getName());
		return this.cs.device.getColorID();
	}
}
