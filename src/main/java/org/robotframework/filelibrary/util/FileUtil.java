package org.robotframework.filelibrary.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.robotframework.filelibrary.FileLibraryException;

public class FileUtil {

	private static final Pattern sqlFilenamePattern = Pattern.compile(".*\\.sql");

	public static List<String> parseSQLStatements(String filename) {

		List<String> statements = new ArrayList<String>();

		File inputFile = new File(filename);
		if (!inputFile.exists()) {
			throw new FileLibraryException("File not found '" + inputFile.getAbsolutePath() + "'.");
		}

		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			StringBuilder statement = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {

				line = line.replaceAll("\\t", " ").replaceAll("  ", " ").replaceAll("  ", " ").trim();

				if (line.startsWith("#") || line.startsWith("--") || line.length() == 0) {
					// ignore comment or blank lines.
					continue;
				}

				if (statement.length() > 0) {
					statement.append(" ");
				}

				if (line.indexOf(';') != -1) {
					line = line.substring(0, line.lastIndexOf(';')).trim();
					statement.append(line);
					statements.add(statement.toString().trim());
					statement = new StringBuilder();
				} else {
					statement.append(line);
				}
			}

			if (statement.length() > 0) {
				statements.add(statement.toString());
			}

		} catch (Exception e) {
			throw new FileLibraryException("Cannot read sql file '" + inputFile.getAbsolutePath() + "'.", e);
		}

		if (statements.isEmpty()) {
			throw new FileLibraryException("No statements found in file '" + inputFile.getAbsolutePath() + "'.");
		}

		return statements;
	}

	public static boolean isSqlFileName(String value) {
		return value != null && sqlFilenamePattern.matcher(value.trim().toLowerCase()).matches();
	}

}
