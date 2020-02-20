package com.github.mongo.support.dao;

import com.github.mongo.support.mapper.EntityMapper;
import com.github.mongo.support.mapper.EntityMapperManager;
import com.github.mongo.support.mapper.FieldInfo;
import com.github.mongo.support.mongo.DatastoreExt;
import com.google.common.base.Strings;
import com.mongodb.AggregationOptions;
import com.mongodb.Cursor;
import com.mongodb.DBObject;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础方法实现类
 *
 * @param <T>
 */
@Getter
@Setter
public abstract class BaseDaoImpl<T> implements BaseDao<T> {

    private DatastoreExt datastore;
    private Class<T> clazz;

    public BaseDaoImpl(DatastoreExt datastoreExt, Class<T> clazz) {
        this.datastore = datastoreExt;
        this.clazz = clazz;
    }

    @Override
    public DatastoreExt getDatastore() {
        return this.datastore;
    }

    @Override
    public String save(T entity) {
        return datastore.save(entity).getId().toString();
    }

    @Override
    public List<T> queryList(T condition) {
        final Query<T> query = createQuery(condition);
        return query.asList();
    }

    @Override
    public List<T> queryList(T condition, int offset, int limit) {
        final Query<T> query = createQuery(condition);
        FindOptions findOptions = new FindOptions();
        findOptions.skip(offset);
        findOptions.limit(limit);
        return query.asList(findOptions);
    }

    @Override
    public T queryById(String id) {
        final Query<T> query = createQuery();
        query.field("_id").equal(new ObjectId(id));
        return query.get();
    }

    @Override
    public List<T> queryByIds(List<String> ids) {
        final Query<T> query = createQuery();
        List<ObjectId> objectIds = ids.stream().map(ObjectId::new).collect(Collectors.toList());
        query.field("_id").in(objectIds);
        return query.asList();
    }

    @Override
    public long queryCount(T condition) {
        final Query<T> query = createQuery(condition);
        return datastore.getCount(query);
    }

    @Override
    public Query<T> createQuery() {
        return datastore.createQuery(clazz);
    }

    @Override
    public UpdateOperations<T> createUpdateOperations() {
        return datastore.createUpdateOperations(clazz);
    }

    public Query<T> createQuery(T condition) {
        final EntityMapper<T> entityMapper = EntityMapperManager.INSTANCE.getEntityMapper(clazz);
        final Query<T> query = createQuery();
        try {
            final String id = (String) entityMapper.getIdField().getGetterMethod().invoke(condition);
            if (id != null) {
                query.field("_id").equal(new ObjectId(id));
            }

            for (final FieldInfo fieldInfo : entityMapper.getFieldInfos()) {
                if (!fieldInfo.getFieldName().equals(entityMapper.getIdField().getFieldName())) {
                    final Object fieldValue = fieldInfo.getGetterMethod().invoke(condition);
                    if (fieldValue instanceof String) {
                        if (!Strings.isNullOrEmpty((String) fieldValue)) {
                            query.field(fieldInfo.getFieldName()).equal((String) fieldValue);
                        }
                    } else {
                        if (null != fieldValue) {
                            query.field(fieldInfo.getFieldName()).equal(fieldValue);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return query;
    }

    public Query<T> createQuery(T condition, int offset, int limit) {
        Query<T> query = createQuery(condition);
        query.offset(offset);
        query.limit(limit);
        return query;
    }

    /**
     * 聚合查询
     *
     * @param pipeline pipeline
     * @param options  AggregationOptions
     * @return
     */
    public Cursor aggregate(final List<? extends DBObject> pipeline, final AggregationOptions options) {
        return datastore.getCollection(clazz).aggregate(pipeline, options);
    }
}