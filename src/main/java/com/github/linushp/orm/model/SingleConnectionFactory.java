package com.github.linushp.orm.model;


import com.github.linushp.orm.ConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class SingleConnectionFactory implements ConnectionFactory {

    private Connection connection;

    public SingleConnectionFactory(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.connection;
    }
}
