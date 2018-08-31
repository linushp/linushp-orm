package com.github.linushp.orm.model;

import java.util.ArrayList;
import java.util.List;

public class UpdateResult {

    private Object generatedKey = null;
    private List<Object> generatedKeys = new ArrayList<>();

    private int affectedRows = 0;

    private String errMsg = null;

    public UpdateResult() {
    }

    public UpdateResult(String errMsg) {
        this.errMsg = errMsg;
    }


    public List<Object> getGeneratedKeys() {
        return generatedKeys;
    }

    public void setGeneratedKeys(List<Object> generatedKeys) {
        this.generatedKeys = generatedKeys;
    }

    public int getAffectedRows() {
        return affectedRows;
    }

    public void setAffectedRows(int affectedRows) {
        this.affectedRows = affectedRows;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public Object getGeneratedKey() {
        return generatedKey;
    }

    public void setGeneratedKey(Object generatedKey) {
        this.generatedKey = generatedKey;
    }
}
