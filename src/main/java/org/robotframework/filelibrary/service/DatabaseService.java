package org.robotframework.filelibrary.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.robotframework.filelibrary.FileLibraryException;
import org.robotframework.filelibrary.context.TemplateContext;
import org.robotframework.filelibrary.util.StatementParser;
import org.robotframework.filelibrary.util.TextUtil;

public class DatabaseService {

	private Connection con;

	private static DatabaseService instance;

	private int queryTimeOut = 30;

	private DatabaseService() {

	}

	public static DatabaseService getInstance() {
		if (instance == null) {
			synchronized (DatabaseService.class) {
				if (instance == null) {
					instance = new DatabaseService();
				}
			}
		}
		return instance;
	}

	private Connection getConnection() {
		if (con == null) {
			throw new FileLibraryException("Not connected. Use connect keyword first...");
		}
		return con;
	}

	public void connect(String driver, String url, String user, String password) {

		try {
			Class.forName(driver);
			if (user == null || user.length() == 0) {
				con = DriverManager.getConnection(url);
			} else {
				con = DriverManager.getConnection(url, user, password);
			}
			if (con.isValid(1)) {
				System.out.println("Connected to database " + url);
			}
		} catch (Exception e) {
			throw new FileLibraryException(e);
		}
	}

	public void disconnect() {
		try {
			Connection con = getConnection();
			if (con != null && !con.isClosed()) {
				con.close();
				System.out.println("Connection closed.");
			}
		} catch (SQLException e) {
			throw new FileLibraryException(e);
		}
	}

	public List<Map<String, Object>> DELETEexecuteQuery(String sql) {
		return DELETEexecuteQuery(sql, 0);
	}

	public List<Map<String, Object>> executeQuery(String sql, String[] parameters, int maxResults) {
		try {
			Connection con = getConnection();

			StatementParser parser = new StatementParser(sql);
			PreparedStatement stmt = con.prepareStatement(parser.getStatement());

			int i = 1;
			for (String param : parameters) {
				System.out.println("Setting sql param " + i + " to '" + param + "'");
				stmt.setString(i, param);
				i++;
			}

			System.out.println("Executing stmt: \n" + parser.getStatement());
			stmt.setQueryTimeout(queryTimeOut);
			stmt.setMaxRows(maxResults);
			long start = System.currentTimeMillis();
			ResultSet rs = stmt.executeQuery();
			System.out.println("" + (System.currentTimeMillis() - start) + " ms to execute query.");
			return toMap(rs);
		} catch (SQLException e) {
			throw new FileLibraryException(e);
		}
	}

	public List<Map<String, Object>> DELETEexecuteQuery(String sql, int maxResults) {
		try {
			Connection con = getConnection();

			StatementParser parser = new StatementParser(sql);
			PreparedStatement stmt = con.prepareStatement(parser.getStatement());

			int i = 1;
			for (String param : parser.getParameters()) {

				Object v = TemplateContext.getInstance().getValue(param);
				if (v == null) {
					throw new FileLibraryException("No value found for parameter '" + param + "'.");
				}
				String paramValue = v.toString();
				System.out.println("Setting sql param '" + param + "' to '" + paramValue + "'");
				stmt.setString(i, paramValue);
				i++;
			}
			System.out.println("Executing stmt: \n" + parser.getStatement());
			stmt.setQueryTimeout(queryTimeOut);
			stmt.setMaxRows(maxResults);
			long start = System.currentTimeMillis();
			ResultSet rs = stmt.executeQuery();
			System.out.println("" + (System.currentTimeMillis() - start) + " ms to execute query.");
			return toMap(rs);
		} catch (SQLException e) {
			throw new FileLibraryException(e);
		}
	}

	/**
	 * Convert a SQL result set to a List of HashMaps. NULL values are replaced
	 * with "".
	 * 
	 */
	private List<Map<String, Object>> toMap(ResultSet rs) throws SQLException {
		List<Map<String, Object>> results = new ArrayList<>();

		ResultSetMetaData metadata = rs.getMetaData();
		int columns = metadata.getColumnCount();
		String[] names = new String[columns];
		int[] types = new int[columns];

		for (int i = 0; i < columns; i++) {
			names[i] = metadata.getColumnName(i + 1).toLowerCase();
			types[i] = metadata.getColumnType(i + 1);
		}

		while (rs.next()) {

			Map<String, Object> row = new HashMap<>();
			results.add(row);

			for (int i = 0; i < columns; i++) {

				Object value = null;

				value = rs.getString(i + 1);
				if (value == null) {
					value = "";
				}
				row.put(names[i], value);
			}

		}

		return results;
	}

	public void setQueryTimeOut(int timeOut) {
		this.queryTimeOut = timeOut;
		System.out.println("Query timeout set to " + timeOut + " seconds.");
	}

	public void executeStatement(String statement, String[] parameters) {

		try {
			Connection con = getConnection();
			PreparedStatement stmt = con.prepareStatement(statement);

			System.out.println("Executing stmt: \n" + statement);

			int i = 1;
			for (String paramValue : parameters) {
				System.out.println("Setting sql param " + i + " to '" + paramValue + "'");
				stmt.setString(i, paramValue);
				i++;
			}

			stmt.setQueryTimeout(queryTimeOut);

			long start = System.currentTimeMillis();
			stmt.execute();
			System.out.println("" + (System.currentTimeMillis() - start) + " ms to execute sql.");

		} catch (SQLException e) {
			throw new FileLibraryException(e);
		}

	}

	public void verifyQueryResults(String statement, String[] parameters, String[] expectedResults) {

		try {
			Connection con = getConnection();
			PreparedStatement stmt = con.prepareStatement(statement);

			System.out.println("Executing stmt: \n" + statement);

			int i = 1;
			for (String paramValue : parameters) {
				System.out.println("Setting sql param '" + i + "' to '" + paramValue + "'");
				stmt.setString(i, paramValue);
				i++;
			}
			stmt.setQueryTimeout(queryTimeOut);
			stmt.setMaxRows(1);
			long start = System.currentTimeMillis();
			ResultSet rs = stmt.executeQuery();
			System.out.println("" + (System.currentTimeMillis() - start) + " ms to execute query.");

			if (rs.getMetaData().getColumnCount() < expectedResults.length) {
				throw new FileLibraryException(
						"Expected " + expectedResults.length + "values, but only received " + rs.getMetaData().getColumnCount());
			}

			rs.next();
			for (i = 0; i < expectedResults.length; i++) {
				String expected = expectedResults[i];
				String actual = rs.getString(i + 1);
				if (!TextUtil.matches(actual, expected)) {
					throw new FileLibraryException("Expected '" + expected + "' but received '" + actual + "'.");
				} else {
					System.out.println("Expected '" + expected + "' and received '" + actual + "'.");
				}
			}

		} catch (SQLException e) {
			throw new FileLibraryException(e);
		}

	}

}
