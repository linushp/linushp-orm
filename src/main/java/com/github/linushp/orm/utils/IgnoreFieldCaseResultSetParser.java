package com.github.linushp.orm.utils;

import com.github.linushp.commons.BeanField;
import com.github.linushp.commons.BeanUtils;
import com.github.linushp.commons.ifs.MapToBeanFilter;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 将ResultSet转换成List对象，忽略字段的大小写
 *
 * @param <T>
 */
public class IgnoreFieldCaseResultSetParser<T> implements ResultSetParser<T> {

    private IgnoreFieldCaseMapToBeanFilter ignoreFieldCaseMapToBeanFilter;
    private Class<T> clazz;

    public IgnoreFieldCaseResultSetParser(Class clazz) {
        this.clazz = clazz;
        this.ignoreFieldCaseMapToBeanFilter = new IgnoreFieldCaseMapToBeanFilter();
    }

    @Override
    public List<T> parseResultSet(ResultSet resultSet) throws Exception {
        List<Map<String, ?>> mapList = ResultSetUtils.resultSetToMapList(resultSet, true);
        List<T> resultList = new ArrayList<>();

        for (Map<String, ?> map : mapList) {
            T bean = BeanUtils.mapToBean(this.clazz, map, this.ignoreFieldCaseMapToBeanFilter);
            resultList.add(bean);
        }

        return resultList;
    }


    private static class IgnoreFieldCaseMapToBeanFilter implements MapToBeanFilter {
        @Override
        public Object getValue(BeanField beanField, Map<String, ?> map) {

            String filedName = beanField.getFieldName();
            Object value = map.get(filedName);
            if (value == null) {
                String filedName2 = beanField.getFieldNameUnderline();
                if (!filedName2.equals(filedName)) {
                    value = map.get(filedName2);
                }

                if (value == null) {
                    String filedName22 = filedName2.toLowerCase();
                    value = map.get(filedName22);
                }
            }

            if (value == null) {
                String filedName3 = filedName.toLowerCase();
                value = map.get(filedName3);
            }

            return value;
        }


        @Override
        public Object toBeanFieldType(Object value, BeanField beanField, Map<String, ?> map) throws Exception {
            return beanField.valueOf(value);
        }
    }

}