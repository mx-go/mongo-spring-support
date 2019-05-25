package com.github.mongo.support.mongo;

import lombok.Getter;
import lombok.Setter;
import org.mongodb.morphia.Datastore;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@Setter
@Getter
class DatastoreHandler implements InvocationHandler {
    private final MongoDataStoreFactoryBean factory;
    private final String dbName;
    private Datastore delegate;

    DatastoreHandler(MongoDataStoreFactoryBean factory, String dbName, Datastore datastore) {
        this.factory = factory;
        this.dbName = dbName;
        this.delegate = datastore;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        switch (methodName) {
            case "toString":
                return "Proxy-Datastore-db:" + dbName;
            case "use":
                return factory.getOrCreate((String) args[0], null);
            case "getDatastoreByPrefix":
                return factory.getOrCreate((String) args[0], args[1] + "_%s");
            case "getDatastoreBySuffix":
                return factory.getOrCreate((String) args[0], "%s_" + args[1]);
            default:
                return method.invoke(delegate, args);
        }
    }
}
