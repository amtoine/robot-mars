package rover;

import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;

/**
 * A navigator is the central control of the rover's movements.
 * It is a redefinition of the Navigator class from leJOS which appears to be behaving not as intented. Same methods will be
 * redefined here to adapt better to our setup and the current task we have been given.
 * 
 * @author Antoine Stevan
 *
 */
public class Navigator {
	/** The current position + heading of the rover. */
	private Pose pose;
	/** The right engine of the rover. */
	private Engine right;
	/** The left engine of the rover. */
	private Engine left;
	
	public Navigator(Pose initialPose, Engine right, Engine left) {
		this.pose = initialPose;
		this.right = right;
		this.left = left;
	}
	
	//######################################################################################################################
	//### Relative rotations. ##############################################################################################
	//######################################################################################################################
	/**
	 * Rotates the rover from a given angle, in degrees.
	 * During the process, the heading of the rover is updated.
	 * 
	 * @param angle the relative angle, in degrees.
	 */
	public void rotate(float angle) {
		// theta is the angle the wheels need to rotate to complete a macroscopic rotation of 'angle'.
		// a negative sign has to be introduced because it was more convenient to mount motors backwards.
		int theta = (int)(-angle * Rover.HALF_AXIS_DIFF/Rover.WHEEL_RADIUS);
		// both motors move together.
		this.right.device.rotate(theta, true);
		this.left.device.rotate(-theta, true);
		while (this.left.device.isMoving() || this.right.device.isMoving()) {
			Thread.yield();
		}
		// when rotation ends, need to add the angle to the current heading of the rover.
		// no - sign required because the motors do not affect the coordinate system, which is introduced in the main doc.
		this.pose.setHeading(this.pose.getHeading()+angle);
	}
	/**
	 * Wrapper of the {@link Navigator}{@link #rotate(float)} method.
	 * 
	 * @param angle the relative angle, in degrees.
	 */
	public void rotate(double angle) {
		this.rotate((float)angle);
	}
	/**
	 * Rotates the rover from a given angle, in degrees.
	 * During the process, the heading of the rover is updated.
	 * 
	 * @param angle the relative angle, in degrees.
	 * @param immediate_return if true, the execution continues, allowing the user to do other stuff during rotation.
	 * Otherwise, the program halts and waits for the rotation to end.
	 */
	public void rotate(float angle, boolean immediate_return) {
		// remarks are the same as above.
		int theta = (int)(-angle * Rover.HALF_AXIS_DIFF/Rover.WHEEL_RADIUS);
		this.right.device.rotate(theta, immediate_return);
		this.left.device.rotate(-theta, immediate_return);
		if (!immediate_return) {
			while (this.left.device.isMoving() || this.right.device.isMoving()) {
				Thread.yield();
			}
			this.pose.setHeading(this.pose.getHeading()+angle);
		}
		// else the method returns immediately. user have to take care of the pose of the rover,
		// e.g. by using Navigatorcompute_new_heading()
	}
	/**
	 * Wrapper of the {@link Navigator}{@link #rotateTo(float, boolean)} method.
	 * 
	 * @param angle the relative angle, in degrees.
	 * @param immediate_return if true, the execution continues, allowing the user to do other stuff during rotation.
	 * Otherwise, the program halts and waits for the rotation to end.
	 */
	public void rotate(double angle, boolean immediate_return) {
		this.rotate((float)angle, immediate_return);
	}
	/**
	 * Sets up a rotation for the rover.
	 * During the setup, a rotation with immediate return is launched and tacho counts are reset for future computations.
	 * After a rotation, setup here, one should consider using {@link Navigator#compute_new_heading()} to update the heading
	 * of the rover using tachometers.
	 * 
	 * @param angle the angle of rotation, in degrees.
	 */
	public void setup_rotate(int angle) {
		// reset both tacho counts.
		this.left.device.resetTachoCount();
		this.right.device.resetTachoCount();
		// launch rotation with immediate return
		this.rotate(angle, true);		
	}
	/**
	 * Wrapper of the {@link Navigator#setup_rotate(int)} method.
	 * 
	 * @param angle the angle of rotation, in degrees.
	 * @see Navigator#setup_rotate(int)
	 */
	public void setup_rotate(float angle) {
		this.setup_rotate((int)angle);
	}
	/**
	 * Computes a new location, assuming straight travel, from previous location, using tacho counts.
	 * 
	 * @see Navigator#setup_travel(int)
	 */
	public void compute_new_heading() {
		// get right and left tacho counts.
		int l_tacho = this.left.device.getTachoCount();
		int r_tacho = this.right.device.getTachoCount();
		// compute mean of both tachos, i.e.(l_tacho-r_tacho)/2, for more accuracy.
		// as wheel rotations are opposed to perform a rover rotation, there is a - sign in the mean.
		// and convert result to the angle of rotation using the formula between rover angle of rotation and motor angle of
		// rotation: angle*HALF_AXIS_DIFF = theta*WHEEL_RADIUS, which works both in radians and degrees.
		// so no need to convert to radians here.
		// no need to convert distances to m, they cancel each other.
		this.pose.setHeading(this.pose.getHeading()+(l_tacho-r_tacho)/2*Rover.WHEEL_RADIUS/Rover.HALF_AXIS_DIFF);		
	}
	
