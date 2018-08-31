package com.github.linushp.orm.utils;



import com.github.linushp.commons.BeanField;
import com.github.linushp.commons.BeanFieldUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class ResultSetUtils {

    public static <T> List<T> resultSetToEntityList(ResultSet resultSet, Class<T> tClass) throws Exception {
        List<String> columnLabels = getColumnLabels(resultSet);
        Set<String> columnLabelSet = new HashSet<>(columnLabels);
        List<T> tList = new ArrayList<>();
        while (resultSet.next()) {
            tList.add(resultSetToEntity(columnLabelSet, resultSet, tClass));
        }
        return tList;
    }


    public static <T> T resultSetToEntity(ResultSet resultSet, Class<T> tClass) throws Exception {
        List<String> columnLabels = getColumnLabels(resultSet);
        Set<String> columnLabelSet = new HashSet<>(columnLabels);
        return resultSetToEntity(columnLabelSet, resultSet, tClass);
    }


    private static <T> T resultSetToEntity(Set<String> columnLabelSet, ResultSet resultSet, Class<T> tClass) throws Exception {

        T obj = tClass.newInstance();

        List<BeanField> beanFields = BeanFieldUtils.getBeanFields(tClass);

        for (BeanField beanField : beanFields) {

            Object value = getValueOfResultSet(columnLabelSet, resultSet, beanField.getFieldName(), beanField.getFieldNameUnderline());

            beanField.setBeanValue_autoConvert(obj, value);
        }

        return obj;
    }


    private static Object getValueOfResultSet(Set<String> columnLabelSet, ResultSet resultSet, String fieldName1, String fieldName2) throws SQLException {

        if (columnLabelSet.contains(fieldName1)) {
            return resultSet.getObject(fieldName1);
        }

        if (columnLabelSet.contains(fieldName2)) {
            return resultSet.getObject(fieldName2);
        }

        return null;

    }


    /**
     * 处理结果集, 得到 Map 的一个 List, 其中一个 Map 对象对应一条记录
     *
     * @param resultSet
     * @return
     * @throws SQLException
     */
    public static List<Map<String, ?>> resultSetToMapList(ResultSet resultSet) throws SQLException {
        List<Map<String, ?>> values = new ArrayList<>();
        if (resultSet != null) {
            List<String> columnLabels = getColumnLabels(resultSet);
            // 7. 处理 ResultSet, 使用 while 循环
            while (resultSet.next()) {

                Map<String, Object> map = new HashMap<>();

                for (String columnLabel : columnLabels) {
                    Object value = resultSet.getObject(columnLabel);
                    map.put(columnLabel, value);
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
