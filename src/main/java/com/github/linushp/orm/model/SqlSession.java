package com.github.linushp.orm.model;

import java.sql.Connection;

public class SqlSession {
    private boolean autoClose;
    private Connection connection;

    public SqlSession(Connection connection, boolean autoClose) {
        this.autoClose = autoClose;
        this.connection = connection;
    }

    public boolean isAutoClose() {
        return autoClose;
    }

    public void setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
