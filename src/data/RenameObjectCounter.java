package data;

/**
 * Counts the amount of objects in total that will be renamed, these can be
 * anything from variable names to package names
 * 
 * @author sander
 * 
 */
public class RenameObjectCounter {
	private static long count = 0;

	public static void increaseCount(int c) {
		count += c;
	}

	public static long getCount() {
		return count;
	}

	public static void reset() {
		count = 0;
	}
}
