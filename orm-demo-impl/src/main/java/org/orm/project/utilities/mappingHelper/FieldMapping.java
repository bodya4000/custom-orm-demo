package org.orm.project.utilities.mappingHelper;

import java.lang.reflect.Field;

@FunctionalInterface
interface FieldMapping<T> {
    void apply(Field field, int i, T entity, Object valueDB) throws IllegalAccessException;
}
