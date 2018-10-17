package com.github.linushp.orm;

import com.github.linushp.orm.model.SimpleDaoInfo;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DynamicConnectionFactory implements ConnectionFactory {
    private SimpleBaseDAO tSimpleBaseDAO;

    public DynamicConnectionFactory(SimpleBaseDAO tSimpleBaseDAO) {
        this.tSimpleBaseDAO = tSimpleBaseDAO;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Class clazz = this.tSimpleBaseDAO.getClass();
        SimpleDaoInfo simpleDaoInfo = (SimpleDaoInfo) clazz.getAnnotation(SimpleDaoInfo.class);
        String dataSourceName = null;
        if (null != simpleDaoInfo) {
            dataSourceName = simpleDaoInfo.dataSourceName();
        }
        DataSource dataSource = this.tSimpleBaseDAO.getDataSourceByName(dataSourceName);
        return dataSource.getConnection();
    }
}
