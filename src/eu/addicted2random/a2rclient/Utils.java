package eu.addicted2random.a2rclient;

public class Utils {
	
	/**
	 * Scale a value from one range to another range.
	 * 
	 * @param value The value to scale
	 * @param min The minimum value of the input range
	 * @param max The maximum value of the input range
	 * @param outMin The minimum value of the output range
	 * @param outMax The maximum value of the output range
	 * @return
	 */
	static public float scale(float value, float min, float max, float outMin, float outMax) {
		return (((outMax - outMin) * (value - min)) / (max - min)) + outMin;
	}
}
