package com.xebialabs.deployit.plugins.database.util;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class SqlQueryRunner {
	public enum DatabaseType {
		ORACLE, MYSQL, DB2;
	}

	private static final Properties databaseProperties;
	private String databaseTypePrefix;

	static {
		databaseProperties = new Properties();
		URL propertiesUrl = Thread.currentThread().getContextClassLoader().getResource("databases.properties");
		try {
			databaseProperties.load(propertiesUrl.openStream());
		} catch (IOException e) {
			throw new RuntimeException("Couldn't load databases.properties from classpath", e);
		}
	}

	public SqlQueryRunner(DatabaseType databaseType) {
		switch (databaseType) {
		case ORACLE:
			databaseTypePrefix = "oracle";
			break;
		case MYSQL:
			databaseTypePrefix = "mysql";
			break;
		case DB2:
			databaseTypePrefix = "db2";
			break;
		default:
			throw new RuntimeException("Incorrect database type:" + databaseType);
		}
	}

	public <T> T executeQuery(String sql, ResultSetHandler<T> handler) {
		Connection connection = getConnection();
		ResultSet rs = null;
		try {
			Statement statement = connection.createStatement();
			rs = statement.executeQuery(sql);
			return handler.handleResultSet(rs);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				throw new RuntimeException("Couldn't close the connection successfully");
			}
		}
	}

	private Connection getConnection() {
		try {
			Class.forName(getJdbcDriverClassName()).newInstance();
			Connection conn = DriverManager.getConnection(getJdbcUrl(), getUsername(), getPassword());
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Couldn't get jdbc connection for connection options " + this);
		}
	}

	public String getJdbcDriverClassName() {
		return databaseProperties.getProperty(databaseTypePrefix + ".database.driver");
	}

	public String getJdbcUrl() {
		return databaseProperties.getProperty(databaseTypePrefix + ".database.url");
	}

	public String getUsername() {
		return databaseProperties.getProperty(databaseTypePrefix + ".database.username");
	}

	public String getPassword() {
		return databaseProperties.getProperty(databaseTypePrefix + ".database.password");
	}

	@Override
	public String toString() {
		return "{jdbcDriverClassName:" + getJdbcDriverClassName() + ", url:" + getJdbcUrl() + ", username:" + getUsername() + ", password:" + getPassword()
		        + "}";
	}

}
