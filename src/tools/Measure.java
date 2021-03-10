package tools;

/**
 * Data container to read from any peripheral indifferently.
 * 
 * @author Antoine Stevan.
 *
 */
public class Measure {
	/** A Measure container is composed of a float value. */
	private float value;
	
	/**
	 * Takes a value and stores it inside a Measure data container.
	 * @param value the real value to be stored inside the container.
	 */
	public Measure(float value){
		this.value = value;
	}
	
	public float getValue() {
		return this.value;
	}
}
