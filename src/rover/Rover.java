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
	static final float  x             = 170													/1000f;
	/**	The length of the travels during calibration time, in m. */
	static final float  search_length = 500													/1000f;
	/** A margin all around the zone to avoid going out, in m. */
	static final float  margin        = 50													/1000f;
	/** A path of points on the zone. */
	static final Pose path[] = new Pose[(int)(2 * 1500/(x*1000))];
	
	/** A list of obstacles detected */
	Point[] obstacles = new Point[20];
	/** Index of the last treated waypoint in exploration mode*/
	int current_wp;
	/** A list of the number of times an obstacle was detected*/
	int visits[];
	/** Index of last detected obstacle*/
	int j_obst;
	
	/**	The length of one side of the landing zone. */
	static final float land_zone_side = 500													/1000f;
	
	
	/** The diameter of the wheels, expressed in mm. */
	static final float WHEEL_DIAMETER = 55													/1000f;
	/** The radius of the wheels, expressed in mm. */
	static final float WHEEL_RADIUS   = Rover.WHEEL_DIAMETER/2;
	/** The distance between the two axis of the wheels, expressed in mm. */
	static final float AXIS_DIFF      = 107													/1000f;
	/** The half distance between the two axis of the wheels, expressed in mm. */
	static final float HALF_AXIS_DIFF = Rover.AXIS_DIFF/2;
	/** As the battery is full with 9000mV, we assume that the situation is critical below 10%, i.e. 900mV*/
	private static final int VOLTAGE_THRESHOLD = 900;
	
	// position of the ultrasonic sensor w.r.t. the center of rotation of the rover.
	static final float ULTRA_Dx    = 126													/1000f;
	static final float ULTRA_Dy    = 47														/1000f;
	static final float ULTRA_R2    = ULTRA_Dx*ULTRA_Dx + ULTRA_Dy*ULTRA_Dy;
	static final float ULTRA_R     = (float)Math.sqrt(ULTRA_R2);
	static final float ULTRA_THETA = (float)Math.atan2(ULTRA_Dy, ULTRA_Dx);
	static final float MIN_DIST_DETECTION = 200												/1000f;
	
	// position of the pliers w.r.t. the center of rotation of the rover.
	static final float PLIERS_Dx    = 135													/1000f;
	
	/** A map of the whole intervention zone. */
	static final MapZone map = new Map();
	/** A map of the recovery zone which is a subset of the intervention zone. */
	static final RecupZone recup_zone = new RecupZone();
	/** The maximum object size in the zone.
	 * If two objects are away from more than this threshold, they have to be part of two distinct objects. */
	static final float MAX_OBJECT_SIZE = 300												/1000f;
	
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
	 * Initialize the obstacle detection.
	 */
	public void init_obstacle_detection() {
		this.obstacles = new Point[20];
		for (int i = 0; i < this.obstacles.length; i++) {
			this.obstacles[i] = new Point(0, 0); // initialization for the incremental mean computations.
		}
		this.visits = new int[20];
		this.current_wp = 0;
		this.j_obst = 0;
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
//		if (this.color.connect()) 
//				{ Beeper.beep();     this.logger.println("con. cs: ok"); }
//		else 	{ Beeper.twoBeeps(); this.logger.println("con. cs: ko (" + this.color.port.getName() + ")");
//		          error +=  2; }
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
	public Point explore() {
		this.logger.println("starting exploration mode");
		this.mode.enter_exploration_mode();
		
		// Current pose of the rover before starting exploration
		this.logger.println("current pose: " +	this.nav.getPose().getX() + ", " +
												this.nav.getPose().getY() + ", " +
												this.nav.getPose().getHeading());
		
		Point direction;
		int angle;
		float d;
		Point detected_obj;
		
		boolean harvest_needed = false;
		
		while (this.current_wp < Rover.path.length && !harvest_needed) {
			// compute the direction from current position to the next checkpoint
			direction = Rover.path[this.current_wp].getLocation().subtract(this.nav.getPose().getLocation());
			// the angle of rotation is equal to the angle of the vector 'direction', modulus the current heading.
			angle = (int) (180/Math.PI * direction.angle());
			
			// Rotating in direction of next checkpoint while scanning
			this.nav.setup_rotate(this.nav.getPose().getHeading() - angle);
			while (this.nav.isMoving()) {
				d = this.ultra.read().getValue(); //scanning for obstacles
				if (d < Double.MAX_VALUE) {
					detected_obj = this.point_from_ultra(d);
					if (Rover.map.inside(detected_obj) && !Rover.recup_zone.inside(detected_obj)) {
						if (this.is_new_sample(detected_obj)) {
							this.logger.println("d: " + d);
							this.logger.println("det (X:" +	detected_obj.getX() + " Y:" +	detected_obj.getY() + ")");
							
							this.j_obst++;
							this.nav.setup_travel(0);
							this.nav.compute_new_location();
							return detected_obj;
						}
					}
				}
			}
			this.nav.compute_new_heading();
			this.logger.println("rotated pose: " +	this.nav.getPose().getX() + ", " +
													this.nav.getPose().getY() + ", " +
													this.nav.getPose().getHeading());
			
			// Traveling to the next checkpoint while scanning
			this.nav.setup_travel(direction.length());
			while (this.nav.isMoving()) {
				d = this.ultra.read().getValue(); // scanning for obstacles
				if (d < Double.MAX_VALUE) {
					// there is something...
					detected_obj = this.point_from_ultra(d); // compute location.
					if (Rover.map.inside(detected_obj) && !Rover.recup_zone.inside(detected_obj)) {
						if (this.is_new_sample(detected_obj)) {
							// ...inside the map.
							this.logger.println("d: " + d);
							this.logger.println("det (X:" +	detected_obj.getX() + " Y:" +	detected_obj.getY() + ")");
							this.j_obst++;
							this.nav.setup_travel(0);
							this.nav.compute_new_location();
							return detected_obj;
						}
					}
				}
			}
			this.nav.compute_new_location();
			this.logger.println("travelled pose: " +	this.nav.getPose().getX() + ", " +
														this.nav.getPose().getY() + ", " +
														this.nav.getPose().getHeading());
			
			this.current_wp++;
			
		}

		this.logger.println("ending exploration mode");
		this.mode.stop();
		this.j_obst = 2;
		return null;
	}
	/**
	 *  _____________________________________________TODO_____________________________________________.
	 */
	public void harvest(Point sample) {
		this.logger.println("starting harvest mode");
		this.mode.enter_harvest_mode();
		
		float factor = 0.5f;
		boolean approach = (sample == null)? false:true;
		if (approach) {
			// make sure the pliers are open.
			this.pliers.release();
			
			float distance = this.nav.getPose().getLocation().subtract(sample).length();
			float prev_distance = distance;
	
			// an array containing a set of relative angles to check where the sample is 
			int check_precision = 10;
			int nb_check_each_side = 2;
			int check_relative_angles[] = new int[2*nb_check_each_side];
			for (int i = 0; i < check_relative_angles.length; i++) {
				if 		(i  < nb_check_each_side) { 	check_relative_angles[i] = -check_precision; }
				else if (i == nb_check_each_side) { 	check_relative_angles[i] = (nb_check_each_side+1)*check_precision; }
				else {									check_relative_angles[i] = check_precision; }
			}
			// e.g. with precision of 10 and 3 checks on each side, result is [-10, -10, -10, 40, 10, 10]
			// i.e. three -10 rotations to explore right, then +40 to compensate and begin exploration on the left with the 
			// two other +10 rotations.
			
			while (approach) {
				if (distance >= Float.MAX_VALUE) {
					// rover lost the sample.
					// it could be a bit to the right or a bit to the left, let's check both.
					Point check_obj;
					boolean found_back = false;
					for (int i = 0; i < check_relative_angles.length; i++) {
						this.nav.rotate(check_relative_angles[i]); // rotate to the current checking angle.
						distance = this.ultra.read().getValue(); // get the distance.
						if (distance < Float.MAX_VALUE) { // there is something...
							check_obj = this.point_from_ultra(distance); // compute location.
							if (Rover.map.inside(check_obj) && !Rover.recup_zone.inside(check_obj)) {
								if (this.is_new_sample(check_obj)) {
									this.logger.println("check ("+distance+"): " +	check_obj.getX() + ", " +
											check_obj.getY());
									this.logger.println(" ("+	this.nav.getPose().getX() + ", " +
											this.nav.getPose().getY() + ")");
									// ...inside the zone.
									found_back = true;
									break;
								}
							}
						}
					}
					if (!found_back) {
						// either sample is lost...
						// or it is too close to be detected !
						// let's suppose it is always that the sample is too close.
						approach = false;
						// by default, get rover back to its starting heading.
						this.nav.rotate(-check_relative_angles[nb_check_each_side]+check_precision);
						// because check_relative_angles[nb_check_each_side] is, in the example above, the +40, so that
						// -check_relative_angles[nb_check_each_side]+check_precision is -30 which is exactly what is
						// needed to go back to starting heading.
	
						// it is possible to detect something outside the zone, but we do not want to take it into account.
						distance = Float.MAX_VALUE;
					}
				}
				// not an else because distance could have changed inside previous if statement.
				if (distance < Double.MAX_VALUE) {	
					Point check_obj = this.point_from_ultra(distance); // compute location.
					if (Rover.map.inside(check_obj) && !Rover.recup_zone.inside(check_obj)) {
						if (this.is_new_sample(check_obj)) {
							this.logger.println("check ("+distance+"): " +	check_obj.getX() + ", " +
									check_obj.getY());
							this.logger.println(" ("+	this.nav.getPose().getX() + ", " +
									this.nav.getPose().getY() + ")");
							prev_distance = distance; // backup of the distance.
							this.nav.travel(factor*distance); // travel 90% of the distance to the sample.
							distance = this.ultra.read().getValue(); // new distance to the sample.
						}
					}	
				}
				if (distance < Rover.MIN_DIST_DETECTION) {
					approach = false;
				}
			}
			
			// now, the rover is just in front the sample, or its ghost...
			// let us correct the distance to the sample.
			distance = factor*prev_distance;
			Point sample_to_grab = this.point_from_ultra(distance);
			this.j_obst = 1;
			this.obstacles[this.j_obst-1] = sample_to_grab;
			// rotate by the angle between the axis of the rover and the vector pointing towards the sample.
			this.nav.rotate(Math.atan2(Rover.ULTRA_Dy, Rover.ULTRA_Dx+distance));
			
			// approach the sample.
			this.nav.travel(sample_to_grab.subtract(
					this.nav.getPose().getLocation().pointAt(
							Rover.PLIERS_Dx, this.nav.getPose().getHeading())).length());
			// grab the sample.
			this.pliers.grab();
			
			// now that the rover has the sample in its pliers, simply go to the sample zone.
			// point towards the recovery zone.
			Point direction = RecupZone.center.subtract(
					this.nav.getPose().getLocation().pointAt(
							Rover.PLIERS_Dx, this.nav.getPose().getHeading()));
			this.nav.rotate(-(direction.angle()*180/Math.PI - this.nav.getPose().getHeading()));
			// travel the distance.
			this.nav.travel(direction.length() - RecupZone.diameter);
			// release the sample.
			this.pliers.release();
			
			this.nav.travel(-50 /1000f);
			
			this.pliers.grab(); // go to previous pliers state.
		} else {
			this.logger.println("no sample to fetch");
		}
		
		this.logger.println("ending harvest mode");
		this.mode.stop();	
	}
	 /**
	  *_____________________________________________TODO_____________________________________________ (blocking method).
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
	//### Tools #########################################################################################################
	//###################################################################################################################
	/**
	 * Computes the position of an obstacle detected by the ultrasonic sensor.
	 * As the sensor is not exactly at the center of rotation of the rover, its position w.r.t. to this center of rotation
	 * has to be taken into account to have an accurate position for the obstacle.
	 * 
	 * @param distance the distance from the sensor to the object, assuming that the sensor is aligned with the axis of
	 * the rover
	 * @return the obstacle position, with ultrasonic correction.
	 */
	private Point point_from_ultra(float distance) {
		return this.nav.getPose().pointAt(distance, this.nav.getPose().getHeading());
//		return this.nav.getPose().getLocation().
//					pointAt(Rover.ULTRA_R, this.nav.getPose().getHeading()+Rover.ULTRA_THETA).
//					pointAt(distance, this.nav.getPose().getHeading()); // compute location.
	}
	
//	public static Point convertPose(boolean relative,Point p,Pose rover_pose) {
//		if(relative) {
//			p.x = (p.x-rover_pose.getX());
//			p.y = (p.y-rover_pose.getY());
//		}
//		return p;
//	}

	public void compute_path() {
		for (int i = 0; i < Rover.path.length; i++) {
			float x = ((i%4 == 0) || (i%4 == 3))? Rover.x : Map.length*1000-Rover.x;
			float y = Rover.x+2*Rover.x*(int)(i/2);
			float angle = (i%2 == 1)? -90 : ((i%4 == 0)? 0 : -180);
			path[i] = new Pose(x/1000f, y/1000f, angle);
			this.logger.println("p["+i+"]: "+path[i]);
		}
	}
	
	/**
	 * Tells whether a mission is done or not.
	 * A mission consists of fetching two samples in the zone under 7 minutes. Mission is done when two samples have been
	 * fetched.
	 * 
	 * @return true if the mission is done, i.e. two samples have been fetched, false otherwise.
	 */
	public boolean mission_done() {
//>>>>>>>>>>>>> ANTOINE
		return this.j_obst == 2;
//>>>>>>>>>>>>>
//		if(this.j_obst >= 1){
//			// Mission finished so returns to starting zone
//			Point direction = new Point(0.25f,0.75f).subtract(this.nav.getPose().getLocation());
//			int angle = (int) (180/Math.PI * direction.angle());
//			this.nav.rotate(this.nav.getPose().getHeading() - angle);
//			this.nav.setup_travel(direction.length());
//			return true;
//		} else {
//			return false;
//		}
//>>>>>>>>>>>>> CLAIRE
	}
	
	/**
	 * Tells whether a newly detected object is a new sample indeed.
	 * 
	 * @param detected_obj the newly detected object
	 * @return true if the detected object has never been seen before, false if known.
	 */
	private boolean is_new_sample(Point detected_obj) {
		boolean new_sample = true;
		for (int i = 0; i < j_obst; i++) {
			if (this.obstacles[i].subtract(detected_obj).length() < Rover.MIN_DIST_DETECTION) {
				new_sample = false;
				break;
			}
		}
		return new_sample;
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
		this.nav.travel(10 /1000f);
		this.logger.println(this.nav.getPose().toString() + ", " + this.right.device.getTachoCount());
	}
	
	public void test_navigator_square_antoine() {
		for (int i = 0; i < 0; i++) {
			this.logger.println("travel");
			this.nav.travel(200 /1000f);
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
		this.nav.setup_travel(200 /1000f);
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
			this.nav.travel(500 /1000f);
			Button.waitForAnyPress();
		}
	}
	
	public void test_rotate_antoine() {
		while (Button.readButtons() != Button.ID_ENTER) {
			this.logger.println("travel");
			this.nav.rotate(90);
			Button.waitForAnyPress();
		}
	}
	
	//###################################################################################################################
	//### Grabber tests #################################################################################################
	//###################################################################################################################
	public void test_grabber_antoine() {
		this.harvest(new Point(0.75f, 0.75f));
	}
}
