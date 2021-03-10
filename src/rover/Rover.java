package rover;


import java.util.Arrays;

import lejos.hardware.Battery;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import tools.Beeper;
import tools.Blinker;
import tools.Order;

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
	UltraEyes ultra;
	/** The color sensor of the rover. */
	ColorEye  color;
	/** The pliers in front of the rover, used to grab samples. */
	Grabber   pliers;
	/** The right motor of the rover, used to actuate the right track. */
	Engine    right;
	/** The left motor of the rover, used to actuate the left track. */
	Engine    left;
	
	/** The navigator controlling the rover's movement inside the intervention zone. */
	Navigator nav;
	
	/** The diameter of the wheels, expressed in mm. */
	static final int WHEEL_RADIUS = 56/2;
	/** The half distance between the two axis of the tracks, expressed in mm. */
	static final int HALF_WIDTH = 138/2;
	/** As the battery is full with 9000mV, we assume that the situation is critical below 10%, i.e. 900mV*/
	private static final int VOLTAGE_THRESHOLD = 900;

	// position of the ultrasonic sensor w.r.t. the center of rotation of the rover.
	static final int   ULTRA_Dx    = 0;
	static final int   ULTRA_Dy    = 0;
	static final float ULTRA_R2    = ULTRA_Dx*ULTRA_Dx + ULTRA_Dy*ULTRA_Dy;
	static final float ULTRA_R     = (float)Math.sqrt(ULTRA_R2);
	static final float ULTRA_THETA = (float)Math.atan2(ULTRA_Dy, ULTRA_Dx);
	
	/** A map of the whole intervention zone. */
	static final MapZone map = new Map();
	/** A map of the recuperation zone which is a subset of the intervention zone. */
	static final MapZone recup_zone = new RecupZone();
	/** The maximum object size in the zone.
	 * If two objects are away from more than this threshold, they have to be part of two distinct objetcs. */
	static final double MAX_OBJECT_SIZE = 0.3;
	
	// private default constructor.
	private Rover() {
		this.logger = new Logger();
		this.mode = new RoverMode();
		
		this.ultra  = new UltraEyes(SensorPort.S4);
		this.color  = new ColorEye(SensorPort.S1);
		this.pliers = new Grabber(MotorPort.A);
		this.right  = new Engine(MotorPort.B);
		this.left   = new Engine(MotorPort.C);
		
		this.nav = new Navigator(MapZone.initial_pose, this.right, this.left);
	}
	// private constructor with parameters.
	private Rover(Port ultrasonic_port, Port color_port,
			      Port pliers_motor_port, Port right_motor_port, Port left_motor_port) {
		this.logger = new Logger();
		this.mode = new RoverMode();
		
		this.ultra  = new UltraEyes(ultrasonic_port);
		this.color  = new ColorEye(color_port);
		this.pliers = new Grabber(pliers_motor_port);
		this.right  = new Engine(right_motor_port);
		this.left   = new Engine(left_motor_port);
		
		this.nav = new Navigator(MapZone.initial_pose, this.right, this.left);
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
		this.logger.println(" with " + bv + " mV");
		
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
		if (this.ultra.connect()) 
				{ Beeper.beep();     this.logger.println("con. us: ok"); }
		else 	{ Beeper.twoBeeps(); this.logger.println("con. us: ko (" + this.ultra.port.getName() + ")");
		          error +=  1; }
		if (this.color.connect()) 
				{ Beeper.beep();     this.logger.println("con. cs: ok"); }
		else 	{ Beeper.twoBeeps(); this.logger.println("con. cs: ko (" + this.color.port.getName() + ")");
		          error +=  2; }
		if (this.pliers.connect()) 
				{ Beeper.beep();     this.logger.println("con. pm: ok"); }
		else 	{ Beeper.twoBeeps(); this.logger.println("con. pm: ko (" + this.pliers.motor.port.getName() + ")");
		          error +=  4; }
		if (this.right.connect()) 
				{ Beeper.beep();     this.logger.println("con. rm: ok"); }
		else 	{ Beeper.twoBeeps(); this.logger.println("con. rm: ko (" + this.right.port.getName() + ")");
		          error +=  8; }
		if (this.left.connect()) 
				{ Beeper.beep();     this.logger.println("con. lm: ok"); }
		else 	{ Beeper.twoBeeps(); this.logger.println("con. lm: ko (" + this.left.port.getName() + ")");
	              error += 16; }
		
		// diagnostic is now done.
		this.mode.stop();
		this.logger.println("ending diagnostic mode");
		
		// if an error occurred, 'error' is non zero.
		if (error != 0) { this.error(); }	
	}
	
	public static Point convertPose(boolean relative,Point p,Pose rover_pose) {
		if(relative) {
			p.x = (p.x-rover_pose.getX());
			p.y = (p.y-rover_pose.getY());
		}
		return p;
	}
	
	Point[] scan() {
		float angles[] = new float[19];
		float dists[] = new float[19];
		
		for (int i = 0; i < angles.length; i++) {
			angles[i] = 10*i - 90;
			this.nav.rotateTo(angles[i]);
			dists[i] = this.ultra.read().getValue();
//			dists[i] = (float)Math.sqrt(
//				dists[i]*dists[i] + Rover.ULTRA_R2 - 2*dists[i]*Rover.ULTRA_R2*Math.cos(Rover.ULTRA_THETA));
			System.out.println(dists[i]);
		}
		this.logger.println(Arrays.toString(angles));
		this.logger.println(Arrays.toString(dists));
		
		Point obstacles[] = new Point[19];
		for (int i = 0; i < obstacles.length; i++) {
			obstacles[i] = new Point(Float.MAX_VALUE, Float.MAX_VALUE);
		}
		int j = 0;
		boolean obst = false;
		
		for (int i = 0; i < angles.length; i++) {
			String msg = "d["+i+"]="+dists[i]+": ";
			if (dists[i] < Double.MAX_VALUE) {
				msg.concat("finite");
				obst = true;
				if (dists[i] < this.nav.getPose().getLocation().subtract(obstacles[j]).length()) {
					msg.concat(", enough close");
					Point detected_point = this.nav.getPose().getLocation().pointAt(dists[i], angles[i]);
//					detected_point = this.nav.getPose().getLocation().
//							pointAt(Rover.ULTRA_R, this.nav.getPose().getHeading()+Rover.ULTRA_THETA).
//							pointAt(dists[i], this.nav.getPose().getHeading());
					if(Rover.map.inside(detected_point) && !Rover.recup_zone.inside(detected_point)) {
						msg.concat(", inside the zone");
						if (detected_point.subtract(obstacles[j]).length() > Rover.MAX_OBJECT_SIZE) {
							j++;
							msg.concat(", too big -> next obstacle "+j);
						} else {
							msg.concat(", good size -> update on "+j);
						}
						obstacles[j] = detected_point;
					} else {
						msg.concat(", outside the zone -> no update on "+j);
					}
				} else {
					msg.concat(", not enough close -> no update on "+j);
				}
			} else {
				msg.concat("infinite");
				if (obst) {
					msg.concat(", end of obstacle -> next obstacle "+j);
					j++;
					obst = false;
				} else {
					msg.concat("not in obstacle -> no update on "+j);
				}
			}
			this.logger.println(msg);
		}
		
		this.logger.println(Arrays.deepToString(obstacles));
		Point result[] = new Point[(obst)? j+1:j];
		for (int i = 0; i < result.length; i++) {
			result[i] = obstacles[i];
		}
		return result;
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
		
		this.pliers.grab();

		Point[] res = this.scan();
		this.logger.println("res: " + Arrays.deepToString(res));
		
		for (int i = 0; i < res.length; i++) {
			Beeper.beep(3, 50);
			Button.waitForAnyPress();
			this.nav.goTo(res[i]);
		}

		this.logger.println("ending exploration mode");
		this.mode.stop();
	}
	/**
	 *  _____________________________________________TODO_____________________________________________.
	 */
	public void harvest() {
		this.logger.println("starting harvest mode");
		this.mode.enter_harvest_mode();
		
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
			dist = this.ultra.read().getValue();
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
			id = this.color.read().getValue();
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
		this.right.device.resetTachoCount();
		this.left.device.resetTachoCount();
		
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
		this.logger.println("rotating right..."); this.logger.println(this.right.device.getTachoCount());
		this.right.write(new Order(90, 360));  
		while (this.right.device.isMoving()) {
			this.logger.println(this.right.device.getTachoCount() + "(" + this.right.device.getRotationSpeed() + ")");
		}
		this.logger.println("done");
		Blinker.blink(Blinker.GREEN, Blinker.SLOW, 0);  Button.waitForAnyPress(); Beeper.beep();

		// left track
		this.logger.println("rotating left..."); this.logger.println(this.left.device.getTachoCount());
		this.left.write(new Order(90, 360));  
		while (this.left.device.isMoving()) {
			this.logger.println(this.left.device.getTachoCount() + "(" + this.left.device.getRotationSpeed() + ")");
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
		Pose pose = this.nav.getPose();
		this.logger.println("("+pose.getX()+","+pose.getY()+") at "+pose.getHeading() + " & " + this.right.device.getTachoCount());
		
		this.logger.println("goTo");
		this.nav.goTo(new Waypoint(pose.pointAt(100, pose.getHeading()+90)));
		
		this.logger.println("travel");
		this.nav.forward();
		this.nav.travel(10);
		this.logger.println(this.nav.getPose().toString() + ", " + this.right.device.getTachoCount());
	}
	
	public void test_navigator_square_antoine() {
		for (int i = 0; i < 4; i++) {
			this.nav.travel(20);
			this.nav.rotate(90);
		}
		
		Point waypoints[] = new Point[4];
		waypoints[0] = MapZone.initial_pose.getLocation();
		Point dir = new Point(20, 0);
		for (int i = 1; i < waypoints.length; i++) {
			waypoints[i] = waypoints[i-1].add(dir);
			dir = dir.leftOrth();
		}
		
		for (int i = 0; i < 4; i++) {
			this.nav.goTo(waypoints[i]);
			this.nav.rotate(90);
		}
	}

	public void test_navigator_sweep_antoine() {
		this.logger.println("pose before: "+this.nav.getPose().toString());
		this.nav.travel(40, true);
		this.nav.getLeft().device.resetTachoCount();
		this.nav.getRight().device.resetTachoCount();
		while (this.nav.isMoving()) {
			System.out.println(this.ultra.read().getValue());
			Beeper.beep();
		}
		int l_tacho = this.nav.getLeft().device.getTachoCount();
		int r_tacho = this.nav.getRight().device.getTachoCount();
		float dist = Rover.WHEEL_RADIUS*(l_tacho+r_tacho)/2;
		this.nav.add_dist(dist);
		this.logger.println("pose after: "+this.nav.getPose().toString());
	}
}
