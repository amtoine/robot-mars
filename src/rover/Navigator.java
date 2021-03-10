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
		int theta = (int)(angle * Rover.HALF_WIDTH/Rover.WHEEL_RADIUS);
		this.right.device.rotate(theta, true);
		this.left.device.rotate(-theta, true);
		while (this.left.device.isMoving() || this.right.device.isMoving()) {
			Thread.yield();
		}
		this.pose.setHeading(this.pose.getHeading()+angle);
	}
	
	public void rotateTo(float angle) {
		int theta = (int)((angle-this.pose.getHeading()) * Rover.HALF_WIDTH/Rover.WHEEL_RADIUS);
		this.right.device.rotate(theta, true);
		this.left.device.rotate(-theta, true);
		while (this.left.device.isMoving() || this.right.device.isMoving()) {
			Thread.yield();
		}
		this.pose.setHeading(angle);
	}
	
	public void travel(float length) {
		int theta = (int)(length / Rover.WHEEL_RADIUS);
		this.right.device.rotate(theta, true);
		this.left.device.rotate( theta, true);
		while (this.left.device.isMoving() || this.right.device.isMoving()) {
			Thread.yield();
		}
	}
	
	public void travel(float length, boolean b) {
		
	}

	public void goTo(Point point) {
		Point direction = point.subtract(this.pose.getLocation());
		this.rotateTo(direction.angle());
		this.travel(direction.length());
		
		this.pose.setLocation(point);
		this.pose.setHeading(direction.angle());
	}
	
	public void goTo(Pose pose) {
		Point direction = pose.getLocation().subtract(this.pose.getLocation());
		this.rotateTo(direction.angle());
		this.travel(direction.length());
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
		this.pose.setLocation(this.pose.pointAt(dist, this.pose.getHeading()));
	}
}
