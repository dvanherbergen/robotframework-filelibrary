package org.robotframework.filelibrary.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.robotframework.filelibrary.FileLibraryException;
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;

public class CompareUtil {

	public static final String WILDCARD = "{IGNORE}";

	public static void compareXMLFiles(String templateFilePath, String actualFilePath) {
		compareXMLFiles(templateFilePath, actualFilePath, new HashSet<String>());
	}

	public static void compareXMLFiles(String templateFilePath, String actualFilePath, Set<String> nodeFilter) {
		File expectedSource = loadFile(templateFilePath);
		File actualSource = loadFile(actualFilePath);
		// @formatter:off
		final Diff documentDiff = DiffBuilder
				.compare(expectedSource)
				.ignoreComments()
				.ignoreWhitespace()
				.withTest(actualSource)
				.withDifferenceEvaluator(DifferenceEvaluators.chain(DifferenceEvaluators.Default, new DifferenceEvaluator() {
					@Override
					public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
						if (outcome == ComparisonResult.EQUAL)
							return outcome; // only evaluate differences.

						Object test = comparison.getTestDetails().getValue();
						if (WILDCARD.equals(test)) {
							return ComparisonResult.SIMILAR;
						}

						return outcome;
					}
				}))
				.withNodeFilter(node -> shouldEvaluateNode(node, nodeFilter))
				.checkForSimilar()
				.build();
		// @formatter:off
		if (documentDiff.hasDifferences()) {
			StringBuffer buffer = new StringBuffer();
			documentDiff.getDifferences().forEach(difference -> buffer.append(difference).append("\n"));
			throw new FileLibraryException("Files have differences:\n" + buffer.toString());
		}
	}

	private static File loadFile(String templateFilePath) {
		File expectedSource = new File(templateFilePath);
		if (!expectedSource.exists()) {
			throw new FileLibraryException("Cannot find '" + expectedSource.getAbsolutePath() + "'");
		}
		return expectedSource;
	}

	private static boolean shouldEvaluateNode(Node node, Set<String> nodeFilter) {
		if (nodeFilter.isEmpty()) {
			return true;
		}
		return !nodeFilter.contains(getXPath(node));

	}

	public static String getXPath(Node node) {
		Node parent = node.getParentNode();
		if (parent == null) {
			return "";
		}
		return getXPath(parent) + "/" + node.getLocalName() ;
	}

	public static void compareFiles(String filePath1, String filePath2) {

		// TODO add better support for XML comparisons
		// TODO add ignore wildcards

		File file1 = loadFile(filePath1);
		File file2 = loadFile(filePath2);

		BufferedReader reader1 = null;
		BufferedReader reader2 = null;

		try {
			reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(file1)));
			reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(file2)));

			String line1 = null;
			String line2 = null;

			while ((line1 = reader1.readLine()) != null) {
				line2 = reader2.readLine();

				if (line2 == null) {
					throw new FileLibraryException("Missing line: " + line1);
				}

				if (!TextUtil.matches(line1, line2)) {
					throw new FileLibraryException("Lines do not match: \n" + line1 + "\n" + line2);
				}

			}

			if ((line2 = reader2.readLine()) != null) {
				throw new FileLibraryException("Found unexpected line: " + line2);
			}
		} catch (IOException e) {
			throw new FileLibraryException(e);
		} finally {
			try {
				reader1.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				reader2.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
