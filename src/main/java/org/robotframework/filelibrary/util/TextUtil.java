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

	public static boolean containsIndex(String input) {
		return input.matches(".+\\[[0-9]*\\]");
	}

	public static int getIndex(String input) {
		if (!containsIndex(input)) {
			return 0;
		}
		String indexValue = input.substring(input.lastIndexOf('[') + 1, input.lastIndexOf(']'));
		if (indexValue.length() == 0) {
			return -1;
		}
		return new Integer(indexValue);
	}

	public static String removeIndex(String input) {
		if (!containsIndex(input)) {
			return input;
		}
		return input.substring(0, input.lastIndexOf('['));
	}

	/**
	 * Extract the first segment of a . separated input.
	 */
	public static String getFirstSegment(String input) {

		if (input == null || input.trim().length() == 0) {
			return null;
		}

		if (input.indexOf('.') != -1) {
			return input.split("\\.")[0].trim();
		} else {
			return input.trim();
		}

	}

	/**
	 * Return the input text with the first segment removed.
	 */
	public static String getNextSegments(String input) {

		if (input.indexOf('.') != -1) {
			return input.substring(input.indexOf('.') + 1).trim();
		} else {
			return null;
		}
	}

	public static String addSegment(String segment, String segmentToAdd) {
		if (segment == null || segment.length() == 0) {
			return segmentToAdd;
		}
		if (segmentToAdd == null || segmentToAdd.length() == 0) {
			return segment;
		}
		return segment + "." + segmentToAdd;
	}

	public static String[] populateIndexes(String source, String[] target) {

		if (target == null || target.length == 0) {
			return new String[0];
		}

		String[] results = new String[target.length];

		for (int i = 0; i < target.length; i++) {
			results[i] = copyIndex(source, target[i]);
		}

		return results;
	}

	private static String copyIndex(String source, String target) {

		if (target.indexOf("[]") == -1) {
			return target;
		}

		String[] sourceElements = source.split("\\.");
		String[] targetElements = target.split("\\.");
		String result = "";

		for (int i = 0; i < targetElements.length; i++) {

			if (targetElements[i].endsWith("[]")) {
				result = TextUtil.addSegment(result, TextUtil.removeIndex(targetElements[i]) + "[" + TextUtil.getIndex(sourceElements[i]) + "]");
			} else {
				result = TextUtil.addSegment(result, targetElements[i]);
			}
		}

		return result;
	}
}