	//######################################################################################################################
	//### Absolute rotations. ##############################################################################################
	//######################################################################################################################
	/**
	 * Rotates the rover to a given angle, in degrees.
	 * During the process, the heading of the rover is updated.
	 * 
	 * @param angle the absolute angle, in degrees.
	 */
	public void rotateTo(float angle) {
		// remarks are the same as above, expect for theta.
		// here the angle of rotation is angle-heading, by following the 'after-before' rule, with an extra - sign to
		// compensate motors mounting, as explained above
		int theta = (int)((-angle+this.pose.getHeading()) * Rover.HALF_AXIS_DIFF/Rover.WHEEL_RADIUS);
		this.right.device.rotate(theta, true);
		this.left.device.rotate(-theta, true);
		while (this.left.device.isMoving() || this.right.device.isMoving()) {
			Thread.yield();
		}
		// here the new heading is directly the one given as argument.
		this.pose.setHeading(angle);
	}
	/**
	 * Rotates the rover to a given angle, in degrees.
	 * During the process, the heading of the rover is updated.
	 * 
	 * @param angle the absolute angle, in degrees.
	 * @param immediate_return if true, the execution continues, allowing the user to do other stuff during rotation.
	 * Otherwise, the program halts and waits for the rotation to end.
	 */
	public void rotateTo(float angle, boolean immediate_return) {
		// remarks are the same as above
		int theta = (int)((angle-this.pose.getHeading()) * Rover.HALF_AXIS_DIFF/Rover.WHEEL_RADIUS);
		this.right.device.rotate(theta, immediate_return);
		this.left.device.rotate(-theta, immediate_return);
		if (!immediate_return) {
			while (this.left.device.isMoving() || this.right.device.isMoving()) {
				Thread.yield();
			}
			this.pose.setHeading(angle);
		}
	}
	
	//######################################################################################################################
	//### Travels. #########################################################################################################
	//######################################################################################################################
	/**
	 * The rover travels a certain distance.
	 * During the process, the location of the rover is updated.
	 * 
	 * @param angle the relative distance, in mm.
	 */
	public void travel(float length) {
		// same - sign as explained above.
		// here the rotation of each motor has to be converted in degrees and uses the formula for the length of an arc.
		int theta = (int)(-length / Rover.WHEEL_RADIUS * 180 / Math.PI);
		this.right.device.rotate(theta, true);
		this.left.device.rotate( theta, true);
		while (this.left.device.isMoving() || this.right.device.isMoving()) {
			Thread.yield();
		}
		// the new location is 'length' mm away, in the direction of the current heading.
		this.pose.setLocation(this.pose.getLocation().pointAt(length/1000, this.pose.getHeading()));
	}
	/**
	 * The rover travels a certain distance.
	 * During the process, the location of the rover is updated.
	 * 
	 * @param angle the relative distance, in mm.
	 * @param immediate_return if true, the execution continues, allowing the user to do other stuff during tarvel.
	 * Otherwise, the program halts and waits for the travel to end.
	 */
	public void travel(float length, boolean immediate_return) {
		// same remarks as above.
		int theta = (int)(-length / Rover.WHEEL_RADIUS * 180 / Math.PI);
		this.right.device.rotate(theta, immediate_return);
		this.left.device.rotate( theta, immediate_return);
		if (!immediate_return) {
			while (this.left.device.isMoving() || this.right.device.isMoving()) {
				Thread.yield();
			}
			this.pose.setLocation(this.pose.getLocation().pointAt(length/1000, this.pose.getHeading()));
		}
	}
	/**
	 * Sets up a travel for the rover.
	 * During the setup, a travel with immediate return is launched and tacho counts are reset for future computations.
	 * After a travel, setup here, one should consider using {@link Navigator#compute_new_location()} to update the location
	 * of the rover using tachometers.
	 * 
	 * @param length the length of the travel, in mm.
	 */
	public void setup_travel(int length) {
		this.left.device.resetTachoCount();
		this.right.device.resetTachoCount();
		this.travel(length, true);
	}
	/**
	 * Wrapper of the {@link Navigator#setup_travel(int)} method.
	 * 
	 * @param length the length of the travel, in mm.
	 * @see Navigator#setup_travel(int)
	 */
	public void setup_travel(float length) {
		this.setup_travel((int)length);
	}
	/**
	 * Computes a new location, assuming straight travel, from previous location, using tacho counts.
	 * 
	 * @return the vector representing the travel, from previous location to the new one.
	 * 
	 * @see Navigator#setup_travel(int)
	 */
	public Point compute_new_location() {
		// get right and left tacho counts.
		int l_tacho = this.left.device.getTachoCount();
		int r_tacho = this.right.device.getTachoCount();
		// compute mean of both tachos, i.e.(l_tacho+r_tacho)/2, for more accuracy.
		// convert result to radians, i.e. ...*(float)Math.PI/180
		// and convert result to a distance using the formula between length of an arc and angle: arc = angle*radius
		// do not forget that Rover.WHEEL_RADIUS is in mm -> conversion to m with the 1/1000.
		float dist = Rover.WHEEL_RADIUS/1000*(l_tacho+r_tacho)/2*(float)Math.PI/180;
		// update the location appropriately.
		Point prev = this.pose.getLocation();
		this.add_dist(dist);
		return this.pose.getLocation().subtract(prev);
	}

