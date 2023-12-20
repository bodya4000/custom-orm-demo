package org.orm.project.utilities.FieldHelper;


import org.orm.project.core.annotations.Column;
import org.orm.project.core.annotations.Id;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

public class FieldHelper {
    public static String getColumnAnnotationName(Field field) {
        if (hasAnnotation(field, Id.class)) {
            return "id";
        } else if (hasAnnotation(field, Column.class)) {
            return field.getAnnotation(Column.class).name();
        } else {
            return field.getName();
        }
    }
    public static  <T extends Annotation> boolean hasAnnotation(Field field, Class<T> annotationType) {
        return Arrays.stream(field.getDeclaredAnnotations()).anyMatch(annotation -> annotation == field.getAnnotation(annotationType));
    }
    public static void setAccessibleToAllFieldsTrue(Field[] fields) {
        Arrays.stream(fields).forEach(field -> field.setAccessible(true));
    }
    public static Field[] sortFieldsByName(Field[] fields) {
        return  Arrays.stream(fields).sorted(Comparator.comparing(Field::getName)).toArray(Field[]::new);
    }

    public static  <T> Field[] getFieldWithoutAnnotation(T entity, Class<? extends Annotation> annotationType) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> field.getDeclaredAnnotation(annotationType) == null)
                .toArray(Field[]::new);
    }

    public static  <T> List<Object> filterFieldsOnSpecificName(Field[] fields, T entity, String fieldName) {
        return Arrays.stream(fields)
                .filter(field -> !field.getName().equals(fieldName))
                .map(field -> {
                    try {
                        field.setAccessible(true);
                        return field.get(entity);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

}
