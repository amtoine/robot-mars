package rover;

/**
 * Data container to read from any peripheral indifferently.
 * 
 * @author Antoine Stevan.
 *
 */
class Measure {
	/** A Measure container is composed of a float value. */
	float value;
	
	/**
	 * Takes a value and stores it inside a Measure data container.
	 * @param value the real value to be stored inside the container.
	 */
	Measure(float value){
		this.value = value;
	}
}
