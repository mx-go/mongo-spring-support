package com.github.mongo.support.mapper;

import com.github.mongo.support.utils.NameUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FieldInfo {
    private String fieldName;
    private String columnName;
    private Method getterMethod;
    private Method setterMethod;

    public static List<FieldInfo> parseField(Class<?> clazz) {
        final Field[] fields = clazz.getDeclaredFields();
        List<FieldInfo> fieldInfos = new ArrayList<>();
        Stream.of(fields).forEach(field -> {
            try {
                final Method getterMethod = clazz.getMethod("get" + NameUtil.capitalize(field.getName()));
                final Method setterMethod = clazz.getMethod("set" + NameUtil.capitalize(field.getName()), field.getType());
                if (getterMethod.getReturnType().equals(field.getType())) {
                    fieldInfos.add(new FieldInfo(field.getName(), field.getName(), getterMethod, setterMethod));
                }
            } catch (NoSuchMethodException e) {
                //ignore exception: just skip this field
            }
        });
        return fieldInfos;
    }
}
