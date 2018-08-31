package com.github.linushp.orm.utils;


import java.sql.ResultSet;
import java.util.List;


public class DefaultResultSetParser<T> implements ResultSetParser<T> {

    public Class<T> clazz;


    public DefaultResultSetParser(Class<T> clazz) {
        if (clazz == null) {
            throw new NullPointerException();
        }
        this.clazz = clazz;
    }


    @Override
    public List<T> parseResultSet(ResultSet resultSet) throws Exception {
        return ResultSetUtils.resultSetToEntityList(resultSet, this.clazz);
//        List<Map<String, ?>> mapList = ResultSetUtils.resultSetToMapList(resultSet);
//        return BeanUtils.mapListToBeanList(this.clazz, mapList);
    }
}
