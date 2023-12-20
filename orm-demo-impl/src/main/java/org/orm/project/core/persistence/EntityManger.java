package org.orm.project.core.persistence;

import lombok.SneakyThrows;

import org.orm.project.entity.record.EntryKey;
import org.orm.project.managers.DataAccessManager;
import org.orm.project.utilities.FieldHelper.FieldHelper;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class EntityManger {
    private final DataSource dataSource;
    private final DataAccessManager dataAccessManager;
    private final Map<EntryKey<?>, Object> cache = new HashMap<>();
    private final Map<Class<?>, Set<Object>> cacheSet = new HashMap<>();
    private final Map<EntryKey<?>, Object[]> snapShotEntity = new HashMap<>();
    public EntityManger(DataSource dataSource) {
        this.dataAccessManager = new DataAccessManager();
        this.dataSource = dataSource;
    }

    public <T> T find(Class<T> type, Long id) {
        var key = new EntryKey<T>(type, id);
        var result = cache.computeIfAbsent(key, key1 -> {
            var entity = loadFromDB(key, id);

            if (entity == null) {
                throw new RuntimeException("there is now entity with id - " + id);
            }

            return entity;
        });
        return type.cast(result);
    }

    public <T> Set<Object> findAll(Class<T> type) {
        return cacheSet.computeIfAbsent(type, typeEntity -> loadFromDB(type));
    }

    @SneakyThrows
    public <T> void remove(Class<T> type, Long id) {
        try (var connection = dataSource.getConnection()) {
            int status = dataAccessManager.deleteResultSet(type, id, connection);
            if (status <= 0) {
                throw new SQLException("delete didnt performed");
            }
        }
    }

    @SneakyThrows
    public <T> void persist(T entity) {
        try (var connection = dataSource.getConnection()) {
            int status = dataAccessManager.insertResultSet(entity, connection);
        }
    }

    public void close() {
        cache.entrySet().stream()
                .filter(this::dirtyChecking)
                .forEach(this::performUpdate);
    }
    @SneakyThrows
    private boolean dirtyChecking(Map.Entry<EntryKey<?>, Object> objectEntry) {
        ArrayList<Object> oldEntity = new ArrayList<>(Arrays.stream(snapShotEntity.get(objectEntry.getKey())).toList());
        ArrayList<Object> updated = createListFromObjectFields(objectEntry);
        return !Objects.equals(oldEntity, updated);
    }

    private static ArrayList<Object> createListFromObjectFields(Map.Entry<EntryKey<?>, Object> objectEntry) throws IllegalAccessException {
        Field[] fields = objectEntry.getValue().getClass().getDeclaredFields();
        fields = FieldHelper.sortFieldsByName(fields);
        return createList(objectEntry, fields);
    }

    private static ArrayList<Object> createList(Map.Entry<EntryKey<?>, Object> objectEntry, Field[] fields) throws IllegalAccessException {
        ArrayList<Object> secondEntity = new ArrayList<>();
        FieldHelper.setAccessibleToAllFieldsTrue(fields);
        for (Field field : fields) {
            Object value = field.get(objectEntry.getValue());
            secondEntity.add(value);
        }
        return secondEntity;
    }




    @SneakyThrows
    private void performUpdate(Map.Entry<EntryKey<?>, Object> objectEntry) {
        var connection = dataSource.getConnection();
        int status = dataAccessManager.updateResultSet(objectEntry, connection);
    }


    @SneakyThrows
    private <T> T loadFromDB(EntryKey<T> key, Long id) {
        var type = key.type();
        var connection = dataSource.getConnection();
        ResultSet resultSetFromDB = dataAccessManager.findByIdResultSet(type, id, connection);
        if (resultSetFromDB.next()) {
            return dataAccessManager.getMappedEntity(key, resultSetFromDB, snapShotEntity);
        } else {
            return null;
        }
    }

    @SneakyThrows
    private <T> Set<Object> loadFromDB(Class<T> type) {
        try (var connection = dataSource.getConnection()) {
            ResultSet resultSetFromDB = dataAccessManager.findAllResultSet(type, connection);
            return dataAccessManager.getMappedSetOfEntity(type, resultSetFromDB);
        }
    }

}