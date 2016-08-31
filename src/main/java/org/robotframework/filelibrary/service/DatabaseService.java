package org.robotframework.filelibrary.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.robotframework.filelibrary.FileLibraryException;
import org.robotframework.filelibrary.context.TemplateContext;
import org.robotframework.filelibrary.util.StatementParser;

public class DatabaseService {

	private Connection con;

	private static DatabaseService instance;

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
				System.out.println("Connected to database.");
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

	public List<Map<String, Object>> executeQuery(String sql) {
		try {
			Connection con = getConnection();

			StatementParser parser = new StatementParser(sql);

			PreparedStatement stmt = con.prepareStatement(parser.getStatement());
			ParameterMetaData metadata = stmt.getParameterMetaData();
			List<String> params = parser.getParameters();
			int i = 1;
			for (String param : params) {

				String paramValue = TemplateContext.getInstance().getValue(param).toString();
				switch (metadata.getParameterType(i)) {

				case Types.NUMERIC:
				case Types.DECIMAL:
					stmt.setBigDecimal(i, new BigDecimal(paramValue));
					break;
				case Types.BIT:
					stmt.setBoolean(i, Boolean.parseBoolean(paramValue));
					break;
				case Types.TINYINT:
					stmt.setByte(i, Byte.valueOf(paramValue));
					break;
				case Types.SMALLINT:
					stmt.setShort(i, Short.valueOf(paramValue));
					break;
				case Types.INTEGER:
					stmt.setInt(i, Integer.valueOf(paramValue));
					break;
				case Types.BIGINT:
					stmt.setLong(i, Long.valueOf(paramValue));
					break;
				case Types.REAL:
				case Types.FLOAT:
					stmt.setFloat(i, Float.valueOf(paramValue));
					break;
				case Types.DOUBLE:
					stmt.setDouble(i, Double.valueOf(paramValue));
					break;
				case Types.BINARY:
				case Types.VARBINARY:
				case Types.LONGVARBINARY:
					stmt.setBytes(i, paramValue.getBytes());
				case Types.DATE:
					stmt.setDate(i, java.sql.Date.valueOf(paramValue));
					break;
				case Types.TIME:
					stmt.setTime(i, Time.valueOf(paramValue));
					break;
				case Types.TIMESTAMP:
					stmt.setTimestamp(i, Timestamp.valueOf(paramValue));
					break;
				default:
					stmt.setString(i, paramValue);
					break;
				}
				i++;
			}
			ResultSet rs = stmt.executeQuery();
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

				switch (types[i]) {
				// TODO add more native types
				case Types.DATE:
				case Types.TIME:
				case Types.TIMESTAMP:
					value = new Date(rs.getTimestamp(i + 1).getTime());
					break;
				case Types.INTEGER:
					value = Integer.valueOf(rs.getInt(i + 1));
					break;

				default:
					value = rs.getString(i + 1);
				}

				if (value == null) {
					value = "";
				}
				row.put(names[i], value);
			}

		}

		return results;
	}
}
