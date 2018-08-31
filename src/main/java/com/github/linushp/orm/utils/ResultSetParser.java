package com.github.linushp.orm.utils;

import java.sql.ResultSet;
import java.util.List;

public interface ResultSetParser<T> {
    List<T> parseResultSet(ResultSet resultSet) throws Exception;
}
