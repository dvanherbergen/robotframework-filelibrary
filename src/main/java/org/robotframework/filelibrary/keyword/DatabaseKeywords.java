package org.robotframework.filelibrary.keyword;

import org.robotframework.filelibrary.FileLibraryException;
import org.robotframework.filelibrary.remote.RPCServer;
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

	@RobotKeyword("Set default query timeout.")
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
