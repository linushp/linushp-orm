package com.github.linushp.orm;

import com.github.linushp.commons.CastBasicTypeUtils;
import com.github.linushp.orm.model.UpdateResult;
import com.github.linushp.orm.utils.ResultSetParser;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


/**
 * 不固定表名
 *
 * @param <T>
 */
public abstract class DataAccessAbstract<T> implements ResultSetParser<T> {
    protected DataAccess dataAccess;

    public DataAccessAbstract(ConnectionFactory connectionFactory) {
        this.dataAccess = new DataAccess(connectionFactory);
        this.dataAccess.setResultSetParser(this);
    }

    /**
     * update , insert , delete 都是调用此方法
     *
     * @param sql
     * @param args
     * @return
     * @throws Exception
     */
    public UpdateResult update(String sql, Object... args) throws Exception {
        return dataAccess.update(sql, args);
    }

    public T queryObject(String sql, Object... args) throws Exception {
        return dataAccess.queryObject(null, sql, args);
    }

    public List<T> queryObjects(String sql, Object... args) throws Exception {
        return dataAccess.query(null, sql, args);
    }

    public long queryValue(String sql, Object... args) throws Exception {
        Object x = dataAccess.queryValue(sql, args);
        return CastBasicTypeUtils.toLong(x);
    }

    @Override
    public List<T> parseResultSet(ResultSet resultSet) throws Exception {
        List<T> result = new ArrayList<>();
        while (resultSet.next()) {
            T obj = this.resultSetToObject(resultSet);
            result.add(obj);
        }
        return result;
    }

    abstract protected T resultSetToObject(ResultSet resultSet);
}
