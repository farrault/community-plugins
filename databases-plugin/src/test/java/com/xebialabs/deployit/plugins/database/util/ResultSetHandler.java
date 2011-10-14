package com.xebialabs.deployit.plugins.database.util;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetHandler<T> {

	T handleResultSet(ResultSet rs) throws SQLException;

}
