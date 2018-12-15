package com.github.linushp.orm.model;

import com.github.linushp.commons.CollectionUtils;

import java.util.ArrayList;
import java.util.List;


public class WhereSqlBuilder {

    private StringBuilder whereSql = new StringBuilder(" where 1=1 ");
    private List<Object> whereSqlArgs = new ArrayList<>();


    public WhereSqlBuilder append(String sql, Object... args) {
        whereSql.append(" ");
        whereSql.append(sql);
        whereSql.append(" ");
        List ss = CollectionUtils.toObjectList(args);
        whereSqlArgs.addAll(ss);
        return this;
    }


    public WhereSqlAndArgs toWhereSqlAndArgs() {
        String sql = this.whereSql.toString();
        return new WhereSqlAndArgs(sql, whereSqlArgs);
    }

}
