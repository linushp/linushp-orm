package com.github.linushp.orm.model;

import java.util.ArrayList;
import java.util.List;


public class WhereSqlBuilder {

    private StringBuilder whereSql = new StringBuilder(" where 1=1 ");
    private List<Object> whereSqlArgs = new ArrayList<>();


    public WhereSqlBuilder append(String sql, Object... args) {
        whereSql.append(" ");
        whereSql.append(sql);
        whereSql.append(" ");


        if (args != null && args.length > 0) {
            for (Object arg : args) {
                whereSqlArgs.add(arg);
            }
        }

        return this;
    }


    public WhereSqlAndArgs toWhereSqlAndArgs() {
        String sql = this.whereSql.toString();
        return new WhereSqlAndArgs(sql, whereSqlArgs);
    }

    public String getWhereSqlString(){
        return this.whereSql.toString();
    }

    public Object[] getWhereArgsArray(){
        return whereSqlArgs.toArray();
    }

}
