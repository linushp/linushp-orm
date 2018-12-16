package com.github.linushp.orm.model;

import java.util.List;

public class WhereSqlAndArgs {
    public String whereSql;
    public Object[] whereArgs;
    public List<Object> whereArgsList;

    public WhereSqlAndArgs(String whereSql, List<Object> whereArgs) {
        this.whereSql = whereSql;
        this.whereArgs = whereArgs.toArray();
        this.whereArgsList = whereArgs;
    }

}