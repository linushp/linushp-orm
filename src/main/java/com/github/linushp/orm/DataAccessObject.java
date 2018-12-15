package com.github.linushp.orm;

import com.github.linushp.commons.*;
import com.github.linushp.commons.ifs.CharFilter;
import com.github.linushp.commons.model.Page;
import com.github.linushp.orm.model.*;
import com.github.linushp.orm.utils.ResultSetParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.ConnectException;
import java.sql.Connection;
import java.util.*;


/**
 * 1.如果想动态选择DB，可以在ConnectionFactory中实现
 * 2.如果想动态选择schemaName,可以在子类中的schemaTableName方法实现.
 * <p>
 * 固定表名
 *
 * @param <T> ORM的类名
 */
public class DataAccessObject<T> {
    public static int maxPageRowSize = 100;

    private static final Logger LOGGER = LoggerFactory.getLogger(DataAccessObject.class);

    protected Class<T> clazz;
    protected String tableName;
    protected String selectFields = "*";
    protected String schemaName = "";
    protected String idFieldName = "id"; //可以被重新设置,对应的是数据库中的字段名
    protected DataAccess dataAccess;

    protected boolean isUnderlineKey = true; //只有当Entity的字段名和数据库表的字段名大小写不一样时，启用才有意义。
    protected boolean isIgnoreNull = true;


    public DataAccessObject(Class<T> clazz) {
        this.clazz = clazz;
        this.tableName = clazz.getSimpleName().toLowerCase();
    }


    public DataAccessObject(Class<T> clazz, String tableName) {
        this.clazz = clazz;
        this.tableName = tableName;
    }

    /**
     * 创建一个数据访问对象
     *
     * @param clazz             ORM的类
     * @param tableName         表名
     * @param connectionFactory 如果想动态选择DB，可以在ConnectionFactory中实现
     */
    public DataAccessObject(Class<T> clazz, String tableName, ConnectionFactory connectionFactory) {
        this.clazz = clazz;
        this.tableName = tableName;
        this.dataAccess = new DataAccess(connectionFactory);
    }

    @Deprecated
    public DataAccessObject(Class<T> clazz, String tableName, Connection connection) {
        this.clazz = clazz;
        this.tableName = tableName;
        this.dataAccess = new DataAccess(new SingleConnectionFactory(connection));
    }

    public DataAccess getDataAccess() {
        return this.dataAccess;
    }


    /**
     * protected方法方便子类扩展
     * 如果想动态改变schemaName可以在子类中的schemaTableName方法实现
     *
     * @return select from 后面的表名
     */
    protected String schemaTableName() {
        if (schemaName == null || schemaName.isEmpty()) {
            return tableName;
        }
        return schemaName + "." + tableName;
    }


    public DataAccessObject() {
    }


    public void setDataModifyListener(DataModifyListener dataModifyListener) {
        this.getDataAccess().setDataModifyListener(dataModifyListener);
    }

    /**
     * 请在子类的构造方法中调用
     *
     * @param resultSetParser
     */
    protected void setResultSetParser(ResultSetParser<T> resultSetParser) {
        this.getDataAccess().setResultSetParser(resultSetParser);
    }


    //克隆的是一个子类对象
    //子类对象必须实现一个无参构造方法
    public Object clone() {
        try {
            //获取的是子类的Class
            Object o = this.getClass().newInstance();
            BeanUtils.copyField(o, this);
            return o;
        } catch (IllegalAccessException e) {
            LOGGER.error("", e);
        } catch (InstantiationException e) {
            LOGGER.error("", e);
        }
        return null;
    }


    public List<T> findAll() throws Exception {
        return findByWhere("");
    }

    public T findById(Serializable id) throws Exception {
        return findOneByWhere("where " + getIdFieldNameQuota() + " = ?", id);
    }

    public T findOneByWhere(String whereSql, Object... args) throws Exception {
        whereSql = whereSql + " limit 0,1";
        List<T> list = findByWhere(whereSql, args);
        return CollectionUtils.getFirstElement(list);
    }

