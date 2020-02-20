package com.github.mongo.support.mongo;

import org.mongodb.morphia.AdvancedDatastore;

/**
 * 扩展datastore功能
 */
public interface DatastoreExt extends AdvancedDatastore {

    /**
     * 切换使用的db，使用默认的collection名字
     *
     * @param dbName db名字
     * @return datastore对象
     */
    DatastoreExt use(String dbName);

    /**
     * 同一个库，支持带前缀的CollectionName，例如 2017_user
     *
     * @param dbName db名字
     * @param prefix collection的前缀名(默认_分割)
     * @return datastore对象
     */
    DatastoreExt getDatastoreByPrefix(String dbName, String prefix);

    /**
     * 同一个库，支持带后缀的CollectionName，例如 user_2017
     *
     * @param dbName db名字
     * @param suffix collection的后缀名(默认_分割)
     * @return datastore对象
     */
    DatastoreExt getDatastoreBySuffix(String dbName, String suffix);
}
