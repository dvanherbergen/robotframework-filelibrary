package org.robotframework.filelibrary.keyword;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.robotframework.filelibrary.FileLibraryException;
import org.robotframework.filelibrary.context.TemplateContext;
import org.robotframework.filelibrary.service.DatabaseService;
import org.robotframework.filelibrary.util.CsvUtil;
import org.robotframework.filelibrary.util.FileUtil;
import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywordOverload;
import org.robotframework.javalib.annotation.RobotKeywords;

@RobotKeywords
public class ContextKeywords {

	@RobotKeyword("Clear the template data. All existing data. When the optional argument True is given; the cleared template data will be preloaded with test variables.")
	@ArgumentNames("initialize=")
	public void resetTemplateData(String variables) {
		TemplateContext.getInstance().reset();
		if (variables != null) {
			TemplateContext.getInstance().setValuesFromJSON(variables);
		}
		System.out.println("Template Context Reset.");
	}

	@RobotKeywordOverload
	public void resetTemplateData() {
		resetTemplateData(null);
	}

	@RobotKeyword("Get a specific value from the template data context.")
	@ArgumentNames("attribute")
	public String getTemplateData(String attributeName) {
		Object value = TemplateContext.getInstance().getValue(attributeName);
		if (value != null) {
			return value.toString();
		}
		return "";
	}

	@RobotKeyword("Set a variable in the template context.")
	@ArgumentNames({ "attribute", "value" })
	public void setTemplateData(String name, String value) {
		TemplateContext.getInstance().setValue(name, value);
	}

	@RobotKeywordOverload
	public String logTemplateData() {
		return logTemplateData(null);
	}

	@RobotKeyword("Print all content of the template data as JSON. When an optional file name argument is supplied, the template context is saved as a JSON File.")
	@ArgumentNames("outputFilePath=")
	public String logTemplateData(String outputFilePath) {

		String contextString = TemplateContext.getInstance().toJSON();
		System.out.println("Template context: \n" + contextString);

		if (outputFilePath != null) {
			try {
				File outputFile = new File(outputFilePath);
				FileWriter writer = new FileWriter(outputFile);
				writer.append(TemplateContext.getInstance().toJSON());
				writer.close();
				System.out.println("Created '" + outputFile.getAbsolutePath() + "'");
			} catch (IOException e) {
				throw new FileLibraryException(e);
			}
		}
		return contextString;
	}

	@RobotKeyword("Load template data from CSV.")
	@ArgumentNames({ "attribute", "csvFilePath" })
	public void setTemplateDataFromCSV(String variableName, String file) {

		List<?> records = CsvUtil.loadValuesFromFile(file);
		TemplateContext.getInstance().setValueList(variableName, records);
	}

	@RobotKeyword("Load template data from SQL. Specify either an SQL to execute or the path to a .sql file.")
	@ArgumentNames({ "attribute", "sql" })
	public void setTemplateDataFromSQL(String attributePath, String sql) {

		List<String> sqls = new ArrayList<>();
		if (FileUtil.isSqlFileName(sql)) {
			sqls = FileUtil.parseSQLStatements(sql);
		} else {
			sqls.add(sql);
		}

		for (String stmt : sqls) {

			int resultLimit = 0;
			if (!TemplateContext.isListTarget(attributePath)) {
				resultLimit = 1;
			}

			List<Map<String, Object>> records = DatabaseService.getInstance().executeQuery(stmt, resultLimit);

			System.out.println("Query returned " + records.size() + " result.");
			if (!records.isEmpty()) {
				if (resultLimit == 1) {
					TemplateContext.getInstance().setValue(attributePath, records.get(0));
				} else {
					TemplateContext.getInstance().setValueList(attributePath, records);
				}
			}
		}
	}

	@RobotKeyword("Load template data from SQL. Specify either an SQL to execute or the path to a .sql file.")
	@ArgumentNames({ "attribute", "sql" })
	public void addTemplateDataFromSQL(String variableName, String sql) {

		if (variableName.indexOf("[]") != -1) {
			// TODO iterator over list, execute query for each entry

		}

		List<String> sqls = new ArrayList<>();
		if (FileUtil.isSqlFileName(sql)) {
			sqls = FileUtil.parseSQLStatements(sql);
		} else {
			sqls.add(sql);
		}

		for (String stmt : sqls) {
			List<Map<String, Object>> records = DatabaseService.getInstance().executeQuery(stmt, 1);
			System.out.println("Query returned " + records.size() + " result.");
			if (!records.isEmpty()) {
				TemplateContext.getInstance().setValue(variableName, records.get(0));
			}
		}
	}
}
