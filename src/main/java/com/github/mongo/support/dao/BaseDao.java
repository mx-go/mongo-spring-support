package com.github.mongo.support.dao;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.List;

/**
 * 基础方法
 *
 * @param <T>
 * @author max
 */
public interface BaseDao<T> {
    /**
     * 获取Datastore,自定义操作
     *
     * @return Datastore
     */
    Datastore getDatastore();

    /**
     * 保存实体.
     * 注意，这里只能用于新建保存，更新的话不要用这个方法.
     * 如果ID为String类型时会插入一条新的文档ID为字符串的新记录而不是更新类型为ObjectId的那条记录
     *
     * @return 返回新建的id
     */
    String save(T entity);

    /**
     * 根据逻辑与条件查询列表.
     *
     * @param condition 查询条件
     * @return 实体列表
     */
    List<T> queryList(T condition);

    /**
     * 查询指定数量
     *
     * @param condition 查询条件
     * @param offset    游标
     * @param limit     限制条数
     * @return 实体列表
     */
    List<T> queryList(T condition, int offset, int limit);

    /**
     * 根据id查询指定记录.
     *
     * @param id 记录id
     */
    T queryById(String id);

    /**
     * 根据id列表查询指定记录
     *
     * @param ids 记录id列表
     * @return
     */
    List<T> queryByIds(List<String> ids);

    /**
     * 根据逻辑与条件查询记录数目.
     *
     * @param condition 查询条件
     * @return 记录数目
     */
    long queryCount(T condition);

    /**
     * 创建查询
     *
     * @return Query
     */
    Query<T> createQuery();

    /**
     * 更新操作
     *
     * @return UpdateOperations
     */
    UpdateOperations<T> createUpdateOperations();
}