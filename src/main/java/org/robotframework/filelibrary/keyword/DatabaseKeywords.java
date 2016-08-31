package org.robotframework.filelibrary.keyword;

import org.robotframework.filelibrary.service.DatabaseService;
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

	// @RobotKeyword("Execute the SQL select defined by a given _sqlId_")
	// @ArgumentNames({ "sql", "namedArgs" })
	// public Map<String, Object> executeQuery(String sql, String...
	// namedParameters) {
	// return service.executeQuery(sql, namedParameters);
	// }
	//
	// @RobotKeyword("Execute the SQL select defined by a given _sqlId_")
	// @ArgumentNames({"sql", "namedArgs"})
	// public Map<Object, Object> executeSQLFile(String filename, String...
	// namedParameters) {
	// return service.executeSQLFile(filename, namedParameters);
	// }
	//
	// @RobotKeyword("Execute the SQL select defined by a given _sqlId_")
	// @ArgumentNames("sqlId")
	// public List<Map> executeQueryAsList(String id) {
	// return getSession().selectList(id, null);
	// }

	@RobotKeyword("Close SQL session")
	public void disconnect() {
		service.disconnect();
	}

}
