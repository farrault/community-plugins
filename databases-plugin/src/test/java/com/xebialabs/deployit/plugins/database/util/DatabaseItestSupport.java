package com.xebialabs.deployit.plugins.database.util;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseItestSupport {

	public static int findNumberOfRowsInTable(String tableName, SqlQueryRunner.DatabaseType databaseType) {
		SqlQueryRunner sqlRunner = new SqlQueryRunner(SqlQueryRunner.DatabaseType.ORACLE);
		return sqlRunner.executeQuery("select count(*) from " + tableName, new ResultSetHandler<Integer>() {

			@Override
			public Integer handleResultSet(ResultSet rs) throws SQLException {
				while (rs.next()) {
					return rs.getInt(1);
				}
				return 0;
			}
		});
	}

}
