package rover;


import lejos.hardware.Battery;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import tools.Beeper;
import tools.Blinker;

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
	Logger logger;
	RoverMode mode;
	
	UltraEyes us;
	ColorEye  cs;
	Engine    pm;
	Engine    rm;
	Engine    lm;
	
	// private default constructor.
	private Rover() {
		this.logger = new Logger();
		
		this.us = new UltraEyes(SensorPort.S4);
		this.cs = new ColorEye(SensorPort.S1);
		this.pm = new Engine(MotorPort.A);
		this.rm = new Engine(MotorPort.B);
		this.lm = new Engine(MotorPort.C);
	}
	// private constructor with parameters.
	private Rover(Port ultrasonic_port, Port color_port,
			     Port pliers_motor_port, Port right_motor_port, Port left_motor_port) {
		this.logger = new Logger();
		
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
		Rover rover = new Rover();
		rover.logger.open("log.log");
		return rover;
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
		Rover rover = new Rover(ultrasonic_port, color_port, pliers_motor_port, right_motor_port, left_motor_port);
		rover.logger.open("log.log");
		return rover;
	}
	
	/**
	 * Checks the battery of the rover.
	 * If the batteries are too low, the rover will enter the error mode because the mission is compromised.
	 */
	public void checkBattery() {
		float bc = Battery.getBatteryCurrent();
		float mc = Battery.getMotorCurrent();
		float bv = Battery.getVoltage();
		this.logger.println("bc: "+bc+",mc: "+mc+",bv: "+bv);
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
		this.mode.enter_diagnostic_mode();
		
		// initialize and check every peripheral. if an error occurs whilst trying to talk to a given peripheral, put it in
		// the 'error' variable.
		int error = 0;
		if (this.us.connect()) 
				{ Beeper.beep();     this.logger.println("con. us: ok"); }
		else 	{ Beeper.twoBeeps(); this.logger.println("con. us: ko (" + this.us.port.getName() + ")"); error +=  1; }
		if (this.cs.connect()) 
				{ Beeper.beep();     this.logger.println("con. cs: ok"); }
		else 	{ Beeper.twoBeeps(); this.logger.println("con. cs: ko (" + this.cs.port.getName() + ")"); error +=  2; }
		if (this.pm.connect()) 
				{ Beeper.beep();     this.logger.println("con. pm: ok"); }
		else 	{ Beeper.twoBeeps(); this.logger.println("con. pm: ko (" + this.pm.port.getName() + ")"); error +=  4; }
		if (this.rm.connect()) 
				{ Beeper.beep();     this.logger.println("con. rm: ok"); }
		else 	{ Beeper.twoBeeps(); this.logger.println("con. rm: ko (" + this.rm.port.getName() + ")"); error +=  8; }
		if (this.lm.connect()) 
				{ Beeper.beep();     this.logger.println("con. lm: ok"); }
		else 	{ Beeper.twoBeeps(); this.logger.println("con. lm: ko (" + this.lm.port.getName() + ")"); error += 16; }
		
		// diagnostic is now done.
		this.mode.stop();
		
		// if an error occurred, 'error' is non zero.
		if (error != 0) {
			// the rover enters the error mode...
			this.mode.enter_error_mode();
			this.logger.println("starting error mode");
			Button.waitForAnyPress();
			this.logger.println("ending error mode -> exit program");
			this.mode.stop();
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
		this.logger.println("starting landing mode");
		this.mode.enter_landind_mode();
		System.out.println("  -> press any key to end landing");
		Button.waitForAnyPress();
		this.logger.println("ending landing mode");
		this.mode.stop();		
	}
	/**
	 *  _____________________________________________TODO_____________________________________________.
	 */
	public void explore() {
		this.logger.println("starting exploration mode");
		this.mode.enter_exploration_mode();
		System.out.println("  -> press any key to end exploration");
		Button.waitForAnyPress();
		this.logger.println("ending exploration mode");
		this.mode.stop();
	}
	/**
	 *  _____________________________________________TODO_____________________________________________.
	 */
	public void harvest() {
		this.logger.println("starting harvest mode");
		this.mode.enter_harvest_mode();
		System.out.println("  -> press any key to end harvest");
		Button.waitForAnyPress();
		this.logger.println("ending harvest mode");
		this.mode.stop();		
	}
	/**
	 *  _____________________________________________TODO_____________________________________________ (blocking method).
	 */
	public void await() {
		this.logger.println("starting wait mode");
		this.mode.enter_wait_mode();
		System.out.println("  -> press any key to end wait");
		Button.waitForAnyPress();
		this.logger.println("ending wait mode");
		this.mode.stop();		
	}
	/**
	 *  _____________________________________________TODO_____________________________________________ (blocking method).
	 */
	public void sleep() {
		this.logger.println("starting sleep mode");
		this.mode.enter_sleep_mode();
		System.out.println("  -> press any key to end sleep");
		Button.waitForAnyPress();
		this.logger.println("ending sleep mode");
		this.mode.stop();		
	}

	//######################################################################################################################
	//### Setters and Getters ##############################################################################################
	//######################################################################################################################
	
	//######################################################################################################################
	//### Take Measures ####################################################################################################
	//######################################################################################################################
	
	//###################################################################################################################
	//### sensors tests #################################################################################################
	//###################################################################################################################
	public void test_ultrasonic_sensor() {
		this.logger.println("starting tests on ultra...");
		float dist;
		while (Button.readButtons() != Button.ID_ENTER) {
			dist = this.us.read().value;
			System.out.println(dist);
		}
		this.logger.println("ultra done");
	}
	public void test_color_sensor() {
		this.logger.println("starting tests on color...");
		float id = -1;
		while (Button.readButtons() != Button.ID_ENTER) {
			LCD.clear();
			id = this.cs.read().value;
			System.out.println("id: " + id);
		}
		this.logger.println("color done");	
	}
	
	//###################################################################################################################
	//### motors tests ##################################################################################################
	//###################################################################################################################
	public void test_motors() {
		this.logger.println("starting tests on motors...");
		Blinker.blink(Blinker.ORANGE, Blinker.FAST, 0); Button.waitForAnyPress(); Beeper.beep();
		
		this.logger.println("rotating pliers..."); this.pm.write(new Order(90, 360)); this.logger.println("done");
		Blinker.blink(Blinker.ORANGE, Blinker.SLOW, 0); Button.waitForAnyPress(); Beeper.beep();

		this.logger.println("rotating right...");  this.rm.write(new Order(90, 360)); this.logger.println("done");
		Blinker.blink(Blinker.GREEN, Blinker.SLOW, 0);  Button.waitForAnyPress(); Beeper.beep();

		this.logger.println("rotating left...");   this.lm.write(new Order(90, 360)); this.logger.println("done");
		Blinker.blink(Blinker.GREEN, Blinker.STILL, 0); Button.waitForAnyPress(); Beeper.beep();

		this.logger.println("motors done");
	}
}
