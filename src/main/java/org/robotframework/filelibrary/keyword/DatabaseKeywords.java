package org.robotframework.filelibrary.keyword;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.robotframework.filelibrary.FileLibraryException;
import org.robotframework.filelibrary.context.TemplateContext;
import org.robotframework.filelibrary.remote.RPCServer;
import org.robotframework.filelibrary.service.DatabaseService;
import org.robotframework.filelibrary.util.FileUtil;
import org.robotframework.filelibrary.util.StatementParser;
import org.robotframework.filelibrary.util.TextUtil;
import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;

@RobotKeywords
public class DatabaseKeywords {

	DatabaseService service = DatabaseService.getInstance();

	@RobotKeyword("Connect to database using the specified arguments")
	@ArgumentNames({ "driver", "url", "user", "password" })
	public void connect(String driver, String url, String user, String password) {
		service.connect(driver, url, user, password);
	}

	@RobotKeyword("Set query timeout. Default timeout is 30 seconds.")
	@ArgumentNames({ "seconds" })
	public void setQueryTimeOut(int timeout) {
		service.setQueryTimeOut(timeout);
	}

	@RobotKeyword("Execute SQL INSERT or UPDATE statement(s). Specify either an SQL directly or the path to a .sql file.")
	@ArgumentNames({ "sql" })
	public void executeSQL(String sql) {

		List<String> sqls = new ArrayList<>();
		if (FileUtil.isSqlFileName(sql)) {
			sqls = FileUtil.parseSQLStatements(sql);
		} else {
			sqls.add(sql);
		}

		for (String rawSql : sqls) {
			StatementParser parser = new StatementParser(rawSql);
			service.executeStatement(parser.getStatement(), TemplateContext.getInstance().resolveAttributes(parser.getParameters()));
		}
	}

	@RobotKeyword("Execute a SQL statement or .sql file and verify that returned values match the expected values.")
	@ArgumentNames({ "sql", "*values" })
	public void verifySQLResult(String sql, String... expectedValues) {

		StatementParser parser = new StatementParser(sql);
		service.verifyQueryResults(parser.getStatement(), TemplateContext.getInstance().resolveAttributes(parser.getParameters()), expectedValues);
	}

	@RobotKeyword("Close SQL session")
	public void disconnect() {
		service.disconnect();
	}

	@RobotKeyword("Stop remote library.")
	public void stopFileLibraryProcess() {

		service.disconnect();
		try {
			RPCServer.getInstance().stop();
		} catch (Exception e) {
			throw new FileLibraryException(e);
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
			"|                   \"name\" : \"the best tomato company\" ,\n" + 
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
	public void setTemplateDataFromSQL(String targetAttributePath, String sql) {

		boolean targetIsList = TemplateContext.isListTarget(targetAttributePath);

		List<String> sqls = new ArrayList<>();
		if (FileUtil.isSqlFileName(sql)) {
			sqls = FileUtil.parseSQLStatements(sql);
		} else {
			sqls.add(sql);
		}

		for (String attributePath : TemplateContext.getInstance().expandTargetAttributes(targetAttributePath)) {

			for (String stmt : sqls) {
				StatementParser parser = new StatementParser(stmt);
				String[] parameters = TextUtil.populateIndexes(attributePath, parser.getParameters());
				parameters = TemplateContext.getInstance().resolveAttributes(parameters);
				List<Map<String, Object>> records = DatabaseService.getInstance().executeQuery(parser.getStatement(), parameters,
						(targetIsList ? 0 : 1));
				System.out.println("Query returned " + records.size() + " result.");
				if (!records.isEmpty()) {
					if (targetIsList) {
						// populate an data attribute with a list of records
						TemplateContext.getInstance().setValueList(attributePath, records);
					} else {
						// populate an data attribute with the values of a
						// single record
						TemplateContext.getInstance().setValue(attributePath, records.get(0));
					}
				}
			}
		}
	}

}
