package org.robotframework.filelibrary.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Util to replace all ${..} parameters in an sql statement with a ?
 */
public class StatementParser {

	private List<String> parameters = new ArrayList<String>();

	private StringBuilder statement = new StringBuilder();

	public StatementParser(String text) {
		parseText(text);
	}

	private void parseText(String text) {

		int startPos = text.indexOf("${");
		if (startPos == -1) {
			statement.append(text);
			return;
		}

		int endPos = text.indexOf('}', startPos);
		if (endPos == -1) {
			statement.append(text);
			return;
		}

		if (startPos > 0) {
			statement.append(text.substring(0, startPos));
		}

		parameters.add(text.substring(startPos + 2, endPos));

		statement.append("?");
		if (endPos < text.length()) {
			parseText(text.substring(endPos + 1));
		}

	}

	public List<String> getParameters() {
		return parameters;
	}

	public String getStatement() {
		return statement.toString();
	}

}
