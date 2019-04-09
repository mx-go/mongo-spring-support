package com.max.mongo.support.mongo;

import lombok.Data;

/**
 * mongodb参数配置类
 *
 * @author max
 */
@Data
public class MongoConfiguration {
    /**
     * MongoDB连接地址
     * for example: mongodb://userName:password@localhost:27017/testDB
     */
    public String servers;
    /**
     * MongoDB数据库名称
     */
    public String dbName;
    /**
     * 扫描包路径做orm映射
     */
    public String mapPackage;
    /**
     * 读优先配置。用于查询、Map-Reduce、聚合、计数的读取首选项。
     * 设置为secondary则优先读从库
     */
    public String readPreference = "primary";
    /**
     * 连接超时时间（毫秒），仅在新建连接时使用。
     * <p>
     * 默认为 10,000（10秒），0 表示无限制，不能小于 0。
     */
    public int connectTimeout = 5000;
    /**
     * 读取mongo响应超时设置
     * <p>
     * 默认为 0，表示无限制。
     */
    public int socketTimeout = 60000;
    /**
     * 找不到合适服务器时候就决定放弃的时间间隔
     */
    public int serverSelectionTimeout = 10000;
    /**
     * 线程从连接池中获取可用连接的最长等待时间（毫秒）。
     * <p>
     * 默认为 120,000（120秒），0 表示不等待，负值意味着无限期等待。
     */
    public int maxWaitTime = 120000;
    /**
     * 连接池中连接的最大使用寿命（毫秒）。超出使用寿命的连接将被关闭，并在必要时由新建连接替换。
     * <p>
     * 默认为 0，表示无限制，不能小于 0。
     */
    public int maxConnectionLifeTime = 86400000;
    /**
     * 连接池中连接的最大空闲时间（毫秒）。超出空闲时间的连接将被关闭，并在必要时由新建连接替换。
     * <p>
     * 默认为 0，表示无限制，不能小于 0。
     */
    public int maxConnectionIdleTime = 30000;
    /**
     * 每个主机允许的最大连接数，这些连接在空闲时将保持在连接池中。当连接池耗尽后，任何需要连接的操作都将被阻塞并等待可用连接。
     * <p>
     * 默认为 100，不能小于 1。
     */
    public int maxConnectionsPerHost = 100;
    /**
     * 是否保存空集合
     */
    public boolean storeEmpties = false;
    /**
     * 是否保存null对象
     */
    public boolean storeNulls = false;
    /**
     * orm映射是否忽略class映射不对
     */
    public boolean ignoreInvalidClasses = false;
    /**
     * 默认鉴权库名
     * 鉴权信息保存在一个独立库中，登录后再切换操作其他数据库
     */
    public String trustDbName;
}