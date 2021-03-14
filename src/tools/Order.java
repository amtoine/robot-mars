package tools;

/**
 * Data container to write to any peripheral indifferently.
 * 
 * @author Antoine Stevan.
 *
 */
public class Order {
	/** An order can contain a speed, for a Motor. */
	int speed;
	/** An order can contain an angle, for a Motor. */
	int angle;
	
	/**
	 * Takes a speed and an angle and stores them inside an Order data container.
	 * @param speed the speed for the Motor.
	 * @param angle the relative rotation angle for the Motor.
	 */
	public Order(int speed, int angle) {
		this.speed = speed;
		this.angle = angle;
	}
	/**
	 * Getter for the 'speed' field.
	 * @return the speed given as an order.
	 */
	public int getSpeed() {
		return this.speed;
	}
	/**
	 * Getter for the 'angle' field.
	 * @return the angle given as an order.
	 */
	public int getAngle() {
		return this.angle;
	}
}
