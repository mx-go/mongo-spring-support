package com.max.mongo.support.mongo;

import org.mongodb.morphia.mapping.Mapper;

/**
 * 支持设定collection的前后缀，比如 2017_%s, %s_2017
 *
 * @author max
 */
public class MapperExt extends Mapper {
    private final String format;

    public MapperExt(String format) {
        this.format = format;
    }

    @Override
    public String getCollectionName(Object object) {
        return String.format(format, super.getCollectionName(object));
    }
}