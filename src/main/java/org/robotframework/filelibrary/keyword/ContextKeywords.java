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

	// @formatter:off
	@RobotKeyword("Load template data from SQL results. Specify either an SQL to execute or the path to a .sql file.\n" + 
			"\n" + 
			"Example uses:\n" + 
			"\n" + 
			"\n" + 
			"Usage:\n" + 
			"| Set Template Data From SQL | _attributePath_ | _sql_ |\n" + 
			"| Set Template Data From SQL | _attributePath_ | _sqlFile_ |\n" + 
			"Example usage:\n" + 
			"\n" + 
			"| Set Template Data From SQL | soup | soup-select.sql |\n" + 
			"| Set Template Data From SQL | soup | select brand, type, color from soups where id = 1 |\n" + 
			"| Set Template Data From SQL | soup.ingredients[] | select i.name from ingredients i, soup_ingredients si where i.id = si.ingredient_id and si.soup_id = 1 |\n" + 
			"| Set Template Data From SQL | soup.ingredients[].suppliers | select s.name from suppliers s, ingredient_suppliers is where s.id = is.supplier_id and is.ingredient_name = ${soup.ingredients[].name}  |\n" + 
			"| Set Template Data From SQL | soup.ingredients[].suppliers[].contact | select c.phone from supplier_contact c where c.supplier_name = ${soup.ingredients[].suppliers[].name} |\n" + 
			"\n\nThe examples shown above could result in the following template data structure:\n\n" + 
			"| { \"soup\": {\n" + 
			"|      \"brand\" : \"Campbells\",\n" + 
			"|      \"type\" : \"Tomato\",\n" + 
			"|      \"color\" : \"red\",\n" + 
			"|      \"ingredients\" : [\n" + 
			"|          {\n" + 
			"|             \"name\" : \"tomato\",\n" + 
			"|             \"suppliers\" : [\n" + 
			"|                { \n" + 
			"|                   \"name\" : \"the best tomato company\" \n" + 
			"|                   \"contact\" : {\n" + 
			"|                      \"phone\" : \"555-555.555\"\n" + 
			"|                   }\n" + 
			"|                },\n" + 
			"|                { \n" + 
			"|                   \"name\" : \"the second best tomato company\",\n" + 
			"|                   \"contact\" : {\n" + 
			"|                      \"phone\" : \"666-666.666\"\n" + 
			"|                   }\n" + 
			"|                }\n" + 
			"|             ]\n" + 
			"|          },\n" + 
			"|          {\n" + 
			"|             \"name\" : \"potato\",\n" + 
			"|             \"suppliers\" : [\n" + 
			"|                { \"name\" : \"the only potato company\" }\n" + 
			"|             ]            \n" + 
			"|          },\n" + 
			"|          {\n" + 
			"|             \"name\" : \"water\",\n" + 
			"|             \"suppliers\" : []            \n" + 
			"|          },\n" + 
			"|          {\n" + 
			"|             \"name\" : \"pepper\",\n" + 
			"|             \"suppliers\" : [\n" + 
			"|                { \"name\" : \"the sweet pepper company\" },\n" + 
			"|                { \"name\" : \"the spicy pepper company\" }\n" + 
			"|             ]               \n" + 
			"|          }\n" + 
			"|       ]\n" + 
			"|    }\n" + 
			"| }"  
		)
	// @formatter:on
	@ArgumentNames({ "attribute", "sql" })
	public void setTemplateDataFromSQL(String attributePath, String sql) {

		if (attributePath.indexOf("[].") != -1) {
			iterateAndSetDataFromSQL(attributePath, sql);
			// TODO add list option
		}

		if (TemplateContext.isListTarget(attributePath)) {
			setDataListFromSQL(attributePath, sql);
		} else {
			setDataFromSQL(attributePath, sql);
		}
	}

	private List<String> getSQLStatements(String input) {
		List<String> sqls = new ArrayList<>();
		if (FileUtil.isSqlFileName(input)) {
			sqls = FileUtil.parseSQLStatements(input);
		} else {
			sqls.add(input);
		}
		return sqls;
	}

	/**
	 * populate an data attribute with a list of records
	 */
	private void setDataListFromSQL(String attributePath, String sql) {

		for (String stmt : getSQLStatements(sql)) {
			List<Map<String, Object>> records = DatabaseService.getInstance().executeQuery(stmt, 0);
			System.out.println("Query returned " + records.size() + " result.");
			if (!records.isEmpty()) {
				TemplateContext.getInstance().setValueList(attributePath, records);
			}
		}

	}

	/**
	 * populate an data attribute with the values of a single record
	 */
	private void setDataFromSQL(String attributePath, String sql) {

		for (String stmt : getSQLStatements(sql)) {
			List<Map<String, Object>> records = DatabaseService.getInstance().executeQuery(stmt, 1);
			System.out.println("Query returned " + records.size() + " result.");
			if (!records.isEmpty()) {
				TemplateContext.getInstance().setValue(attributePath, records.get(0));
			}
		}
	}

	private void iterateAndSetDataFromSQL(String variableName, String sql) {

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
