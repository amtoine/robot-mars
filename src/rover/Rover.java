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
	/**	Output stream to write both in the console and in the log file. */
	Logger logger;
	/**	Current mode of the rover, used to broadcast appropriate sound and light effects. */
	RoverMode mode;
	
	/** The ultrasonic sensor of the rover. */
	UltraEyes us;
	/** The color sensor of the rover. */
	ColorEye  cs;
	/** The pliers in front of the rover, used to grab samples. */
	Grabber   pliers;
	/** The right motor of the rover, used to actuate the right track. */
	Engine    rm;
	/** The left motor of the rover, used to actuate the left track. */
	Engine    lm;
	
	/** The navigator controlling the rover's movement inside the intervention zone. */
	Navigator nav;
	
	SampleSensor sp_sensor;
	
	/** The diameter of the wheels, expressed in mm. */
	static int WHEEL_DIAMETER = 40;
	/** The half distance between the two axis of the tracks, expressed in mm. */
	static int HALF_WIDTH = 63;
	/** As the battery is full with 9000mV, we assume that the situation is critical below 10%, i.e. 900mV*/
	private static final int VOLTAGE_THRESHOLD = 900;
	
	// private default constructor.
	private Rover() {
		this.logger = new Logger();
		this.mode = new RoverMode();
		
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
		this.mode = new RoverMode();
		
		this.us = new UltraEyes(ultrasonic_port);
		this.cs = new ColorEye(color_port);
		this.pliers = new Grabber(pliers_motor_port);
		this.rm = new Engine(right_motor_port);
		this.lm = new Engine(left_motor_port);
	}
	
	/**
	 * Default constructor of a Rover.
	 * Each component is initialized with a default value. Each component is hence assigned to a default port on the
	 * brick : ultrasonic (S4); color (S1); pliers (MA); right (MB); left (MC).
	 * 
	 * @return a newly built default rover.
	 */
	public static Rover build() {
		Rover rover = new Rover();
		// the logger's output streams need to be opened.
		rover.logger.open("log.log");
		return rover;
	}
	
	/**
	 * Constructor with parameters for a Rover.
	 * User can manually set the ports for his own rover by giving a set of ports in the following order : (1) ultrasonic
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
		// the logger's output streams need to be opened.
		rover.logger.open("log.log");
		return rover;
	}
	
	/**
	 * Checks the battery of the rover.
	 * If the batteries are too low, the rover will enter the error mode because the mission is compromised. As said in the
	 * leJOS documentation, the embedded battery can hold up to 6 1.5V alkaline batteries, which correspond to a total of
	 * 9V. The battery voltage level is expressed in volts (V) or millivolts (mV), i.e. 9 or 9000 when Battey.getVoltage~()
	 * is called. If the voltage drops below a certain threshold, the rover will enter the error mode.
	 */
	public void checkBattery() {
		this.logger.println("checking batteries");
		this.mode.enter_diagnostic_mode();
		// measure the battery voltage level.
		int bv = Battery.getVoltageMilliVolt();
		
		// compute voltages slices for enhanced display and log.
		int bat = bv/1000;
		if      (bat == 8) 	{ this.logger.print("battery is full");        Blinker.blink(Blinker.GREEN,  Blinker.STILL); }
		else if (bat == 7) 	{ this.logger.print("battery is almost full"); Blinker.blink(Blinker.GREEN,  Blinker.SLOW); }
		else if (bat == 6) 	{ this.logger.print("battery is very good");   Blinker.blink(Blinker.GREEN,  Blinker.FAST); }
		else if (bat == 5) 	{ this.logger.print("battery is good");        Blinker.blink(Blinker.ORANGE, Blinker.STILL); }
		else if (bat == 4) 	{ this.logger.print("battery is half");        Blinker.blink(Blinker.ORANGE, Blinker.SLOW); }
		else if (bat == 3) 	{ this.logger.print("battery is fine");        Blinker.blink(Blinker.ORANGE, Blinker.FAST); }
		else if (bat == 2) 	{ this.logger.print("battery is low");         Blinker.blink(Blinker.RED,    Blinker.STILL); }
		else if (bat == 1) 	{ this.logger.print("battery is very low");    Blinker.blink(Blinker.RED,    Blinker.SLOW); }
		else if (bat == 0) 	{ this.logger.print("battery is critical");    Blinker.blink(Blinker.RED,    Blinker.FAST); }
		this.logger.println("with " + bv + " mV");
		
		this.mode.stop();
		this.logger.println("battery checked");
		
		Button.waitForAnyPress(5000);
		// throw error if the battery is too low.
		if (bv < VOLTAGE_THRESHOLD) { this.error(); } 
	}
	
	//######################################################################################################################
	//### Connecting peripherals through wires #############################################################################
	//######################################################################################################################
	/**
	 * Used to initialize all the peripherals of the rover.
	 * Initializes all the used peripherals of the rover, i.e. the two motors for the tracks, the motor that controls the
	 * pliers, the color sensor and the ultrasonic sensor. When one or more of the above listed peripherals does not respond
	 * the error is handled and eventually stops the execution of the mission. It is sad, but the rover cannot accomplish
	 * its mission with even one motor or sensor down.
	 * 
	 * Example 
	 * 
	 * @see Engine
	 * @see UltraEyes
	 * @see ColorEye
	 */
	public void connect_peripherals() {
		// first the rover enters the diagnostic mode.
		this.logger.println("starting diagnostic mode");
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
		this.logger.println("ending diagnostic mode");
		
		// if an error occurred, 'error' is non zero.
		if (error != 0) { this.error(); }	
	}
	
	/**
	 * Before trying to accomplish any mission, the rover needs a navigator.
	 * To go from one location to another, the rover uses a navigator which handles the tracks control part. This method
	 * wakes up the navigator by initializing wheels, chassis and navigator from leJOS.
	 */
	public void wake_up_navigator() {
		// use structural constants to build the wheels and the chassis.
		Wheel left  = WheeledChassis.modelWheel(this.rm.device, WHEEL_DIAMETER).offset(-HALF_WIDTH);
		Wheel right = WheeledChassis.modelWheel(this.lm.device, WHEEL_DIAMETER).offset( HALF_WIDTH);
		Chassis chassis = new WheeledChassis(new Wheel[] {right, left}, WheeledChassis.TYPE_DIFFERENTIAL);
		
		// from the wheels and the chassis, extract a navigator.
		this.nav = new Navigator(new MovePilot(chassis), chassis.getPoseProvider());
	}
	
	public void wake_up_sample_sensor() {
		this.sp_sensor = new SampleSensor(2,us,nav.getPoseProvider(),nav);
	}
	
	public static Point convertPose(boolean relative,Point p,Pose rover_pose) {
		if(relative) {
			p.x = (p.x-rover_pose.getX());
			p.y = (p.y-rover_pose.getY());
		}
		return p;
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
		Point[] samples = sp_sensor.scan(180, false);
		if(samples[0].x==-1000) {
			this.logger.println("x,y: " + samples[0].x + "," + samples[0].y);
			this.logger.println("ending exploration mode");
			this.mode.stop();
			return samples;
		}
		
		nav.rotateTo(0);
		nav.goTo(200, 60);
		nav.rotateTo(90);
		samples = sp_sensor.scan(180, false);
		this.logger.println("x,y: " + samples[0].x + "," + samples[0].y);
		this.logger.println("ending exploration mode");
		this.mode.stop();
		return samples;
		
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
	/** */
	public void test_ultrasonic_sensor() {
		this.logger.println("starting tests on ultra...");
		float dist;
		while (Button.readButtons() != Button.ID_ENTER) {
			dist = this.us.read().value;
			System.out.println(dist);
		}
		this.logger.println("ultra done");
	}
	/** */
	public void test_color_sensor() {
		this.logger.println("starting tests on color...");
		float id = -1;
		while (Button.readButtons() != Button.ID_ENTER) {
			LCD.clear();
			id = this.cs.read().value;
			System.out.println("id: " + id);
			Button.waitForAnyPress();
		}
		this.logger.println("color done");	
	}
	
	//###################################################################################################################
	//### motors tests ##################################################################################################
	//###################################################################################################################
	/** */
	public void test_motors() {
		this.logger.println("starting tests on motors...");
		// reseting the tacho counts.
		this.pliers.motor.device.resetTachoCount();
		this.rm.device.resetTachoCount();
		this.lm.device.resetTachoCount();
		
		Blinker.blink(Blinker.ORANGE, Blinker.FAST, 0); Button.waitForAnyPress(); Beeper.beep();
		
		// pliers
		this.logger.println("closing pliers..."); this.logger.println(this.pliers.getTachoCount());
		this.pliers.motor.device.setAcceleration(90);
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
	/** */
	public void test_navigator() {
		Pose pose = this.nav.getPoseProvider().getPose();
		this.logger.println("("+pose.getX()+","+pose.getY()+") at "+pose.getHeading());
		
		this.nav.goTo(new Waypoint(pose.pointAt(100, pose.getHeading()+90)));
	}
}
