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
	private Pose pose;
	private Engine right;
	private Engine left;
	
	public Navigator(Pose initialPose, Engine right, Engine left) {
		this.pose = initialPose;
		this.right = right;
		this.left = left;
	}

	public void rotate(float angle) {
		int theta = (int)(-angle * Rover.HALF_AXIS_DIFF/Rover.WHEEL_RADIUS);
		this.right.device.rotate(theta, true);
		this.left.device.rotate(-theta, true);
		while (this.left.device.isMoving() || this.right.device.isMoving()) {
			Thread.yield();
		}
		this.pose.setHeading(this.pose.getHeading()+angle);
	}
	
	public void rotate(float angle, boolean immediate_return) {
		int theta = (int)(-angle * Rover.HALF_AXIS_DIFF/Rover.WHEEL_RADIUS);
		this.right.device.rotate(theta, immediate_return);
		this.left.device.rotate(-theta, immediate_return);
		if (!immediate_return) {
			while (this.left.device.isMoving() || this.right.device.isMoving()) {
				Thread.yield();
			}
		}
	}
	
	public void rotateTo(float angle) {
		int theta = (int)((angle-this.pose.getHeading()) * Rover.HALF_AXIS_DIFF/Rover.WHEEL_RADIUS);
		System.out.println("t: " + theta);
		this.right.device.rotate(theta, true);
		this.left.device.rotate(-theta, true);
		while (this.left.device.isMoving() || this.right.device.isMoving()) {
			Thread.yield();
		}
		this.pose.setHeading(angle);
	}
	
	public void rotateTo(float angle, boolean immediate_return) {
		int theta = (int)((angle-this.pose.getHeading()) * Rover.HALF_AXIS_DIFF/Rover.WHEEL_RADIUS);
		System.out.println("t: " + theta);
		this.right.device.rotate(theta, immediate_return);
		this.left.device.rotate(-theta, immediate_return);
		if (!immediate_return) {
			while (this.left.device.isMoving() || this.right.device.isMoving()) {
				Thread.yield();
			}
		}
	}
	
	public void travel(float length) {
		int theta = (int)(-length / Rover.WHEEL_RADIUS * 180 / Math.PI);
		this.right.device.rotate(theta, true);
		this.left.device.rotate( theta, true);
		while (this.left.device.isMoving() || this.right.device.isMoving()) {
			Thread.yield();
		}
		this.pose.setLocation(this.pose.getLocation().pointAt(length, this.pose.getHeading()));
	}
	
	public void travel(float length, boolean immediate_return) {
		int theta = (int)(-length / Rover.WHEEL_RADIUS * 180 / Math.PI);
		this.right.device.rotate(theta, immediate_return);
		this.left.device.rotate( theta, immediate_return);
		if (!immediate_return) {
			while (this.left.device.isMoving() || this.right.device.isMoving()) {
				Thread.yield();
			}
		}
	}

	public void goTo(Point point) {
		Point direction = point.subtract(this.pose.getLocation());
		System.out.println("rotateTo " + direction.angle()*180/(float)Math.PI);
		this.rotateTo(direction.angle()*180/(float)Math.PI);
		System.out.println("travel " + direction.length()*1000);
		this.travel(direction.length()*1000);
		
		this.pose.setLocation(point);
		this.pose.setHeading(direction.angle());
	}
	
	public void goTo(Point point, boolean immediate_return) {
		Point direction = point.subtract(this.pose.getLocation());
		System.out.println("rotateTo " + direction.angle()*180/(float)Math.PI);
		this.rotateTo(direction.angle()*180/(float)Math.PI, immediate_return);
		System.out.println("travel " + direction.length()*1000);
		this.travel(direction.length()*1000, immediate_return);
	}
	
	public void goTo(Pose pose) {
		Point direction = pose.getLocation().subtract(this.pose.getLocation());
		System.out.println("dir: "+direction.getX() + ", "+ direction.getY());
		System.out.println("rotateTo " + direction.angle()*180/Math.PI);
		this.rotateTo(direction.angle()*180/(float)Math.PI);
		System.out.println("travel " + direction.length()*1000);
		this.travel(direction.length()*1000);
		System.out.println("rotateTo " + pose.getHeading());
		this.rotateTo(pose.getHeading());
		
		this.pose = pose;
	}

	public Pose getPose() {
		return this.pose;
	}

	public Engine getRight() {
		return this.right;
	}

	public Engine getLeft() {
		return this.left;
	}

	public void forward() {
		this.right.device.forward();
		this.left.device.forward();
	}

	public boolean isMoving() {
		return this.left.device.isMoving() || this.right.device.isMoving();
	}

	public void add_dist(float dist) {
		this.pose.setLocation(this.pose.pointAt(-dist, this.pose.getHeading()));
	}

	public void setHeading(int heading) {
		this.pose.setHeading(heading);
	}

	public void setHeading(float heading) {
		this.pose.setHeading((int)heading);
	}

	public void setup_travel(int length) {
		this.travel(length, true);
		this.left.device.resetTachoCount();
		this.right.device.resetTachoCount();
	}
	public void setup_travel(float length) {
		this.setup_travel((int)length);
	}

	public void compute_new_position() {
		int l_tacho = this.left.device.getTachoCount();
		int r_tacho = this.right.device.getTachoCount();
		float dist = Rover.WHEEL_RADIUS/1000*(l_tacho+r_tacho)/2*(float)Math.PI/180;
		this.add_dist(dist);		
	}

	public void setup_rotate(int angle) {
		this.rotate(angle, true);
		this.left.device.resetTachoCount();
		this.right.device.resetTachoCount();		
	}
	
	public void setup_rotate(float angle) {
		this.setup_rotate((int)angle);
	}

	public void compute_new_heading() {
		int l_tacho = this.left.device.getTachoCount();
		int r_tacho = this.right.device.getTachoCount();
		this.pose.setHeading(this.pose.getHeading()-(l_tacho-r_tacho)/2*Rover.WHEEL_RADIUS/Rover.HALF_AXIS_DIFF);		
	}
	
	public void setup_goTo(Point point) {
		this.goTo(point, true);
		this.left.device.resetTachoCount();
		this.right.device.resetTachoCount();		
	}

	public void compute_new_position_goTo() {
		int l_tacho = this.left.device.getTachoCount();
		int r_tacho = this.right.device.getTachoCount();
		this.pose.setHeading((l_tacho-r_tacho)/2*Rover.WHEEL_RADIUS/Rover.HALF_AXIS_DIFF);		
	}
}
