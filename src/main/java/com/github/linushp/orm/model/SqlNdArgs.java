package com.github.linushp.orm.model;


public class SqlNdArgs {
    private String sql;
    private Object[] args;

    public SqlNdArgs(String sql, Object[] args) {
        this.sql = sql;
        this.args = args;
    }


    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
