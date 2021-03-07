package rover;


import lejos.hardware.Battery;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
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
	ColorEye  cs;
	Engine    pm;
	Engine    rm;
	Engine    lm;
	
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
	
	/**
	 * Checks the battery of the rover.
	 * If the batteries are too low, the rover will enter the error mode because the mission is compromised.
	 */
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
	
	//######################################################################################################################
	//### Take Measures ####################################################################################################
	//######################################################################################################################
	public float take_us_measure() {
		return this.us.read().value;
	}
	
	public float take_cs_measure() {
		return this.cs.read().value;
	}
	
	//###################################################################################################################
	//### sensors tests #################################################################################################
	//###################################################################################################################
	public void test_ultrasonic_sensor() {
		float dist;
		while (Button.readButtons() != Button.ID_ENTER) {
			dist = this.take_us_measure();
			System.out.println(dist);
		}		
	}
	public void test_color_sensor() {
		float id = -1;
		while (Button.readButtons() != Button.ID_ENTER) {
			LCD.clear();
			id = this.take_cs_measure();
			System.out.println("id: " + id);
		}		
	}
	
	//###################################################################################################################
	//### motors tests ##################################################################################################
	//###################################################################################################################
	public void test_motors() {
		Blinker.blink(Blinker.ORANGE, Blinker.FAST, 0); Button.waitForAnyPress(); Beeper.beep();
		
		this.pm.write(new Order(90, 360));
		Blinker.blink(Blinker.ORANGE, Blinker.SLOW, 0); Button.waitForAnyPress(); Beeper.beep();

		this.rm.write(new Order(90, 360));
		Blinker.blink(Blinker.GREEN, Blinker.SLOW, 0); Button.waitForAnyPress(); Beeper.beep();
		
		this.lm.write(new Order(90, 360));
		Blinker.blink(Blinker.GREEN, Blinker.STILL, 0); Button.waitForAnyPress(); Beeper.beep();		
	}
}
