package com.github.linushp.orm.utils;


import com.github.linushp.commons.BeanField;
import com.github.linushp.commons.BeanFieldUtils;
import com.github.linushp.commons.CastBasicTypeUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class ResultSetUtils {

    public static <T> List<T> resultSetToEntityList(ResultSet resultSet, Class<T> tClass, boolean isIgnoreCase) throws Exception {
        List<String> columnLabels = getColumnLabels(resultSet);
        Set<String> columnLabelSet = new HashSet<>(columnLabels);
        List<T> tList = new ArrayList<>();
        while (resultSet.next()) {
            tList.add(resultSetToEntity(columnLabelSet, resultSet, tClass,isIgnoreCase));
        }
        return tList;
    }


    public static <T> T resultSetToEntity(ResultSet resultSet, Class<T> tClass,boolean isIgnoreCase) throws Exception {
        List<String> columnLabels = getColumnLabels(resultSet);
        Set<String> columnLabelSet = new HashSet<>(columnLabels);
        return resultSetToEntity(columnLabelSet, resultSet, tClass,isIgnoreCase);
    }


    private static <T> T resultSetToEntity(Set<String> columnLabelSet, ResultSet resultSet, Class<T> tClass,boolean isIgnoreCase) throws Exception {

        T obj = tClass.newInstance();

        List<BeanField> beanFields = BeanFieldUtils.getBeanFields(tClass);

        for (BeanField beanField : beanFields) {

            Object value = getValueOfResultSet(columnLabelSet, resultSet, beanField,isIgnoreCase);

            beanField.setBeanValue_autoConvert(obj, value);
        }

        return obj;
    }


    private static Object getValueOfResultSet(Set<String> columnLabelSet, ResultSet resultSet, BeanField beanField, boolean isIgnoreCase) throws SQLException {

        String fieldName1 = beanField.getFieldName();
        String fieldName2 = beanField.getFieldNameUnderline();

        if (beanField.getField().getType() == Integer.class || beanField.getField().getType() == int.class) {
            return getResultSetIntegerValue(columnLabelSet, resultSet, fieldName1, fieldName2, isIgnoreCase);
        }

        return getResultSetObjectValue(columnLabelSet, resultSet, fieldName1, fieldName2,isIgnoreCase);
    }


    private static Object getResultSetObjectValue(Set<String> columnLabelSet, ResultSet resultSet, String fieldName1, String fieldName2,boolean isIgnoreCase) throws SQLException {
        if (columnLabelSet.contains(fieldName1)) {
            return resultSet.getObject(fieldName1);
        }
        if (columnLabelSet.contains(fieldName2)) {
            return resultSet.getObject(fieldName2);
        }

        if (isIgnoreCase) {
            String fieldName21 = fieldName1.toLowerCase();
            if (columnLabelSet.contains(fieldName21)) {
                return resultSet.getObject(fieldName21);
            }
            String fieldName22 = fieldName2.toLowerCase();
            if (columnLabelSet.contains(fieldName22)) {
                return resultSet.getObject(fieldName22);
            }
            String fieldName31 = fieldName1.toUpperCase();
            if (columnLabelSet.contains(fieldName31)) {
                return resultSet.getObject(fieldName31);
            }
            String fieldName32 = fieldName2.toUpperCase();
            if (columnLabelSet.contains(fieldName32)) {
                return resultSet.getObject(fieldName32);
            }
        }
        return null;
    }


    private static int getResultSetIntegerValue(Set<String> columnLabelSet, ResultSet resultSet, String fieldName1, String fieldName2,boolean isIgnoreCase) throws SQLException {
        if (columnLabelSet.contains(fieldName1)) {
            return resultSet.getInt(fieldName1);
        }
        if (columnLabelSet.contains(fieldName2)) {
            return resultSet.getInt(fieldName2);
        }

        if (isIgnoreCase) {
            String fieldName21 = fieldName1.toLowerCase();
            if (columnLabelSet.contains(fieldName21)) {
                return resultSet.getInt(fieldName21);
            }
            String fieldName22 = fieldName2.toLowerCase();
            if (columnLabelSet.contains(fieldName22)) {
                return resultSet.getInt(fieldName22);
            }
            String fieldName31 = fieldName1.toUpperCase();
            if (columnLabelSet.contains(fieldName31)) {
                return resultSet.getInt(fieldName31);
            }
            String fieldName32 = fieldName2.toUpperCase();
            if (columnLabelSet.contains(fieldName32)) {
                return resultSet.getInt(fieldName32);
            }
        }
        return 0;
    }


    public static List<Map<String, ?>> resultSetToMapList(ResultSet resultSet) throws SQLException {
        return resultSetToMapList(resultSet, false);
    }


    /**
     * 处理结果集, 得到 Map 的一个 List, 其中一个 Map 对象对应一条记录
     *
     * @param resultSet
     * @return
     * @throws SQLException
     */
    public static List<Map<String, ?>> resultSetToMapList(ResultSet resultSet, boolean isToLowerCaseKey) throws SQLException {
        List<Map<String, ?>> values = new ArrayList<>();
        if (resultSet != null) {
            List<String> columnLabels = getColumnLabels(resultSet);
            // 7. 处理 ResultSet, 使用 while 循环
            while (resultSet.next()) {

                Map<String, Object> map = new HashMap<>();

                for (String columnLabel : columnLabels) {
                    Object value = resultSet.getObject(columnLabel);

                    if (isToLowerCaseKey) {
                        map.put(columnLabel.toLowerCase(), value);
                    } else {
                        map.put(columnLabel, value);
                    }
                }
                // 11. 把一条记录的一个 Map 对象放入 5 准备的 List 中
                values.add(map);
            }
        }

        return values;
    }

    /**
     * 获取结果集的 ColumnLabel 对应的 List
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    public static List<String> getColumnLabels(ResultSet rs) throws SQLException {
        List<String> labels = new ArrayList<>();

        ResultSetMetaData rsmd = rs.getMetaData();
        for (int i = 0; i < rsmd.getColumnCount(); i++) {
            labels.add(rsmd.getColumnLabel(i + 1));
        }

        return labels;
    }

}
