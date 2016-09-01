package org.robotframework.filelibrary.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Util to replace all ${..} parameters in an sql statement with a ?
 */
public class StatementParser {

	private List<String> parameters = new ArrayList<String>();

	private String statement;

	public StatementParser(String text) {
		statement = parseText(text).trim();
		if (statement.endsWith(";")) {
			statement = statement.substring(0, statement.indexOf(';')).trim();
		}
	}

	private String parseText(String text) {

		StringBuilder builder = new StringBuilder();

		int startPos = text.indexOf("${");
		if (startPos == -1) {
			return text;
		}

		int endPos = text.indexOf('}', startPos);
		if (endPos == -1) {
			return text;
		}

		if (startPos > 0) {
			builder.append(text.substring(0, startPos));
		}

		parameters.add(text.substring(startPos + 2, endPos));

		builder.append("?");
		if (endPos < text.length()) {
			builder.append(parseText(text.substring(endPos + 1)));
		}

		return builder.toString();
	}

	public List<String> getParameters() {
		return parameters;
	}

	public String getStatement() {
		return statement.toString();
	}

}
