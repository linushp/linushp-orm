package com.github.linushp.orm;

import com.github.linushp.commons.CollectionUtils;
import com.github.linushp.orm.model.DataModifyListener;
import com.github.linushp.orm.model.SqlNdArgs;
import com.github.linushp.orm.model.SqlSession;
import com.github.linushp.orm.model.UpdateResult;
import com.github.linushp.orm.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataAccess.class);

    private ConnectionFactory connectionFactory;
    private ResultSetParser<?> resultSetParser = null;
    private DataModifyListener dataModifyListener = null;

    public DataAccess(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public DataAccess() {
    }

    public ConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }


    public void setDataModifyListener(DataModifyListener dataModifyListener) {
        this.dataModifyListener = dataModifyListener;
    }


    private void emitBeforeUpdateEvent(String sql, Object[] args) throws Exception {
        if (this.dataModifyListener != null) {
            this.dataModifyListener.onBeforeDataModify(sql, args);
        }
    }

    private void emitAfterUpdateEvent(String sql, Object[] args, UpdateResult updateResult) throws Exception {
        if (this.dataModifyListener != null) {
            this.dataModifyListener.onAfterDataModify(sql, args, updateResult);
        }
    }


    public UpdateResult update(String sql, List<Object> args) throws Exception {
        Object[] objects = args.toArray(new Object[args.size()]);
        return update(sql, objects);
    }

    // INSERT, UPDATE, DELETE 操作都可以包含在其中
    public UpdateResult update(String sql, Object... args) throws Exception {
        emitBeforeUpdateEvent(sql, args);


        SqlNdArgs sqlNdArgs = parseSqlNdArgs(sql, args);
        sql = sqlNdArgs.getSql();
        args = sqlNdArgs.getArgs();


        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet generatedKeyResultSet = null;
        UpdateResult updateResult = new UpdateResult();

        SqlSession sqlSession = getSqlSession();


        try {
            LOGGER.info("update sql : " + sql);
            connection = sqlSession.getConnection();

            preparedStatement = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);

            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }

            int affectedRows = preparedStatement.executeUpdate();
            updateResult.setAffectedRows(affectedRows);


            generatedKeyResultSet = preparedStatement.getGeneratedKeys();
            List<Map<String, ?>> mapList = ResultSetUtils.resultSetToMapList(generatedKeyResultSet);
            if (mapList != null && !mapList.isEmpty()) {
                for (Map<String, ?> map : mapList) {
                    Object generatedKey = map.get("GENERATED_KEY");
                    if (generatedKey != null) {
                        updateResult.getGeneratedKeys().add(generatedKey);
                        updateResult.setGeneratedKey(generatedKey);
                    }
                }
            }


            emitAfterUpdateEvent(sql, args, updateResult);

            return updateResult;

        } catch (Exception e) {
            throw e;
        } finally {
            release(generatedKeyResultSet, preparedStatement, sqlSession);
        }
    }


    public <E> E queryValue(String sql, List<Object> args) throws Exception {
        Object[] objects = args.toArray(new Object[args.size()]);
        return queryValue(sql, objects);
    }


    /**
     * 返回某条记录的某一个字段的值 或 一个统计的值(一共有多少条记录等.)
     *
     * @param sql
     * @param args
     * @param <E>
     * @return
     */
    public <E> E queryValue(String sql, Object... args) throws Exception {

        SqlNdArgs sqlNdArgs = parseSqlNdArgs(sql, args);
        sql = sqlNdArgs.getSql();
        args = sqlNdArgs.getArgs();


        LOGGER.info("query sql : " + sql);

        //1. 得到结果集: 该结果集应该只有一行, 且只有一列
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        SqlSession sqlSession = getSqlSession();
        try {
            //1. 得到结果集
            connection = sqlSession.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return (E) resultSet.getObject(1);
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            release(resultSet, preparedStatement, sqlSession);
        }

        //2. 取得结果
        return null;
    }


    public <T> T queryObject(Class<T> clazz, String sql, List<Object> args) throws Exception {
        Object[] objects = args.toArray(new Object[args.size()]);
        return queryObject(clazz, sql, objects);
    }


    // 查询一条记录, 返回对应的对象
    public <T> T queryObject(Class<T> clazz, String sql, Object... args) throws Exception {
        List<T> result = query(clazz, sql, args);
        return CollectionUtils.getFirstElement(result);
    }


    public <T> List<T> query(Class<T> clazz, String sql, List<Object> args) throws Exception {
        Object[] objects = args.toArray(new Object[args.size()]);
        return query(clazz, sql, objects);
    }

    /**
     * 传入 SQL 语句， 返回 SQL 语句查询到的记录对应的 Map对象的集合
     *
     * @param clazz 可以为Null
     * @param sql
     * @param args
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> List<T> query(Class<T> clazz, String sql, Object... args) throws Exception {

        SqlNdArgs sqlNdArgs = parseSqlNdArgs(sql, args);
        sql = sqlNdArgs.getSql();
        args = sqlNdArgs.getArgs();


        List<T> list;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        LOGGER.info("query sql : " + sql);

        SqlSession sqlSession = getSqlSession();

        try {
            //1. 得到结果集
            connection = sqlSession.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }

            resultSet = preparedStatement.executeQuery();

            //2. 转换成List<Map>
            list = resultSetToObjectList(clazz, resultSet);

        } catch (Exception e) {
            throw e;
        } finally {
            release(resultSet, preparedStatement, sqlSession);
        }

        return list;
    }


    private <T> List<T> resultSetToObjectList(Class<T> clazz, ResultSet resultSet) throws Exception {

        if (resultSet == null) {
            return new ArrayList<>(0);
        }

        ResultSetParser<?> resultSetParser;
        if (this.resultSetParser != null) {
            resultSetParser = this.resultSetParser;
        } else {
            resultSetParser = new DefaultResultSetParser(clazz);
        }
        return (List<T>) resultSetParser.parseResultSet(resultSet);
    }


    /**
     * 传入 SQL 语句， 返回 SQL 语句查询到的记录对应的 Map对象的集合
     *
     * @param sql
     * @return
     * @throws Exception
     */
    public List<Map<String, ?>> queryTemp(String sql) throws Exception {
        List<Map<String, ?>> list;
        Connection connection = null;
        Statement preparedStatement = null;
        ResultSet resultSet = null;

        LOGGER.info("query sql : " + sql);
        SqlSession sqlSession = getSqlSession();

        try {
            //1. 得到结果集
            connection = sqlSession.getConnection();
            preparedStatement = connection.createStatement();
            resultSet = preparedStatement.executeQuery(sql);

            //2. 转换成List<Map>
            list = ResultSetUtils.resultSetToMapList(resultSet);

        } catch (Exception e) {
            throw e;
        } finally {
            release(resultSet, preparedStatement, sqlSession);
        }

        return list;
    }


    public SqlSession getSqlSession() throws Exception {

        SqlSession connection1 = TransactionUtil.getSqlSession();
        if (connection1 != null) {
            return connection1;
        }

        Connection connection = getConnectionFactory().getConnection();
        return new SqlSession(connection, true);
    }


    public SqlNdArgs parseSqlNdArgs(String sql, Object... args) {
        //允许第一个参数传递过来一个Map，如果第一个参数是一个map，后面其他参数均忽略
        if (args.length > 0 && args[0] instanceof Map) {
            Map map = (Map) args[0];
            return SQLFormatUtils.formatSQLAndArgs(sql, map);
        }
        return new SqlNdArgs(sql, args);
    }


    public void release(ResultSet resultSet, Statement statement, SqlSession connection) throws SQLException {

        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOGGER.info("ResultSet close error");
            }
        }

        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                LOGGER.info("Statement close error");
            }
        }

        if (connection != null) {
            if (connection.isAutoClose()) {
                try {
                    connection.getConnection().close();
                } catch (SQLException e) {
                    LOGGER.info("Connection close error");
                }
            }
        }
    }

    public void setResultSetParser(ResultSetParser<?> resultSetParser) {
        this.resultSetParser = resultSetParser;
    }

}