    public T findOneByField(String fieldName, Object value) throws Exception {
        return this.findOneByWhere(toFieldWhereSql(fieldName), value);
    }

    public List<T> findListByField(String fieldName, Object value) throws Exception {
        return this.findByWhere(toFieldWhereSql(fieldName), value);
    }

    public List<T> findByIdList(List idList) throws Exception {
        return findByIdList(this.idFieldName, idList, new DefaultIdCharFilter());
    }

    public List<T> findByIdList(String idFieldName, List idList) throws Exception {
        return findByIdList(idFieldName, idList, new DefaultIdCharFilter());
    }

    public List<T> findByIdList(List idList, CharFilter idCharFilter) throws Exception {
        return findByIdList(this.idFieldName, idList, idCharFilter);
    }

    public List<T> findByIdList(String idFieldName, List idList, CharFilter idCharFilter) throws Exception {

        //移除null
        idList = CollectionUtils.removeNull(idList);

        //移除重复的
        idList = CollectionUtils.uniqueList(idList);

        //过滤出合法的Id类型。避免SQL注入的问题。
        idList = CollectionUtils.filterOnlyLegalItems(idList, idCharFilter);

        if (CollectionUtils.isEmpty(idList)) {
            return new ArrayList<>();
        }

        StringParser stringParser = new StringParser() {
            @Override
            public String valueOf(Object o) {
                if (o instanceof String) {
                    return "'" + o.toString() + "'";
                }
                return String.valueOf(o);
            }
        };

        String idString = StringUtils.join(idList, ",", stringParser);
        String sql = "select " + selectFields + " from " + schemaTableName() + " where `" + toFieldDbName(idFieldName) + "` in (" + idString + ")";
        List<Map<String, ?>> mapList = getDataAccess().queryTemp(sql);
        return BeanUtils.mapListToBeanList(clazz, mapList);
    }

    public List<T> findByWhere(WhereSqlAndArgs whereSqlAndArgs) throws Exception {
        return findByWhere(whereSqlAndArgs.whereSql, whereSqlAndArgs.whereArgs);
    }

    public List<T> findByWhere(WhereSqlBuilder whereSqlBuilder) throws Exception {
        return findByWhere(whereSqlBuilder.toWhereSqlAndArgs());
    }


    public List<T> findByExample(T example) throws Exception {
        Map<String, Object> exampleMap = entityToMap(example);
        return findByExample(exampleMap);
    }


    public List<T> findByExample(Map<String, Object> example) throws Exception {
        return findByWhere(toWhereSqlAndArgs(example));
    }

    public List<T> findByWhere(String whereSql, Object... args) throws Exception {
        String sql = "select " + selectFields + " from " + schemaTableName() + " " + whereSql;
        return getDataAccess().query(clazz, sql, args);
    }

    public Page<T> findPageByExample(int pageNo, int pageSize, Map<String, Object> example) throws Exception {
        return findPageByExample(pageNo, pageSize, example, "");
    }

    public Page<T> findPageByExample(int pageNo, int pageSize, T example) throws Exception {
        Map<String, Object> exampleMap = entityToMap(example);
        return findPageByExample(pageNo, pageSize, exampleMap, "");
    }

    public Page<T> findPageByExample(int pageNo, int pageSize, T example, String orderBy) throws Exception {
        Map<String, Object> exampleMap = entityToMap(example);
        return findPageByExample(pageNo, pageSize, exampleMap, orderBy);
    }

    public Page<T> findPageByExample(int pageNo, int pageSize, Map<String, Object> example, String orderBy) throws Exception {
        WhereSqlAndArgs mm = toWhereSqlAndArgs(example);
        return findPage(pageNo, pageSize, mm.whereSql, orderBy, mm.whereArgs);
    }

    public Page<T> findPageByWhere(int pageNo, int pageSize, WhereSqlBuilder whereSqlBuilder, String orderBy) throws Exception {
        return findPageByWhere(pageNo, pageSize, whereSqlBuilder.toWhereSqlAndArgs(), orderBy);
    }