	//######################################################################################################################
	//### Getters & Setters. ###############################################################################################
	//######################################################################################################################
	public Pose getPose() {
		return this.pose;
	}

	public Engine getRight() {
		return this.right;
	}

	public Engine getLeft() {
		return this.left;
	}
	
	public void setHeading(int heading) {
		this.pose.setHeading(heading);
	}

	public void setHeading(float heading) {
		this.pose.setHeading((int)heading);
	}

	//######################################################################################################################
	//### Miscellaneous. ###################################################################################################
	//######################################################################################################################
	/**
	 * Forces all the motors to go forward.
	 */
	public void forward() {
		this.right.device.forward();
		this.left.device.forward();
	}

	/**
	 * Tells if the rover is currently moving.
	 * 
	 * @return true if at least one of the motor is moving, false if it is otherwise at rest.
	 */
	public boolean isMoving() {
		return this.left.device.isMoving() || this.right.device.isMoving();
	}

	/**
	 * Adds a certain distance to the current location.
	 * The virtual move is performed along the direction of the rover, indicated by its heading.
	 * 
	 * @param dist the distance of the update, in m.
	 */
	public void add_dist(float dist) {
		this.pose.setLocation(this.pose.pointAt(dist, this.pose.getHeading()));
	}
	
	//######################################################################################################################
	//### GoTos. ###########################################################################################################
	//######################################################################################################################
	/**
	 * Performs a blocking goto to a point.
	 * 
	 * @param point the goal of the goto
	 * @deprecated one should consider using a combination of rotation and travel instead.
	 */
	public void goTo(Point point) {
		Point direction = point.subtract(this.pose.getLocation());
		this.rotateTo(direction.angle()*180/(float)Math.PI); // convert angle to degrees.
		this.travel(direction.length()*1000); // convert length to mm.
		
		// update location and heading.
		this.pose.setLocation(point);
		this.pose.setHeading(direction.angle());
	}
	/**
	 * Performs a blocking goto
	 * 
	 * @param point the goal of the goto
	 * @param immediate_return if true, the execution continues, allowing the user to do other stuff during goto.
	 * Otherwise, the program halts and waits for the goto to end.
	 * @deprecated one should consider using a combination of rotation and travel instead.
	 */
	public void goTo(Point point, boolean immediate_return) {
		// same remarks as above.
		Point direction = point.subtract(this.pose.getLocation());
		this.rotateTo(direction.angle()*180/(float)Math.PI, immediate_return);
		this.travel(direction.length()*1000, immediate_return);
		if (!immediate_return) {
			this.pose.setLocation(point);
			this.pose.setHeading(direction.angle());
		}
	}
	/**
	 * Performs a blocking goto to a pose.
	 * 
	 * @param pose the goal of the goto
	 * @deprecated one should consider using a combination of rotation and travel instead.
	 */
	public void goTo(Pose pose) {
		// same remarks as above.
		Point direction = pose.getLocation().subtract(this.pose.getLocation());
		this.rotateTo(direction.angle()*180/(float)Math.PI);
		this.travel(direction.length()*1000);
		this.rotateTo(pose.getHeading());
		
		this.pose = pose;
	}
	/**
	 * 
	 * @param point
	 * @deprecated use combination of rotations and travels instead.
	 */
	public void setup_goTo(Point point) {
		this.goTo(point, true);
		this.left.device.resetTachoCount();
		this.right.device.resetTachoCount();		
	}
	/**
	 * 
	 * @deprecated use combination of rotations and travels instead.
	 */
	public void compute_new_position_goTo() {
		int l_tacho = this.left.device.getTachoCount();
		int r_tacho = this.right.device.getTachoCount();
		this.pose.setHeading((l_tacho-r_tacho)/2*Rover.WHEEL_RADIUS/Rover.HALF_AXIS_DIFF);		
	}
}
