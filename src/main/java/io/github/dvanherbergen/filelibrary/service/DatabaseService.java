package io.github.dvanherbergen.filelibrary.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.operation.DatabaseOperation;

import io.github.dvanherbergen.filelibrary.FileLibraryException;
import io.github.dvanherbergen.filelibrary.util.StatementParser;
import io.github.dvanherbergen.filelibrary.util.TextUtil;

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
			if (con != null && !con.isClosed()) {
				con.close();
				System.out.println("Connection closed.");
			}
		} catch (SQLException e) {
			throw new FileLibraryException(e);
		}
	}

	public List<Map<String, Object>> getQueryResultsAsMap(String sql, String[] parameters, int maxResults) {
		try {
			return toMap(getQueryResults(sql, parameters, maxResults));
		} catch (SQLException e) {
			throw new FileLibraryException(e);
		}
	}

	public List<Object> getQueryResultsAsList(String sql, String[] parameters, int maxResults) {
		try {

			List<Object> results = new ArrayList<>();
			ResultSet rs = getQueryResults(sql, parameters, maxResults);

			ResultSetMetaData metadata = rs.getMetaData();
			int columns = metadata.getColumnCount();

			while (rs.next()) {

				List<String> columnValues = new ArrayList<>();

				for (int i = 0; i < columns; i++) {

					String value = rs.getString(i + 1);
					if (value == null) {
						value = "";
					}
					columnValues.add(value);
				}

				if (columnValues.size() == 1) {
					results.add(columnValues.get(0));
				} else {
					results.add(columnValues);
				}
			}
			return results;

		} catch (SQLException e) {
			throw new FileLibraryException(e);
		}
	}

	private ResultSet getQueryResults(String sql, String[] parameters, int maxResults) {
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
			return rs;
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

	public void executeProcedure(String statement, Object[] parameters) {
		try {
			Connection con = getConnection();
			CallableStatement stmt = con.prepareCall(statement);

			System.out.println("Executing stmt: \n" + statement);

			int i = 1;
			for (Object paramValue : parameters) {
				System.out.println("Setting sql param '" + i + "' to '" + paramValue + "' + type " + paramValue.getClass().getName());
				if (paramValue instanceof String) {
					stmt.setString(i, (String) paramValue);
				} else if (paramValue instanceof Integer) {
					stmt.setInt(i, (Integer) paramValue);
				} else if (paramValue instanceof Double) {
					stmt.setDouble(i, (Double) paramValue);
				} else if (paramValue instanceof Date) {
					Date utilDate = (Date) paramValue;
					stmt.setDate(i, new java.sql.Date(utilDate.getTime()));
				} else {
					stmt.setString(i, (String) paramValue);
				}
				i++;
			}
			stmt.setQueryTimeout(queryTimeOut);
			long start = System.currentTimeMillis();
			Boolean result = stmt.execute();
			System.out.println("" + (System.currentTimeMillis() - start) + " ms to execute query.");
			if (result) {
				throw new FileLibraryException(
						"Expected no result from calling procedure yet got a result set. Is your statement really a {call ... } statement?");
			}

		} catch (SQLException e) {
			throw new FileLibraryException(e);
		}
	}

	/**
	 * Load an XLS file into the database using DBUnit.
	 * @param path
	 */
	public void loadFromXls(String xlsFilePath, String schema, boolean clear) {
		try {

			DatabaseConnection connection = new DatabaseConnection(con);
			DatabaseConfig config = connection.getConfig();
			config.setProperty("http://www.dbunit.org/properties/escapePattern", "\"?\"");
			config.setProperty("http://www.dbunit.org/features/batchedStatements", "true");
			config.setProperty("http://www.dbunit.org/properties/datatypeFactory", new org.dbunit.ext.mysql.MySqlDataTypeFactory());

			if (StringUtils.isNotBlank(schema)) {
				executeStatement("SET SCHEMA " + schema, new String[] {});
			}

			File data = new File(xlsFilePath);
			if (!data.exists() || ! data.canRead()) {
				throw new FileLibraryException("Cannot read " + data.getAbsolutePath());
			}
			
			IDataSet dataSet = new XlsDataSet(new FileInputStream(data));
			if (dataSet != null) {
				con.setAutoCommit(true);
				if (clear) {
					DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
				} else {
					DatabaseOperation.REFRESH.execute(connection, dataSet);
				}
				con.setAutoCommit(false);
			}

		} catch (DatabaseUnitException | SQLException | IOException e) {
			throw new FileLibraryException("Error loading data from " + xlsFilePath + " : " + e.getClass().getSimpleName() + " : " + e.getMessage(), e);
		}
	}
}