    public Page<T> findPageByWhere(int pageNo, int pageSize, WhereSqlAndArgs whereSqlAndArgs, String orderBy) throws Exception {
        return findPage(pageNo, pageSize, whereSqlAndArgs.whereSql, orderBy, whereSqlAndArgs.whereArgs);
    }


    public Page<T> findPage(int pageNo, int pageSize) throws Exception {
        return findPage(pageNo, pageSize, "", "");
    }


    /**
     * whereSql 里面不能含有order by 和 limit 等语句，因为whereSql不仅作为find的查询条件，也作为count的查询条件
     */
    public Page<T> findPage(int pageNo, int pageSize, String whereSql, String orderBy, List<Object> whereArgs) throws Exception {
        Object[] whereArgArray = whereArgs.toArray(new Object[whereArgs.size()]);
        return findPage(pageNo, pageSize, whereSql, orderBy, whereArgArray);
    }


    /**
     * 分页查询
     * whereSql 里面不能含有order by 和 limit 等语句，因为whereSql不仅作为find的查询条件，也作为count的查询条件
     *
     * @param pageNo    页号从零开始
     * @param pageSize  每夜多少条数据
     * @param whereSql  条件，whereSql 里面不能含有order by 和 limit 等语句，因为whereSql不仅作为find的查询条件，也作为count的查询条件
     * @param orderBy   排序条件
     * @param whereArgs 条件参数
     * @return 返回Page对象
     * @throws Exception 可能的异常
     */
    public Page<T> findPage(int pageNo, int pageSize, String whereSql, String orderBy, Object... whereArgs) throws Exception {
        String fromWhere = "from " + schemaTableName() + " " + whereSql ;
        String selectSql = "select " + selectFields ;
        String countSql = "select count(0)";
        return findPageAdvanced(pageNo,pageSize,countSql,selectSql,fromWhere,orderBy,whereArgs);
    }


    /**
     * 分页查询
     * fromWhere 里面不能含有order by 和 limit 等语句，因为whereSql不仅作为find的查询条件，也作为count的查询条件
     */
    public Page<T> findPageAdvanced(int pageNo, int pageSize, String countSqlPrefix, String selectSqlPrefix, String fromWhere, String orderBy, Object... whereArgs) throws Exception {

        if (pageNo < 0) {
            pageNo = 0;
        }

        if (pageSize < 0) {
            pageSize = 30;
        }

        if (pageSize > maxPageRowSize) {
            pageSize = maxPageRowSize;
        }

        int beginIndex = pageNo * pageSize;


        String countSql = countSqlPrefix + " " + fromWhere;
        Object totalCountObj = this.getDataAccess().queryValue(countSql, whereArgs);
        Long totalCount = CastBasicTypeUtils.toLong(totalCountObj);

        //totalCount 为0的时候可以不查询
        List<T> dataList;
        if (totalCount > 0) {

            String selectSql = selectSqlPrefix + " " + fromWhere + " " + orderBy + "  limit  ?,?  ";

            List whereArgsList = CollectionUtils.toObjectList(whereArgs);
            whereArgsList.add(beginIndex);
            whereArgsList.add(pageSize);

            dataList = this.getDataAccess().query(clazz, selectSql, whereArgsList.toArray());
        } else {
            dataList = new ArrayList<>();
        }
        return new Page<>(dataList, totalCount, pageNo, pageSize);
    }



    /**
     * 统计整个表的大小
     *
     * @return 数量
     */
    public Long countAll() throws Exception {
        return countByWhereSql("");
    }


    /**
     * 判断对象是否存在
     *
     * @param entity 查询条件
     * @return
     */
    public boolean exists(T entity) throws Exception {
        Map<String, Object> example = entityToMap(entity);
        return exists(example);
    }


