package com.max.mongo.support.mongo;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.Reflection;
import com.google.common.util.concurrent.Uninterruptibles;
import com.mongodb.ConnectionString;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.ReadPreference;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.MapperOptions;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * FactoryBean生成代理对象
 *
 * @author max
 */
@Slf4j
public class MongoDataStoreFactoryBean implements InitializingBean, DisposableBean, FactoryBean<DatastoreExt> {

    /**
     * 配置bean
     */
    @Setter
    private MongoConfiguration mongoConfiguration;
    private Map<String, DatastoreExt> stores = Maps.newConcurrentMap();
    /**
     * 代理对象
     */
    private DatastoreExt datastoreExt;

    @Override
    public void afterPropertiesSet() {
        init();
    }

    @Override
    public void destroy() {
        stores.values().forEach(it -> ((DatastoreHandler) Proxy.getInvocationHandler(it)).getDelegate().getMongo().close());
    }

    @Override
    public Class<?> getObjectType() {
        return DatastoreExt.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public DatastoreExt getObject() {
        return datastoreExt;
    }

    @SuppressWarnings("UnstableApiUsage")
    DatastoreExt getOrCreate(String dbName, String format) {
        String key = Strings.isNullOrEmpty(format) ? dbName : (dbName + ':' + format);
        return stores.computeIfAbsent(key, it -> {
            List<String> values = Splitter.on(':').limit(2).splitToList(it);
            String db = values.get(0);
            String fmt = values.size() > 1 ? values.get(1) : null;
            ConnectionString connection = new ConnectionString(getUri(db));
            Datastore datastore = doCreate(connection, fmt);
            DatastoreHandler handler = new DatastoreHandler(this, connection.getDatabase(), datastore);
            return Reflection.newProxy(DatastoreExt.class, handler);
        });
    }

    private void init() {
        if (stores.isEmpty()) {
            initFirst();
        } else {
            List<Mongo> oldClients = Lists.newArrayList();
            for (Iterator<String> it = stores.keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                DatastoreHandler handler = (DatastoreHandler) Proxy.getInvocationHandler(stores.get(key));
                oldClients.add(handler.getDelegate().getMongo());
                //shard模式下，直接删除会在下次setTenantId的时候自动创建，但是其他模式可能会被别的地方引用，必须原位替换delegate对象
                if (key.startsWith("mongodb://")) {
                    it.remove();
                } else {
                    List<String> values = Splitter.on(':').limit(2).splitToList(key);
                    String fmt = values.size() > 1 ? values.get(1) : null;
                    String db = values.get(0);
                    handler.setDelegate(doCreate(new ConnectionString(getUri(db)), fmt));
                }
            }
            // 延迟关闭正在使用的mongoClient
            new Thread(() -> {
                Uninterruptibles.sleepUninterruptibly(30, TimeUnit.SECONDS);
                log.warn("close {} old clients", oldClients.size());
                oldClients.forEach(Mongo::close);
            }).start();
        }
    }

    private void initFirst() {
        String dbName = mongoConfiguration.getDbName();
        String uriDbName = new ConnectionString(mongoConfiguration.getServers()).getDatabase();
        String name = firstNotEmpty(dbName, uriDbName, "admin");
        datastoreExt = getOrCreate(name, null);
    }

    private String firstNotEmpty(String... names) {
        for (String name : names) {
            if (!Strings.isNullOrEmpty(name)) {
                return name;
            }
        }
        return null;
    }

    private String getUri(String dbName) {
        String servers = mongoConfiguration.getServers();
        // 如果配置的dbName和要使用的不一致,这里要做切换
        if (!servers.endsWith('/' + dbName)) {
            int pos = servers.lastIndexOf('/');
            // mongo.servers=mongodb://127.0.0.1:27017/test
            if (servers.charAt(pos - 1) != '/') {
                servers = servers.substring(0, pos + 1) + dbName;
            } else {
                servers = servers + '/' + dbName;
            }
        }
        return servers;
    }

    /**
     * 设置连接池配置
     *
     * @param connection connection
     * @return Datastore
     */
    private Datastore doCreate(ConnectionString connection, String format) {
        MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
        builder.readPreference(ReadPreference.valueOf(mongoConfiguration.getReadPreference()))
                .serverSelectionTimeout(mongoConfiguration.getServerSelectionTimeout())
                .maxWaitTime(mongoConfiguration.getMaxWaitTime())
                .maxConnectionLifeTime(mongoConfiguration.getMaxConnectionLifeTime())
                .maxConnectionIdleTime(mongoConfiguration.getMaxConnectionIdleTime())
                .connectionsPerHost(mongoConfiguration.getMaxConnectionsPerHost())
                .connectTimeout(mongoConfiguration.getConnectTimeout())
                .socketTimeout(mongoConfiguration.getSocketTimeout());

        Mapper mapper = new Mapper();
        if (!Strings.isNullOrEmpty(format)) {
            mapper = new MapperExt(format);
        }
        MapperOptions options = new MapperOptions();
        options.setStoreEmpties(mongoConfiguration.storeEmpties);
        options.setStoreNulls(mongoConfiguration.storeNulls);
        mapper.setOptions(options);
        Morphia morphia = new Morphia(mapper);
        morphia.mapPackage(mongoConfiguration.getMapPackage(), mongoConfiguration.ignoreInvalidClasses);
        MongoClient mongo = new MongoClient(new MongoClientURI(getAuthorizedURI(connection), builder));
        return morphia.createDatastore(mongo, connection.getDatabase());
    }

    private String getAuthorizedURI(ConnectionString connection) {
        String uri = connection.getConnectionString();
        String trustDb = mongoConfiguration.getTrustDbName();
        if ("admin".equals(trustDb)) {
            int pos = uri.indexOf('?');
            // mongo底层authSource参数只支持admin
            return uri + (pos > 0 ? "&authSource=admin" : "?authSource=admin");
        }
        if (connection.getDatabase() != null) {
            return uri;
        }
        String dbName = mongoConfiguration.getDbName();
        if (dbName != null) {
            return uri + '/' + dbName;
        }
        return uri;
    }
}
