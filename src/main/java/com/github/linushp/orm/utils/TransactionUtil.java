package com.github.linushp.orm.utils;

import com.github.linushp.orm.model.SqlSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionUtil {

    private static ThreadLocal<SqlSession> localSqlSession = new ThreadLocal<>();
    private static ThreadLocal<AtomicInteger> localAtomicInteger = new ThreadLocal<>();


    public static void beginTransaction(Connection connection) throws SQLException {

        AtomicInteger atomicInteger = getAtomicInteger();
        if (atomicInteger.get() == 0) {
            connection.setAutoCommit(false);
            localSqlSession.set(new SqlSession(connection, false));
            atomicInteger.incrementAndGet();
        } else {
            atomicInteger.incrementAndGet();
        }

    }


    public static void rollbackTransaction() throws SQLException {
        AtomicInteger atomicInteger = getAtomicInteger();
        if (atomicInteger.get() > 1) {
            return;
        }

        SqlSession sqlConnection = localSqlSession.get();
        Connection connection = sqlConnection.getConnection();
        connection.rollback();
    }

    public static void commitTransaction() throws SQLException {
        AtomicInteger atomicInteger = getAtomicInteger();
        if (atomicInteger.get() > 1) {
            return;
        }

        SqlSession sqlConnection = localSqlSession.get();
        Connection connection = sqlConnection.getConnection();
        connection.commit();
    }


    public static void endTransaction() throws SQLException {
        AtomicInteger atomicInteger = getAtomicInteger();
        int atomicIntegerValue = atomicInteger.decrementAndGet();

        if (atomicIntegerValue == 0) {
            SqlSession sqlConnection = localSqlSession.get();
            Connection connection = sqlConnection.getConnection();
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            localSqlSession.set(null);
            localAtomicInteger.set(null);
        }

    }


    private static AtomicInteger getAtomicInteger() {
        AtomicInteger atomicInteger = localAtomicInteger.get();
        if (atomicInteger == null) {
            atomicInteger = new AtomicInteger(0);
            localAtomicInteger.set(atomicInteger);
        }
        return atomicInteger;
    }


    public static SqlSession getSqlSession() {
        return localSqlSession.get();
    }


    public static Object doTransaction(Connection connection, SqlRunner sqlRunner) throws SQLException {
        Object result = null;
        try {
            beginTransaction(connection);
            result = sqlRunner.doRun();
            commitTransaction();
        } catch (Exception e) {
            rollbackTransaction();
        } finally {
            endTransaction();
        }
        return result;
    }

}
