package com.max.mongo.support.mapper;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class EntityMapper<T> {
    private Class<T> clazz;
    private FieldInfo idField;
    private List<FieldInfo> fieldInfos;
}