package org.robotframework.filelibrary.keyword;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.robotframework.filelibrary.FileLibraryException;
import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;

@RobotKeywords
public class DatabaseKeywords {

	private static SqlSession session;

	@RobotKeyword("Connect to database specified in the given iBatis config")
	@ArgumentNames("path")
	public void connect(String path) {
		try {
			InputStream inputStream = Resources.getResourceAsStream(path);
			SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
			session = sessionFactory.openSession();
		} catch (Exception e) {
			throw new FileLibraryException(e);
		}
	}

	@RobotKeyword("Execute the SQL select defined by a given _sqlId_")
	@ArgumentNames("sqlId")
	public Map<Object, Object> executeQuery(String id) {
		return getSession().selectMap(id, null);
	}

	@RobotKeyword("Execute the SQL select defined by a given _sqlId_")
	@ArgumentNames("sqlId")
	public List<Map> executeQueryAsList(String id) {
		return getSession().selectList(id, null);
	}

	@RobotKeyword("Close SQL session")
	public void disconnect() {
		if (session != null) {
			session.close();
			session = null;
		}
	}

	private SqlSession getSession() {
		if (session == null) {
			throw new FileLibraryException("No Session was open. Use the 'Connect' keyword to open a session first.");
		}
		return session;
	}
}
