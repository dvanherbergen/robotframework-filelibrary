package org.robotframework.filelibrary.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

	public static void appendFiles(String targetFile, String... sourceFiles) {

		OutputStream out = null;
		InputStream in = null;

		try {

			out = new FileOutputStream(targetFile);
			byte[] buf = new byte[4096];
			for (String file : sourceFiles) {

				File inputFile = new File(file);
				if (!inputFile.exists()) {
					throw new FileLibraryException("File not found " + inputFile.getAbsolutePath());
				}
				in = new FileInputStream(inputFile);
				int b = 0;
				while ((b = in.read(buf)) >= 0) {
					out.write(buf, 0, b);
					out.flush();
				}
				in.close();
			}
		} catch (IOException e) {
			throw new FileLibraryException(e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void compressFiles(String targetFile, String[] sourceFiles) {

		ZipOutputStream out = null;

		try {

			out = new ZipOutputStream(new FileOutputStream(targetFile));
			byte[] buffer = new byte[4096];
			for (String file : sourceFiles) {

				File inputFile = new File(file);
				if (!inputFile.exists()) {
					throw new FileLibraryException("File not found " + inputFile.getAbsolutePath());
				}

				ZipEntry ze = new ZipEntry(inputFile.getName());
				out.putNextEntry(ze);
				FileInputStream in = new FileInputStream(inputFile);

				int len;
				while ((len = in.read(buffer)) > 0) {
					out.write(buffer, 0, len);
				}

				in.close();
				out.closeEntry();
			}

		} catch (IOException e) {
			throw new FileLibraryException(e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