    /**
     * 判断对象是否存在
     *
     * @param example 查询条件
     * @return
     */
    public boolean exists(Map<String, Object> example) throws Exception {
        Long x = countByExample(example);
        return (x != null && x > 0);
    }


    public Long countByExample(T entity) throws Exception {
        Map<String, Object> example = entityToMap(entity);
        return countByExample(example);
    }


    public Long countByExample(Map<String, Object> example) throws Exception {
        WhereSqlAndArgs whereSqlArgs = toWhereSqlAndArgs(example);
        return countByWhereSql(whereSqlArgs.whereSql, whereSqlArgs.whereArgs);
    }


    /**
     * 统计数量多少
     */
    public Long countByWhere(WhereSqlBuilder whereSqlBuilder) throws Exception {
        WhereSqlAndArgs whereSqlAndArgs = whereSqlBuilder.toWhereSqlAndArgs();
        return countByWhereSql(whereSqlAndArgs.whereSql, whereSqlAndArgs.whereArgs);
    }


    /**
     * 统计数量多少
     */
    public Long countByWhere(WhereSqlAndArgs whereSqlAndArgs) throws Exception {
        return countByWhereSql(whereSqlAndArgs.whereSql, whereSqlAndArgs.whereArgs);
    }


    /**
     * 统计数量多少
     *
     * @param whereSql  条件
     * @param whereArgs 条件参数
     * @return 数量
     */
    public Long countByWhereSql(String whereSql, Object... whereArgs) throws Exception {
        String sqlCount = "select count(0) from " + schemaTableName() + " " + whereSql;
        Object totalCount = getDataAccess().queryValue(sqlCount, whereArgs);
        return (Long) CastBasicTypeUtils.toBasicTypeOf(totalCount, Long.class);
    }

    /**
     * 统计数量多少
     *
     * @param whereSql  条件
     * @param whereArgs 条件参数
     * @return 数量
     */
    public Long countByWhereSql(String whereSql, List<Object> whereArgs) throws Exception {
        Object[] whereArgArray = whereArgs.toArray(new Object[whereArgs.size()]);
        return countByWhereSql(whereSql, whereArgArray);
    }

    public Long countByField(String fieldName, Object value) throws Exception {
        return this.countByWhereSql(toFieldWhereSql(fieldName), value);
    }


    /**
     * 根据Id删除
     *
     * @param id bean id
     */
    public UpdateResult deleteById(Serializable id) throws Exception {
        return deleteByWhereSql("where " + getIdFieldNameQuota() + " =?", id);
    }


    /**
     * 根据条件删除
     *
     * @param entity 查询条件,不包括null值，并自动驼峰转下划线
     * @return 操作结果
     */
    public UpdateResult deleteByExample(T entity) throws Exception {
        Map<String, Object> example = entityToMap(entity);
        return deleteByExample(example);
    }


    /**
     * 根据条件删除
     */
    public UpdateResult deleteByExample(Map<String, Object> example) throws Exception {
        WhereSqlAndArgs mm = toWhereSqlAndArgs(example);
        return deleteByWhereSql(mm.whereSql, mm.whereArgs);
    }


    /**
     * 删除
     *
     * @param whereSql  条件
     * @param whereArgs 参数
     */
    public UpdateResult deleteByWhereSql(String whereSql, Object... whereArgs) throws Exception {
        String sql = "delete from " + schemaTableName() + " " + whereSql;
        return getDataAccess().update(sql, whereArgs);
    }


    /**
     * 删除
     *
     * @param whereSql  条件
     * @param whereArgs 参数
     */
    public UpdateResult deleteByWhereSql(String whereSql, List<Object> whereArgs) throws Exception {
        Object[] whereArgArray = whereArgs.toArray(new Object[whereArgs.size()]);
        return deleteByWhereSql(whereSql, whereArgArray);
    }


    public UpdateResult deleteByField(String fieldName, Object value) throws Exception {
        return this.deleteByWhereSql(toFieldWhereSql(fieldName), value);
    }


    public UpdateResult update(T entity) throws Exception {
        return updateById(entity);
    }

