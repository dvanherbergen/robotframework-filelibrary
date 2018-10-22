package io.github.dvanherbergen.filelibrary.keyword;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;

import io.github.dvanherbergen.filelibrary.FileLibraryException;
import io.github.dvanherbergen.filelibrary.context.TemplateContext;
import io.github.dvanherbergen.filelibrary.service.DatabaseService;
import io.github.dvanherbergen.filelibrary.util.FileUtil;
import io.github.dvanherbergen.filelibrary.util.StatementParser;

@RobotKeywords
public class DatabaseKeywords {

	DatabaseService service = DatabaseService.getInstance();

	@RobotKeyword("Connect to database using the specified arguments")
	@ArgumentNames({ "url", "user", "password" })
	public void connect(String url, String user, String password) {
		service.connect(url, user, password);
	}

	@RobotKeyword("Set timeout for SQL executions. The default timeout is 30 seconds.")
	@ArgumentNames({ "seconds" })
	public void setQueryTimeOut(int timeout) {
		service.setQueryTimeOut(timeout);
	}

	@RobotKeyword("Replace all database table content with the content in the specified xls/xml file. Uses DBUnit CLEAN_INSERT to perform the update.\n"
	+ "If the database does not have proper primary keys, you can specify custom primary keys per table in the format TABLE=KEY1,KEY2.\n"
	+ "When using this option, no default primary keys will be used. You will have to specify the primary keys for all tables included in the file."
	+ "Example usage:\n"
	+ " | replaceTables | data.xlsx | MYSCHEMA | EMPLOYEE=FIRSTNAME,LASTNAME | EMPLOYER=NAME | \n"
	)
	@ArgumentNames({"xlsFilename", "schema", "*primaryKeys"})
	public void replaceTables(String filename, String schema, String[] primaryKeys) {
		service.loadFromFile(filename, schema, true, primaryKeys);
	}
	
	@RobotKeyword("Reload database tables content with the content in the specified xls/xml file. Existing rows are updated. Missing rows are added. Uses DBUnit REFRESH to perform the update."
			+ "If the database does not have proper primary keys, you can specify custom primary keys per table in the format TABLE=KEY1,KEY2.\n"
			+ "When using this option, no default primary keys will be used. You will have to specify the primary keys for all tables included in the file."
			+ "Example usage:\n"
			+ " | replaceTables | data.xlsx | MYSCHEMA | EMPLOYEE=FIRSTNAME,LASTNAME | EMPLOYER=NAME | \n")
	@ArgumentNames({"xlsFilename", "schema=", "*primaryKeys"})
	public void refreshTables(String filename, String schema, String[] primaryKeys) {
		service.loadFromFile(filename, schema, false, primaryKeys);
	}
	
	@RobotKeyword("Execute SQL INSERT or UPDATE statement(s). Specify either an SQL directly or the path to a .sql file. No results are returned from this keyword.")
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

	// @formatter:off
	@RobotKeyword("Execute SQL Query. Returns a list of values. When the query selects a single column, a simple list of values is returned.\n"
			+ "When the query selects multiple columns a list of lists is returned.\n" +
	"Example usage:\n"
	+ " | @{singleColResult}= | Execute Query | select 1 from dual union select 2 from dual | \n"
	+ " | Log Many | @{singleColResult} | | \n"	
	+ " | @{multiColResult}= | Execute Query | select 1,2 from dual union select 3,4 from dual | \n"
	+ " | Log Many | ${singleColResult[2]} | | \n")
	// @formatter:on	
	@ArgumentNames({ "sql" })
	public List<Object> executeQuery(String sql) {

		List<String> sqls = new ArrayList<>();
		if (FileUtil.isSqlFileName(sql)) {
			sqls = FileUtil.parseSQLStatements(sql);
		} else {
			sqls.add(sql);
		}

		if (sqls.size() != 1) {
			throw new FileLibraryException("Only a single SQL Query is allowed in the .sql file for this keyword.");
		}

		StatementParser parser = new StatementParser(sqls.get(0));
		return service.getQueryResultsAsList(parser.getStatement(), TemplateContext.getInstance().resolveAttributes(parser.getParameters()), 0);
	}

	@RobotKeyword("Execute SQL Query and results a single result. See Execute Query for details")
	@ArgumentNames({ "sql" })
	public Object executeSingleResultQuery(String sql) {
		List<Object> result = executeQuery(sql);
		if (result.size() != 1) {
			throw new FileLibraryException("Expected a single result, got " + result.size() + " records instead");
		}
		return result.get(0);
	}

	@RobotKeyword("Execute a SQL statement or .sql file and verify that returned values match the expected values.")
	@ArgumentNames({ "sql", "*values" })
	public void verifySQLResult(String sql, String... expectedValues) {

		StatementParser parser = new StatementParser(sql);
		service.verifyQueryResults(parser.getStatement(), TemplateContext.getInstance().resolveAttributes(parser.getParameters()), expectedValues);
	}

	@RobotKeyword("Call a stored procedure with the given parameters \n"
			+ "Example: Call Procedure    {call robot.hello_world (?,?,?,?)}     name    123456    45.66    {d}2016-01-21\n"
			+ "Most parameter types will be detected and converted automatically, however sometimes you might need to specify the type. \n"
			+ "    - Use {i} to pass the value as a number\n" + "    - Use {f} to pass the value as a decimal\n"
			+ "    - Use {d} to pass the value as a date in format yyyy-mm-dd")
	@ArgumentNames({ "sql", "*values" })
	public void callProcedure(String sql, String... parameters) {
		Object[] objParams = convertParams(parameters);

		service.executeProcedure(sql, objParams);
	}

	private Object[] convertParams(String[] parameters) {
		List<Object> params = new ArrayList<Object>();
		for (String string : parameters) {
			params.add(convertParam(string));
		}
		return params.toArray();
	}

	private Object convertParam(String param) {
		Pattern pattern = Pattern.compile("\\{(.)\\}(.*)$");
		Matcher matcher = pattern.matcher(param);
		if (matcher.matches()) {
			String type = matcher.group(1);
			String value = matcher.group(2);
			return convertParamToType(type, value);
		} else {
			return param;
		}
	}

	private Object convertParamToType(String type, String value) {
		switch (type) {
		case "i":
			return Integer.parseInt(value);
		case "f":
			return Double.parseDouble(value);
		case "d":
			try {
				return new SimpleDateFormat("yyyy-mm-dd").parse(value);
			} catch (ParseException e) {
				throw new FileLibraryException("Could not parse " + value + " as a date with format yyyy-mm-dd");
			}

		}
		return value;
	}

	@RobotKeyword("Close SQL session")
	public void disconnect() {
		service.disconnect();
	}




}
