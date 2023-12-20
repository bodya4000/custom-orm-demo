package org.orm.project.utilities.mappingHelper;

import lombok.SneakyThrows;
import org.orm.project.entity.record.EntryKey;
import org.orm.project.utilities.FieldHelper.FieldHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Map;

public class MappingHelper {

    @SneakyThrows
    private static <T> T map(ResultSet resultSetFromDB, Class<T> type, Field[] fields, FieldMapping<T> mapping) {
        FieldHelper.setAccessibleToAllFieldsTrue(fields);
        var entity = getEntity(type);
        for (int i =0; i < fields.length; i++) {
            var field = fields[i];
            var columnName = FieldHelper.getColumnAnnotationName(field);
            Object valueDB = resultSetFromDB.getObject(columnName);
            mapping.apply(fields[i], i,  entity, valueDB);
        }
        return entity;
    }

    // * overloaded method to map an entity and create snapshot
    @SneakyThrows
    public static <T> T mapFieldsFromResultSetAndCreateSnapshot(ResultSet resultSetFromDB, EntryKey<T> key, Map<EntryKey<?>, Object[]> snapShotEntity) {
        var type = key.type();
        var fields = FieldHelper.sortFieldsByName(type.getDeclaredFields());
        final var snapshotCopy = new Object[fields.length];
        var newEntity = map(resultSetFromDB, type, fields,((field, i, entity, valueDB) -> {
            field.set(entity, valueDB);
            snapshotCopy[i] = valueDB;
        }));
        snapShotEntity.put(key, snapshotCopy);
        return newEntity;
    }

    // * overloaded method just to map an entity
    @SneakyThrows
    public static <T> T mapFieldsFromResultSet(ResultSet resultSetFromDB, Class<T> type) {
        var entity = getEntity(type);
        Field[] fields = entity.getClass().getDeclaredFields();
        FieldHelper.setAccessibleToAllFieldsTrue(fields);

        return map(resultSetFromDB, type, fields,((field, i, entityParam, valueDB) -> {
            field.set(entityParam, valueDB);
        }));
    }

    private static <T> T getEntity(Class<T> type) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return type.getConstructor().newInstance();
    }
}