    public UpdateResult updateById(T entity) throws Exception {
        Map<String, Object> newValues = entityToMap(entity);
        return updateById(newValues);
    }

    public UpdateResult updateById(T entity, Serializable id) throws Exception {
        Map<String, Object> newValues = entityToMap(entity);
        return updateByWhereSql(newValues, "where " + getIdFieldNameQuota() + " = ? ", id);
    }

    public UpdateResult updateByField(T entity, String fieldName, Object value) throws Exception {
        Map<String, Object> newValues = entityToMap(entity);
        return this.updateByWhereSql(newValues, toFieldWhereSql(fieldName), value);
    }

    public UpdateResult updateByField(Map<String, Object> newValues, String fieldName, Object value) throws Exception {
        return this.updateByWhereSql(newValues, toFieldWhereSql(fieldName), value);
    }

    public UpdateResult updateById(Map<String, Object> newValues) throws Exception {
        Object id = newValues.get(this.getIdFieldDbName());
        if (id == null) {
            throw new NullPointerException("自动获取ID字段失败，ID字段不能为null");
        }
        return updateByWhereSql(newValues, "where " + getIdFieldNameQuota() + " = ? ", id);
    }

    public UpdateResult updateById(Map<String, Object> newValues, Serializable id) throws Exception {
        return updateByWhereSql(newValues, "where " + getIdFieldNameQuota() + " = ? ", id);
    }


    public UpdateResult updateByWhereSql(T entity, String whereSql, Object... whereArgs) throws Exception {
        Map<String, Object> newValues = entityToMap(entity);
        return updateByWhereSql(newValues, whereSql, whereArgs);
    }


    public UpdateResult updateByWhereSql(Map<String, Object> newValues, String whereSql, Object... whereArgs) throws Exception {
        if (!CollectionUtils.isEmpty(newValues)) {

            List[] keysValues = CollectionUtils.listKeyValues(newValues);
            List<String> keys = keysValues[0];
            List<Object> values = keysValues[1];
            List<String> keys2 = CollectionUtils.eachWrap(keys, "`", "`=?");
            String setSql = StringUtils.join(keys2, ",");


            String sql = "update " + schemaTableName() + " set " + setSql + " " + whereSql;
            if (whereArgs != null && whereArgs.length > 0) {
                List<Object> whereArgsList = Arrays.asList(whereArgs);
                values.addAll(whereArgsList);
            }

            return getDataAccess().update(sql, values);
        }
        return new UpdateResult("params is empty");
    }


    /**
     * 更新自增字段 + 1
     */
    public UpdateResult increaseById(String field_name, Serializable id) throws Exception {
        return incOrDecNumberByWhereSql(field_name, 1, "where " + getIdFieldNameQuota() + " = ", id);
    }


    /**
     * 更新自增字段 -1
     */
    public UpdateResult decreaseById(String field_name, Serializable id) throws Exception {
        return incOrDecNumberByWhereSql(field_name, -1, "where " + getIdFieldNameQuota() + " = ", id);
    }


    /**
     * 更新自增字段
     */
    public UpdateResult incOrDecNumberByWhereSql(String field_name, int num, String whereSql, Object... whereArgs) throws Exception {
        field_name = toFieldDbName(field_name);
        String sql = "update " + schemaTableName() + " set `" + field_name + "` = `" + field_name + "`  " + num + "  " + whereSql;
        return getDataAccess().update(sql, whereArgs);
    }


    public UpdateResult insertObject(T entity) throws Exception {
        Map<String, Object> map = entityToMap(entity);
        return insertObject(map);
    }


    /**
     * 保存对象
     *
     * @param entity         要保存的对象
     * @param isUnderlineKey 生成SQL时，自动将驼峰字段名转换为下划线
     * @param isIgnoreNull   忽略值为null的字段
     * @return
     * @throws Exception
     */
    public UpdateResult insertObject(T entity, boolean isUnderlineKey, boolean isIgnoreNull) throws Exception {
        Map<String, Object> map = BeanUtils.beanToMap(entity, isUnderlineKey, isIgnoreNull);
        return insertObject(map);
    }


