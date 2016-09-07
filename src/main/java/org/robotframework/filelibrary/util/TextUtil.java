package org.robotframework.filelibrary.util;

/**
 * Utility class for various string text operations.
 */
public class TextUtil {

	/**
	 * Check if a given string matches the expected string pattern.
	 * 
	 * The expected pattern can contain wildcards.
	 * 
	 * @param actual
	 *            text found in the appliction
	 * @param expected
	 *            text described in the test case
	 * @return true if it matches
	 */
	public static boolean matches(String actual, String expected) {

		if (actual == null) {
			actual = "";
		}
		if (expected == null) {
			expected = "";
		}

		actual = actual.toLowerCase().trim();
		expected = expected.toLowerCase().trim();

		if (expected.endsWith("*")) {
			return actual.startsWith(expected.substring(0, expected.length() - 1));
		}

		return actual.equals(expected);
	}

}
