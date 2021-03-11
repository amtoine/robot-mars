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
	
	/** The width of the ultrasonic sensor's cone. */
	static final int  x      = 170;
	/**	The length of the travels during calibration time, in mm. */
	static final int  search_length = 500;
	/** A margin all around the zone to avoid going out, in mm. */
	static final int  margin = 50;
	/** A closed path of points on the zone. */
	static final Pose path[] = new Pose[1500/x];
	
	/**	The length of one side of the landing zone. */
	static final int land_zone_side = 500;
	
	
	/** The diameter of the wheels, expressed in mm. */
	static final float WHEEL_DIAMETER = 55;
	/** The radius of the wheels, expressed in mm. */
	static final float WHEEL_RADIUS   = Rover.WHEEL_DIAMETER/2;
	/** The distance between the two axis of the wheels, expressed in mm. */
	static final float AXIS_DIFF      = 107;
	/** The half distance between the two axis of the wheels, expressed in mm. */
	static final float HALF_AXIS_DIFF = Rover.AXIS_DIFF/2;
	/** As the battery is full with 9000mV, we assume that the situation is critical below 10%, i.e. 900mV*/
	private static final int VOLTAGE_THRESHOLD = 900;
	
	// position of the ultrasonic sensor w.r.t. the center of rotation of the rover.
	static final int   ULTRA_Dx    = 0;
	static final int   ULTRA_Dy    = 0;
	static final float ULTRA_R2    = ULTRA_Dx*ULTRA_Dx + ULTRA_Dy*ULTRA_Dy;
	static final float ULTRA_R     = (float)Math.sqrt(ULTRA_R2);
	static final float ULTRA_THETA = (float)Math.atan2(ULTRA_Dy, ULTRA_Dx);
	
	// position of the pliers w.r.t. the center of rotation of the rover.
	static final int   PLIERS_Dx    = 0;
	static final int   PLIERS_Dy    = 0;
	static final float PLIERS_R2    = PLIERS_Dx*PLIERS_Dx + PLIERS_Dy*PLIERS_Dy;
	static final float PLIERS_R     = (float)Math.sqrt(PLIERS_R2);
	static final float PLIERS_THETA = (float)Math.atan2(PLIERS_Dy, PLIERS_Dx);
	
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
		this.mode   = new RoverMode();
		
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
		this.mode   = new RoverMode();
		
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
	
	/**
	 * Calibrate the position of the rover in the zone by looking for the borders of the black square.
	 * we assume that the X-axis is aligned with the length of the zone.
	 */
	public void calibrate_origin() {
		float color; // the color under the sensor.
		
		// first the rover moves along X-axis and waits for white floor.
		this.nav.setup_travel(Rover.search_length);
		color = -1;
		while (this.nav.isMoving() && color < ColorEye.threshold) { // white is > 0.5 and black is < 0.5
			color = this.color.read().getValue(); // update the color value.
			Thread.yield();
		}
		if (color < 0.5) { this.error(); } else { Beeper.beep(); } // color stays below 0.5 if something fatal occurred.
		
		this.nav.getPose().setLocation(0.5f, this.nav.getPose().getY()); // update the x coordinate.
		this.nav.travel(-Rover.margin); // give a little margin to search along y axis.
		
		this.nav.rotate(90); // rotate left to search white along the y axis.
		
		// same remarks
		this.nav.setup_travel(Rover.search_length);
		color = -1;
		while (this.nav.isMoving() && color < ColorEye.threshold) {
			color = this.color.read().getValue();
			Thread.yield();
		}
		if (color == -1) { this.error(); } else { Beeper.beep(); }
		
		this.nav.getPose().setLocation(this.nav.getPose().getX(), 0.5f); // update the y coordinate.
		
		// location should be accurate.
	}
	
