package org.robotframework.filelibrary.keyword;

import java.util.ArrayList;
import java.util.List;

import org.robotframework.filelibrary.FileLibraryException;
import org.robotframework.filelibrary.context.TemplateContext;
import org.robotframework.filelibrary.remote.RPCServer;
import org.robotframework.filelibrary.service.DatabaseService;
import org.robotframework.filelibrary.util.FileUtil;
import org.robotframework.filelibrary.util.StatementParser;
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

	// @RobotKeyword("Execute the SQL select defined by a given _sqlId_")
	// @ArgumentNames({ "sql", "namedArgs" })
	// public Map<String, Object> executeQuery(String sql, String...
	// namedParameters) {
	// return service.executeQuery(sql, namedParameters);
	// }
	//

	@RobotKeyword("Execute SQL statement(s). Specify either an SQL to execute or the path to a .sql file.")
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
			// TODO
			// service.executeStmt(parser.getStatement());
			throw new FileLibraryException("Not implemented");
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

}
