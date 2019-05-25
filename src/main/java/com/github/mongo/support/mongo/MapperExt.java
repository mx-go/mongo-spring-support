package com.github.mongo.support.mongo;

import org.mongodb.morphia.mapping.Mapper;

/**
 * 支持设定collection的前后缀，比如 2019_%s, %s_2019
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