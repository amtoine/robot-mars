package rover;


import lejos.hardware.Battery;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
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
	Grabber   pliers;
	Engine    rm;
	Engine    lm;
	
	Navigator nav;
	
	SampleSensor sp_sensor;
	
	static int WHEEL_DIAMETER = 40;
	static int HALF_WIDTH = 63;
	
	// private default constructor.
	private Rover() {
		this.logger = new Logger();
		
		this.us = new UltraEyes(SensorPort.S4);
		this.cs = new ColorEye(SensorPort.S1);
		this.pliers = new Grabber(MotorPort.A);
		this.rm = new Engine(MotorPort.B);
		this.lm = new Engine(MotorPort.C);
	}
	// private constructor with parameters.
	private Rover(Port ultrasonic_port, Port color_port,
			     Port pliers_motor_port, Port right_motor_port, Port left_motor_port) {
		this.logger = new Logger();
		
		this.us = new UltraEyes(ultrasonic_port);
		this.cs = new ColorEye(color_port);
		this.pliers = new Grabber(pliers_motor_port);
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
		
		int bat = (int) (bv*4/9000);
		switch (bat) {
			case 3:
				this.logger.println("battery is full");
				Blinker.blink(Blinker.GREEN, Blinker.SLOW);
				break;
			case 2:
				this.logger.println("battery is ok");
				Blinker.blink(Blinker.ORANGE, Blinker.SLOW);
				break;
			case 1:
				this.logger.println("battery is low");
				Blinker.blink(Blinker.ORANGE, Blinker.FAST);
				break;
			case 0:
				this.logger.println("battery is empty");
				Blinker.blink(Blinker.RED, Blinker.FAST);
				break;
		}
		
		Button.waitForAnyPress(10000);
		if (bat == 0) { this.error(); } 
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
		else 	{ Beeper.twoBeeps(); this.logger.println("con. us: ko (" + this.us.port.getName() + ")");
		          error +=  1; }
		if (this.cs.connect()) 
				{ Beeper.beep();     this.logger.println("con. cs: ok"); }
		else 	{ Beeper.twoBeeps(); this.logger.println("con. cs: ko (" + this.cs.port.getName() + ")");
		          error +=  2; }
		if (this.pliers.connect()) 
				{ Beeper.beep();     this.logger.println("con. pm: ok"); }
		else 	{ Beeper.twoBeeps(); this.logger.println("con. pm: ko (" + this.pliers.motor.port.getName() + ")");
		          error +=  4; }
		if (this.rm.connect()) 
				{ Beeper.beep();     this.logger.println("con. rm: ok"); }
		else 	{ Beeper.twoBeeps(); this.logger.println("con. rm: ko (" + this.rm.port.getName() + ")");
		          error +=  8; }
		if (this.lm.connect()) 
				{ Beeper.beep();     this.logger.println("con. lm: ok"); }
		else 	{ Beeper.twoBeeps(); this.logger.println("con. lm: ko (" + this.lm.port.getName() + ")");
	              error += 16; }
		
		// diagnostic is now done.
		this.mode.stop();
		
		// if an error occurred, 'error' is non zero.
		if (error != 0) { this.error(); }	
	}
	
	public void wake_up_navigator() {
		Wheel left  = WheeledChassis.modelWheel(this.rm.device, WHEEL_DIAMETER).offset(-HALF_WIDTH);
		Wheel right = WheeledChassis.modelWheel(this.lm.device, WHEEL_DIAMETER).offset( HALF_WIDTH);
		Chassis chassis = new WheeledChassis(new Wheel[] {right, left}, WheeledChassis.TYPE_DIFFERENTIAL);
		
		this.nav = new Navigator(new MovePilot(chassis), chassis.getPoseProvider());
	}
	
	public void wake_up_sample_sensor() {
		this.sp_sensor = new SampleSensor(2,us,nav.getPoseProvider(),nav);
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
	public Point[] explore() {
		this.logger.println("starting exploration mode");
		this.mode.enter_exploration_mode();
		
		nav.rotateTo(-90);
		Point[] sp_poses = sp_sensor.scan(180, false);
		if(sp_poses[0].x==-1000) {
			this.logger.println("x,y: " + sp_poses[0].x + "," + sp_poses[0].y);
			this.logger.println("ending exploration mode");
			this.mode.stop();
			return sp_poses;
		}
		nav.rotateTo(0);
		nav.goTo(200, 60);
		nav.rotateTo(90);
		sp_poses = sp_sensor.scan(180, false);
		this.logger.println("x,y: " + sp_poses[0].x + "," + sp_poses[0].y);
		this.logger.println("ending exploration mode");
		this.mode.stop();
		return sp_poses;
		
		//System.out.println("  -> press any key to end exploration");
		//Button.waitForAnyPress();
	}
	/**
	 *  _____________________________________________TODO_____________________________________________.
	 */
	public void harvest() {
		this.logger.println("starting harvest mode");
		this.mode.enter_harvest_mode();
		
		//System.out.println("  -> press any key to end harvest");
		//Button.waitForAnyPress();
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
	/**
	 *  _____________________________________________TODO_____________________________________________ (blocking method).
	 */
	public void error() {
		this.logger.println("starting error mode");
		// the rover enters the error mode...
		this.mode.enter_error_mode();
		System.out.println("  -> press any key to exit");
		Button.waitForAnyPress();
		this.logger.println("ending error mode -> exit program");
		this.mode.stop();
		// and program halts when a button is pressed.
		System.exit(1);
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
		// reseting the tacho counts.
		this.pliers.motor.device.resetTachoCount();
		this.rm.device.resetTachoCount();
		this.lm.device.resetTachoCount();
		
		Blinker.blink(Blinker.ORANGE, Blinker.FAST, 0); Button.waitForAnyPress(); Beeper.beep();
		
		// pliers
		this.logger.println("closing pliers..."); this.logger.println(this.pliers.getTachoCount());
		this.pliers.grab();
		while (this.pliers.isMoving()) {
			this.logger.println(this.pliers.getTachoCount() +
					            "(" + this.pliers.motor.device.getRotationSpeed() + ")");
		}
		this.logger.println("releasing pliers..."); this.logger.println(this.pliers.getTachoCount());
		this.pliers.release();
		while (this.pliers.isMoving()) {
			this.logger.println(this.pliers.getTachoCount() +
					            "(" + this.pliers.motor.device.getRotationSpeed() + ")");
		}
		this.logger.println("done");
		Blinker.blink(Blinker.ORANGE, Blinker.SLOW, 0); Button.waitForAnyPress(); Beeper.beep();

		// right track
		this.logger.println("rotating right..."); this.logger.println(this.rm.device.getTachoCount());
		this.rm.write(new Order(90, 360));  
		while (this.rm.device.isMoving()) {
			this.logger.println(this.rm.device.getTachoCount() + "(" + this.rm.device.getRotationSpeed() + ")");
		}
		this.logger.println("done");
		Blinker.blink(Blinker.GREEN, Blinker.SLOW, 0);  Button.waitForAnyPress(); Beeper.beep();

		// left track
		this.logger.println("rotating left..."); this.logger.println(this.lm.device.getTachoCount());
		this.lm.write(new Order(90, 360));  
		while (this.lm.device.isMoving()) {
			this.logger.println(this.lm.device.getTachoCount() + "(" + this.lm.device.getRotationSpeed() + ")");
		}
		this.logger.println("done");
		Blinker.blink(Blinker.GREEN, Blinker.STILL, 0); Button.waitForAnyPress(); Beeper.beep();

		// end of tests
		this.logger.println("motors done");
	}
	
	//###################################################################################################################
	//### navigator tests ###############################################################################################
	//###################################################################################################################
	public void test_navigator() {
		Pose pose = this.nav.getPoseProvider().getPose();
		this.logger.println("("+pose.getX()+","+pose.getY()+") at "+pose.getHeading());
		
		this.nav.goTo(new Waypoint(pose.pointAt(10, pose.getHeading()+90)));
	}
}