    public UpdateResult insertObject(Map<String, Object> newValues) throws Exception {
        if (!CollectionUtils.isEmpty(newValues)) {

            List[] keysValues = CollectionUtils.listKeyValues(newValues);
            List<String> keys = keysValues[0];
            List<Object> values = keysValues[1];

            List<String> keys2 = CollectionUtils.eachWrap(keys, "`", "`");
            List<String> valuesQuota = CollectionUtils.repeatList("?", values.size());

            String filedSql = StringUtils.join(keys2, ",");
            String valuesSql = StringUtils.join(valuesQuota, ",");

            String sql = "insert into " + schemaTableName() + "(" + filedSql + ") values (" + valuesSql + ")";
            return getDataAccess().update(sql, values);
        }

        return new UpdateResult("params is empty");
    }


    /**
     * 批量插入，使用循环调用的方式。
     * 优点：允许中间出现差错。
     * 缺点：效率低
     * 在调用此方法时,把它放在同一个事务里，效率会更好。
     *
     * @param objectList 循环插入的对象列表
     * @return 插入结果的集合
     */
    public List<UpdateResult> batchInsertUsingRepeat(List<Map<String, Object>> objectList) throws ConnectException {
        List<UpdateResult> results = new ArrayList<>();

        if (!CollectionUtils.isEmpty(objectList)) {

            for (Map<String, Object> obj : objectList) {

                UpdateResult result;
                try {
                    result = insertObject(obj);
                } catch (ConnectException ce) {
                    throw ce;
                } catch (Exception e) {
                    result = new UpdateResult(e.toString());
                }
                results.add(result);
            }
        }

        return results;
    }


    /**
     * 批量插入，拼接成一个大SQL。
     * 优点：高效
     * 缺点：中间出现一个异常会导致整批插入失败。
     *
     * @param entityList 需要插入大对象
     * @return Update Result
     */
    public UpdateResult batchInsertEntityUsingLargeSQL(List<T> entityList) throws Exception {

        if (CollectionUtils.isEmpty(entityList)) {
            return new UpdateResult("params is empty");
        }

        List<Map<String, Object>> objectList = new ArrayList<>();
        for (T entity : entityList) {
            Map<String, Object> map = entityToMap(entity);
            objectList.add(map);
        }

        return batchInsertUsingLargeSQL(objectList);
    }


    /**
     * 批量插入，拼接成一个大SQL。
     * 优点：高效
     * 缺点：中间出现一个异常会导致整批插入失败。
     *
     * @param objectList 需要插入大对象
     * @return Update Result
     */
    public UpdateResult batchInsertUsingLargeSQL(List<Map<String, Object>> objectList) throws Exception {

        objectList = CollectionUtils.removeEmptyMap(objectList);

        if (!CollectionUtils.isEmpty(objectList)) {

            Set<String> fieldKeys = CollectionUtils.getAllMapKeys(objectList);

            List<String> fieldKeysList = new ArrayList<>(fieldKeys);
            List<String> fieldKeysWList = CollectionUtils.eachWrap(fieldKeysList, "`", "`");
            String filedSql = StringUtils.join(fieldKeysWList, ",");


            List<String> valuesQuota = CollectionUtils.repeatList("?", fieldKeysList.size());
            String valuesSql = "(" + StringUtils.join(valuesQuota, ",") + ")"; // (?,?,?)


            List<String> allValuesSqlList = new ArrayList<>();
            List<Object> allValues = new ArrayList<>();
            for (Map<String, Object> object : objectList) {
                allValuesSqlList.add(valuesSql);
                for (String key : fieldKeysList) {
                    Object value = object.get(key);
                    allValues.add(value);
                }
            }

            String allValuesSql = StringUtils.join(allValuesSqlList, ",");
            String sql = "insert into " + schemaTableName() + " (" + filedSql + ") values " + allValuesSql;

            return getDataAccess().update(sql, allValues);
        }


        return new UpdateResult("params is empty");
    }


