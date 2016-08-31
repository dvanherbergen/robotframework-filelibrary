package org.robotframework.filelibrary.keyword;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

	@RobotKeyword("Clear the template context. All existing data in the context will be removed.")
	public void resetTemplateContext() {
		TemplateContext.getInstance().reset();
		System.out.println("Template Context Reset.");
	}

	@RobotKeyword("Populate variables in the template context.")
	@ArgumentNames("props")
	public void setTemplateContext(Object props) {
		System.out.println("Received " + props);
	}

	@RobotKeyword("Populate a variable in the template context.")
	@ArgumentNames({ "name", "value" })
	public void setTemplateVariable(String name, String value) {
		TemplateContext.getInstance().setValue(name, value);
	}

	@RobotKeywordOverload
	public String logTemplateContext() {
		return logTemplateContext(null);
	}

	@RobotKeyword("Print all content of the template context as JSON. When an optional file name argument is supplied, the template context is saved as a JSON File.")
	@ArgumentNames("outputFilePath=")
	public String logTemplateContext(String outputFilePath) {

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

	@RobotKeyword("Load variables from CSV.")
	@ArgumentNames({ "variableName", "csvFilePath" })
	public void setTemplateVariableFromCSV(String variableName, String file) {

		List<?> records = CsvUtil.loadValuesFromFile(file);
		TemplateContext.getInstance().setValues(variableName, records);

	}

	@RobotKeyword("Load variables from SQL. Only the first record is used.")
	@ArgumentNames({ "variableName", "sql" })
	public void setTemplateVariableFromSQL(String variableName, String sql) {

		List<Map<String, Object>> records = DatabaseService.getInstance().executeQuery(sql);
		if (records.isEmpty()) {
			throw new FileLibraryException("SQL returned no results.");
		}
		System.out.println("Query returned " + records.size() + " results.");
		if (records.size() > 1) {
			System.out.println(
					"Only using first record. Use set template variable list from sql instead to use all records.");
		}
		TemplateContext.getInstance().setValue(variableName, records.get(0));
	}

	@RobotKeyword("Load variable list from SQL results.")
	@ArgumentNames({ "variableName", "sql" })
	public void setTemplateVariableListFromSQL(String variableName, String sql) {

		List<?> records = DatabaseService.getInstance().executeQuery(sql);
		System.out.println("Query returned " + records.size() + " results.");
		TemplateContext.getInstance().setValues(variableName, records);

	}

	@RobotKeyword("Load variable list from SQL results.")
	@ArgumentNames({ "variableName", "sql" })
	public void setTemplateVariableListFromSQLFile(String variableName, String filename) {

		List<String> sqls = FileUtil.parseSQLStatements(filename);
		if (sqls.size() != 1) {
			throw new FileLibraryException(
					"The sql file may only contain a single sql statement that returns multiple records. Currently the file contains "
							+ sqls.size() + " statements");
		}
		setTemplateVariableListFromSQL(variableName, sqls.get(0));
	}

	@RobotKeyword("Load variable list from SQL results.")
	@ArgumentNames({ "variableName", "sql" })
	public void setTemplateVariableFromSQLFile(String variableName, String filename) {

		List<String> sqls = FileUtil.parseSQLStatements(filename);
		for (String sql : sqls) {
			setTemplateVariableFromSQL(variableName, sql);
		}
	}
}
