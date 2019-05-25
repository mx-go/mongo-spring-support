package com.github.mongo.support.mapper;


import org.mongodb.morphia.annotations.Id;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityMapperManager {
    public static final EntityMapperManager INSTANCE = new EntityMapperManager();

    private final Map<Class<?>, EntityMapper<?>> cache = new HashMap<>();

    private EntityMapperManager() {
    }

    public <T> EntityMapper<T> getEntityMapper(Class<T> clazz) {
        if (!cache.containsKey(clazz)) {
            final EntityMapper<T> mapper = parseEntityMapper(clazz);
            cache.putIfAbsent(clazz, mapper);
        }
        @SuppressWarnings("unchecked") final EntityMapper<T> mapper = (EntityMapper<T>) cache.get(clazz);
        return mapper;
    }

    private <T> EntityMapper<T> parseEntityMapper(Class<T> clazz) {
        final EntityMapper<T> entityMapper = new EntityMapper<>();
        entityMapper.setClazz(clazz);
        entityMapper.setFieldInfos(FieldInfo.parseField(clazz));

        final List<Field> idAnnotations = Stream.of(clazz.getDeclaredFields())
                .filter(field -> {
                    final Id annotation = field.getAnnotation(Id.class);
                    return annotation != null;
                })
                .collect(Collectors.toList());
        if (idAnnotations.size() > 1) {
            throw new RuntimeException(String.format("There are %d field with annotation @Id, should be only one", idAnnotations.size()));
        } else if (idAnnotations.size() == 1) {
            final Optional<FieldInfo> idFieldInfoOpt = entityMapper.getFieldInfos().stream()
                    .filter(fieldInfo -> fieldInfo.getFieldName().equals(idAnnotations.get(0).getName()))
                    .findAny();
            if (!idFieldInfoOpt.isPresent()) {
                throw new RuntimeException(String.format("field with annotation @Id is not a valid field with getter and setter: %s.%s",
                        clazz.getName(), idAnnotations.get(0).getName()));
            }
            entityMapper.setIdField(idFieldInfoOpt.get());
        } else {
            entityMapper.setIdField(null);
        }
        return entityMapper;
    }
}