    /**
     * 根据ID字段是否存在，决定插入还是修改
     */
    public UpdateResult saveOrUpdateById(T entity) throws Exception {
        Map<String, Object> newValues = entityToMap(entity);
        return saveOrUpdateById(newValues);
    }


    /**
     * 根据ID字段是否存在，决定插入还是修改
     */
    public UpdateResult saveOrUpdateById(Map<String, Object> newValues) throws Exception {
        Object id = newValues.get(this.getIdFieldDbName());
        if (id == null) {
            return insertObject(newValues);
        } else {
            return updateById(newValues, (Serializable) id);
        }
    }


    public UpdateResult saveOrUpdateById(T entity, Serializable id) throws Exception {
        Map<String, Object> newValues = entityToMap(entity);
        return saveOrUpdate(newValues, "where " + getIdFieldNameQuota() + " = ?", id);
    }


    public UpdateResult saveOrUpdateById(Map<String, Object> newValues, Serializable id) throws Exception {
        return saveOrUpdate(newValues, "where " + getIdFieldNameQuota() + " = ?", id);
    }


    public UpdateResult saveOrUpdate(T entity, String whereSql, Object... whereArgs) throws Exception {
        Map<String, Object> newValues = entityToMap(entity);
        return saveOrUpdate(newValues, whereSql, whereArgs);
    }


    public UpdateResult saveOrUpdate(Map<String, Object> newValues, String whereSql, Object... whereArgs) throws Exception {
        List<T> findResult = findByWhere(whereSql, whereArgs);
        if (findResult.isEmpty()) {
            return insertObject(newValues);
        } else {
            return updateByWhereSql(newValues, whereSql, whereArgs);
        }
    }


    protected WhereSqlAndArgs toWhereSqlAndArgs(Map<String, Object> example) {
        if (CollectionUtils.isEmpty(example)) {
            return new WhereSqlAndArgs("", new ArrayList<>());
        }

        List[] keysValues = CollectionUtils.listKeyValues(example);
        List<String> keys = keysValues[0];
        List<Object> values = keysValues[1];
        List<String> whereFields = CollectionUtils.eachWrap(keys, "`", "` = ?");
        String whereSql = "where " + StringUtils.join(whereFields, " and ");
        return new WhereSqlAndArgs(whereSql, values);
    }


    private String toFieldWhereSql(String fieldName) throws Exception {
        fieldName = fieldName.trim();
        if (fieldName.isEmpty()) {
            throw new Exception("fieldName can not be empty");
        }
        return "where `" + toFieldDbName(fieldName) + "` = ?";
    }


    private Map<String, Object> entityToMap(T entity) throws Exception {
        return BeanUtils.beanToMap(entity, this.isUnderlineKey, this.isIgnoreNull);
    }


    private static class DefaultIdCharFilter implements CharFilter {


        private static final char[] WHITE_LIST = {'-', '_', '~', '.'};

        /**
         * 判断是否是合法的ID允许出现的字符
         *
         * @param cc 字符
         * @return 是否合法
         */
        public boolean isOK(char cc) {

            if (cc >= 'A' && cc <= 'Z') {
                return true;
            }
            if (cc >= 'a' && cc <= 'z') {
                return true;
            }
            if (cc >= '0' && cc <= '9') {
                return true;
            }

            for (int i = 0; i < WHITE_LIST.length; i++) {
                if (cc == WHITE_LIST[i]) {
                    return true;
                }
            }

            return false;
        }
    }


    private String getIdFieldDbName() {
        return toFieldDbName(this.idFieldName);
    }


    private String toFieldDbName(String fieldName) {
        if (this.isUnderlineKey) {
            return StringUtils.camel2Underline(fieldName, true);
        }
        return fieldName;
    }


    private String getIdFieldNameQuota() {
        return " `" + this.getIdFieldDbName() + "` ";
    }

}
