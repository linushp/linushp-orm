package com.github.linushp.orm;


import com.github.linushp.orm.model.EntityInfo;

import javax.sql.DataSource;


public abstract class SimpleBaseDAO<T> extends DataAccessObject<T> {

    public SimpleBaseDAO(Class<T> clazz) {
        super(clazz, getTableNameByEntityClass(clazz));
        this.dataAccess = new DataAccess(new DynamicConnectionFactory(this));
    }

    protected abstract DataSource getDataSourceByName(String dataSourceName);

    private static String getTableNameByEntityClass(Class clazz) {
        EntityInfo entityInfo = (EntityInfo) clazz.getAnnotation(EntityInfo.class);
        String tableName = entityInfo.table();
        return tableName;
    }

}
