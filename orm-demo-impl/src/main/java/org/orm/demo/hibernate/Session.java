package org.orm.demo.hibernate;

import lombok.SneakyThrows;

import org.orm.demo.hibernate.business.EntityService;
import org.orm.demo.hibernate.db.QueryProcessor;
import org.orm.demo.hibernate.record.EntryKey;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Session {
    private final DataSource dataSource;
    private final EntityService entityService;
    private final Map<EntryKey<?>, Object> cache = new HashMap<>();
    private final Map<Class<?>, Set<Object>> cacheSet = new HashMap<>();
    private final Map<EntryKey<?>, Object[]> snapShotEntity = new HashMap<>();
    public Session(DataSource dataSource) {
        this.entityService = new EntityService();
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
            int status = entityService.deleteResultSet(type, id, connection);
            if (status <= 0) {
                throw new SQLException("delete didnt performed");
            }
        }
    }

    @SneakyThrows
    public <T> void persist(T entity) {
        try (var connection = dataSource.getConnection()) {
            int status = entityService.insertResultSet(entity, connection);
        }
    }

    public void close() {
        cache.entrySet().stream()
                .filter(this::dirtyChecking)
                .forEach(this::performUpdate);
    }
    @SneakyThrows
    private boolean dirtyChecking(Map.Entry<EntryKey<?>, Object> objectEntry) {
        ArrayList<Object> firstEntity = new ArrayList<>(Arrays.stream(snapShotEntity.get(objectEntry.getKey())).toList());
        ArrayList<Object> secondEntity = EntityService.createListFromObjectFields(objectEntry);
        return !Objects.equals(firstEntity, secondEntity);
    }




    @SneakyThrows
    private void performUpdate(Map.Entry<EntryKey<?>, Object> objectEntry) {
        var connection = dataSource.getConnection();
        int status = entityService.updateResultSet(objectEntry, connection);
    }


    @SneakyThrows
    private <T> T loadFromDB(EntryKey<T> key, Long id) {
        var type = key.type();
        var connection = dataSource.getConnection();
        ResultSet resultSetFromDB = entityService.findByIdResultSet(type, id, connection);
        if (resultSetFromDB.next()) {
            return entityService.getMappedEntity(key, resultSetFromDB, snapShotEntity);
        } else {
            return null;
        }
    }

    @SneakyThrows
    private <T> Set<Object> loadFromDB(Class<T> type) {
        try (var connection = dataSource.getConnection()) {
            ResultSet resultSetFromDB = entityService.findAllResultSet(type, connection);
            return entityService.getMappedSetOfEntity(type, resultSetFromDB);
        }
    }

}
