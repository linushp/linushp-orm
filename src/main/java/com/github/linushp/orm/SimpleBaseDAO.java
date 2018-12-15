package com.github.linushp.orm;


import com.github.linushp.orm.model.EntityInfo;

import javax.sql.DataSource;


public abstract class SimpleBaseDAO<T> extends DataAccessObject<T> {

    public SimpleBaseDAO(Class<T> clazz) {
        super(clazz);
        this.dataAccess = new DataAccess(new DynamicConnectionFactory(this));
        this.settingDaoByEntityInfoAnnotation(clazz);
    }


    protected void settingDaoByEntityInfoAnnotation(Class<T> clazz) {
        EntityInfo entityInfo = clazz.getAnnotation(EntityInfo.class);
        if (entityInfo != null) {
            this.tableName = entityInfo.table();
            this.selectFields = entityInfo.selectFields();
            this.schemaName = entityInfo.schemaName();
            this.idFieldName = entityInfo.idFieldName();
            this.isIgnoreNull = entityInfo.isIgnoreNull();
            this.isUnderlineKey = entityInfo.isUnderlineKey();
        }
    }


    protected abstract DataSource getDataSourceByName(String dataSourceName);

}