//	public static Point convertPose(boolean relative,Point p,Pose rover_pose) {
//		if(relative) {
//			p.x = (p.x-rover_pose.getX());
//			p.y = (p.y-rover_pose.getY());
//		}
//		return p;
//	}
	
	/**
	 * 
	 * @return an array of detected obstacles
	 * @deprecated localisation process changed.
	 */
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
					Point detected_point = this.point_from_ultra(dists[i]); // compute location.
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

	public void compute_path() {
		for (int i = 0; i < Rover.path.length; i++) {
			float x = ((i%4 == 0) || (i%4 == 3))? Rover.x : Map.length*1000-Rover.x;
			float y = Rover.x+2*Rover.x*(int)(i/2);
			float angle = (i%2 == 1)? -90 : ((i%4 == 0)? 0 : -180);
			path[i] = new Pose(x/1000, y/1000, angle);
			this.logger.println("p["+i+"]: "+path[i]);
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
		
		// align with first checkpoint.
		//	compute the direction from current position to the first checkpoint.
		Point direction = Rover.path[0].getLocation().subtract(this.nav.getPose().getLocation());
		// the angle of rotation is equal to the angle of the vector 'direction', modulus pi/2.
		int angle = (int) (180/Math.PI * direction.angle());
		this.nav.rotate(this.nav.getPose().getHeading() - angle);
		this.nav.travel(direction.length()/1000);
		
		this.logger.println("X:" +	this.nav.getPose().getX() + " " + 
							"Y:" +	this.nav.getPose().getY() + " " +
							"H:" + 	this.nav.getPose().getHeading());
		
//		this.nav.rotate(90);
//		this.nav.travel(Rover.land_zone_side-Rover.path[0].getX()-Rover.margin);
//		this.nav.rotate(-90);
//		this.nav.travel(Rover.land_zone_side-Rover.path[0].getY());
//		this.nav.rotate(-90);
		
		float d;
		Point detected_obj;
		
		Point obstacles[] = new Point[19];
		int visits[] = new int[19];
		for (int i = 0; i < obstacles.length; i++) {
//			obstacles[i] = new Point(Float.MAX_VALUE, Float.MAX_VALUE);
			obstacles[i] = new Point(0, 0); // initialization for the incremental mean computations.
		}
		int j = 0;
		
		for (int i = 1; i < Rover.path.length; i++) {
			this.nav.setup_travel((i%2 == 0)? Map.length*1000-2*Rover.x : 2*Rover.x); // start to move towards next point.
			while (this.nav.isMoving()) {
				d = this.ultra.read().getValue(); // look at obstacles.
				if (d < Double.MAX_VALUE) {
					// there is something...
					detected_obj = this.point_from_ultra(d); // compute location.
					if (Rover.map.inside(detected_obj) && !Rover.recup_zone.inside(detected_obj)) {
						// ...inside the map.
						this.logger.println("d: " + d);
						this.logger.println("det (X:" +	detected_obj.getX() + " Y:" +	detected_obj.getY() + ")");
						visits[j]++;
						// compute incremental mean of the detected object location with previous obstacle.
						obstacles[j] = obstacles[j].multiply(visits[j]-1).add(detected_obj).multiply(1/visits[j]);
					}
				}
			}
			this.nav.compute_new_location();

			this.logger.println("pose travel: " +	this.nav.getPose().getX() + ", " +
			                                      	this.nav.getPose().getY() + ", " +
			                                      	this.nav.getPose().getHeading());
			this.nav.setup_rotate(((i%4 == 1) || (i%4 == 2))? -90 : 90);
			while (this.nav.isMoving()) {
				d = this.ultra.read().getValue();
				if (d < Double.MAX_VALUE) {
					detected_obj = this.nav.getPose().pointAt(d, this.nav.getPose().getHeading());
					if (Rover.map.inside(detected_obj) && !Rover.recup_zone.inside(detected_obj)) {
						this.logger.println("d: " + d);
						this.logger.println("det (X:" +	detected_obj.getX() + " Y:" +	detected_obj.getY() + ")");
					}
				}
			}
			this.nav.compute_new_heading();
			this.logger.println("pose rotate: " + 	this.nav.getPose().getX() + ", " +
                    								this.nav.getPose().getY() + ", " +
                    								this.nav.getPose().getHeading());
		}

//		Point[] res = this.scan();
//		this.logger.println("res: " + Arrays.deepToString(res));
//		
//		for (int i = 0; i < res.length; i++) {
//			Beeper.beep(3, 50);
//			Button.waitForAnyPress();
//			this.nav.goTo(res[i]);
//		}

		this.logger.println("ending exploration mode");
		this.mode.stop();
	}
	/**
	 *  _____________________________________________TODO_____________________________________________.
	 */
	public void harvest(Point sample) {
		this.logger.println("starting harvest mode");
		this.mode.enter_harvest_mode();
		
		float factor = 0.9f;
		boolean approach = true;
		float distance = this.nav.getPose().getLocation().subtract(sample).length();

		// an array containing a set of relative angles to check where the sample is 
		int check_precision = 10;
		int nb_check_on_one_side = 2;
		int check_relative_angles[] = new int[2*nb_check_on_one_side];
		for (int i = 0; i < check_relative_angles.length; i++) {
			if 		(i < nb_check_on_one_side) { 	check_relative_angles[i] = -check_precision; }
			else if (i == nb_check_on_one_side) { 	check_relative_angles[i] = (nb_check_on_one_side+1)*check_precision; }
			else {									check_relative_angles[i] = check_precision; }
		}
		// e.g. with precision of 10 and 3 checks on each side, result is [-10, -10, -10, 40, 10, 10]
		// i.e. three -10 rotations to explore right, then +40 to compensate and begin exploration on the left with the two
		// other +10 rotations.
		
		while (approach) {
			if (distance >= Double.MAX_VALUE) {
				// rover lost the sample.
				// it could be a bit to the right or a bit to the left, let's check both.
				Point check_obj;
				boolean found_back = false;
				for (int i = 0; i < check_relative_angles.length; i++) {
					this.nav.rotate(check_relative_angles[i]); // rotate to the current checking angle.
					distance = this.nav.getPose().getLocation().subtract(sample).length(); // get the distance.
					if (distance < Double.MAX_VALUE) { // there is something...
						check_obj = this.point_from_ultra(distance); // compute location.
						if (Rover.map.inside(check_obj) && !Rover.recup_zone.inside(check_obj)) {
							// ...inside the zone.
							found_back = true;
							break;
						}
					}
				}
				if (!found_back) {
					// either sample is lost...
					// or it is too close to be detected !
					// let's suppose it is always that the sample is too close.
					approach = false;
					// get rover back to its starting heading by default.
					this.nav.rotate(-check_relative_angles[nb_check_on_one_side]+check_precision);
					// because check_relative_angles[nb_check_on_one_side] is, in the example above, the +40, so that
					// -check_relative_angles[nb_check_on_one_side]+check_precision is -30 which is exactly what is needed
					// to go back to starting heading.
				}
			}
			this.nav.travel(factor*distance); // travel 90% of the distance to the sample.
			distance = this.nav.getPose().getLocation().subtract(sample).length(); // get the new distance to the sample.
		}
		
		
		
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

	//###################################################################################################################
	//### Setters and Getters ###########################################################################################
	//###################################################################################################################
	
	//###################################################################################################################
	//### Take Measures #################################################################################################
	//###################################################################################################################
	
	//###################################################################################################################
	//### Tools #########################################################################################################
	//###################################################################################################################
	private Point point_from_ultra(float distance) {
		return this.nav.getPose().pointAt(distance, this.nav.getPose().getHeading());
//		return this.nav.getPose().getLocation().
//					pointAt(Rover.ULTRA_R, this.nav.getPose().getHeading()+Rover.ULTRA_THETA).
//					pointAt(distance, this.nav.getPose().getHeading()); // compute location.
	}
	//###################################################################################################################
	//###################################################################################################################
	//### Tests #########################################################################################################
	//###################################################################################################################
	//###################################################################################################################
	
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
	//### Navigator tests ###############################################################################################
	//###################################################################################################################
	/** */
	public void test_navigator() {
		Pose pose = this.nav.getPose();
		this.logger.println("pose before: " +	pose.getX() + ", " +
												pose.getY() + ", " +
												this.right.device.getTachoCount());
		
		this.logger.println("goTo");
		this.nav.goTo(new Waypoint(pose.pointAt(100, pose.getHeading()+90)));
		
		this.logger.println("travel");
		this.nav.forward();
		this.nav.travel(10);
		this.logger.println(this.nav.getPose().toString() + ", " + this.right.device.getTachoCount());
	}
	
	public void test_navigator_square_antoine() {
		for (int i = 0; i < 0; i++) {
			this.logger.println("travel");
			this.nav.travel(200);
			this.logger.println("rotate");
			this.nav.rotate(90);
		}
		
		this.logger.println("pose: " + this.nav.getPose());
		Button.waitForAnyPress();
		
		Point waypoints[] = new Point[4];
		waypoints[0] = MapZone.initial_pose.getLocation();
		Point dir = new Point(200, 0);
		for (int i = 1; i < waypoints.length; i++) {
			waypoints[i] = waypoints[i-1].add(dir);
			dir = dir.leftOrth();
		}
		
		for (int i = 0; i < 4; i++) {
			this.logger.println(waypoints[i].toString());
			this.nav.goTo(waypoints[i]);
			this.nav.rotate(90);
		}
	}

	public void test_navigator_sweep_antoine() {
		this.logger.println("pose before: " +	this.nav.getPose().getX() + ", " +
              									this.nav.getPose().getY() + ", " +
              									this.nav.getPose().getHeading());
		Button.waitForAnyPress();
		this.nav.setup_travel(200);
		while (this.nav.isMoving()) {
			Beeper.beep();
			System.out.println(this.ultra.read().getValue());
		}
		this.nav.compute_new_location();
		this.logger.println("pose travel: " +	this.nav.getPose().getX() + ", " +
          										this.nav.getPose().getY() + ", " +
          										this.nav.getPose().getHeading());
		Button.waitForAnyPress();
		
		this.logger.println("pose before: "+this.nav.getPose().toString());
		this.nav.setup_rotate(90);
		while (this.nav.isMoving()) {
			System.out.println(this.ultra.read().getValue());
			Beeper.beep();
		}
		this.nav.compute_new_heading();
		this.logger.println("pose travel: " +	this.nav.getPose().getX() + ", " +
              									this.nav.getPose().getY() + ", " +
              									this.nav.getPose().getHeading());
		Button.waitForAnyPress();
	}
	
	public void test_travel_antoine() {
		while (Button.readButtons() != Button.ID_ENTER) {
			this.nav.travel(500);
			Button.waitForAnyPress();
		}
	}
	
	public void test_rotate_antoine() {
		
		while (Button.readButtons() != Button.ID_ENTER) {
			this.nav.rotate(90);
			Button.waitForAnyPress();
		}
	}
}
