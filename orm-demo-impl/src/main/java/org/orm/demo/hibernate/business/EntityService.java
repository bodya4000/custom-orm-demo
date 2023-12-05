package org.orm.demo.hibernate.business;

import lombok.SneakyThrows;
import org.orm.demo.hibernate.annotations.Column;
import org.orm.demo.hibernate.annotations.Id;
import org.orm.demo.hibernate.annotations.Table;
import org.orm.demo.hibernate.db.QueryManager;
import org.orm.demo.hibernate.db.QueryProcessor;
import org.orm.demo.hibernate.record.EntryKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;



public class EntityService {

    private QueryProcessor queryProcessor;

    public EntityService() {
        this.queryProcessor = new QueryProcessor(new QueryManager());
    }

    /*-------------------------------------- ResultSets from db ------------------------------------------*/

    public <T> ResultSet findAllResultSet(Class<T> type, Connection connection) throws SQLException {
        PreparedStatement statement = queryProcessor.getFindAllStatement(type, connection);
        return statement.executeQuery();
    }
    public <T> ResultSet findByIdResultSet(Class<T> type, Long id, Connection connection) throws SQLException {
        var statement = queryProcessor.getFindByIdStatement(type, id, connection);
        return statement.executeQuery();
    }
    public <T> int insertResultSet(T entity, Connection connection) throws SQLException {
        var statement = queryProcessor.getInsertStatement(entity, connection);
        return statement.executeUpdate();
    }
    public <T> int deleteResultSet(Class<T> type, Long id, Connection connection) throws SQLException {
        var statement =  queryProcessor.getDeleteStatement(type, connection);
        statement.setLong(1, id);
        return statement.executeUpdate();
    }
    @SneakyThrows
    public int updateResultSet(Map.Entry<EntryKey<?>, Object> objectEntry, Connection connection) {
        var statement = queryProcessor.getUpdateStatement(objectEntry, connection);
        var entityType = objectEntry.getKey().type();
        Field[] fields = sortFieldsByName(entityType.getDeclaredFields());
        setAccessibleToAllFieldsTrue(fields);
        setUpdatesStatement(objectEntry, statement, fields);
        return statement.executeUpdate();
    }

    /*-------------------------------------- get entities logic ------------------------------------------*/

    @SneakyThrows
    public  <T> Set<Object> getMappedSetOfEntity(Class<T> type, ResultSet resultSetFromDB) {
        Set<Object> setOfEntity = new HashSet<>();
        while (resultSetFromDB.next()) {
            var entity = mapEachField(resultSetFromDB, type);
            setOfEntity.add(type.cast(entity));
        }
        return setOfEntity;
    }
    @SneakyThrows
    public  <T> T getMappedEntity(EntryKey<T> key, ResultSet resultSetFromDB, Map<EntryKey<?>, Object[]>  snapShotEntity){
        var type = key.type();
        var entity = mapEachField(resultSetFromDB, key, snapShotEntity);
        return type.cast(entity);
    }



    /*-------------------------------------- mapping entities fields logic ------------------------------------------*/

    // * overloaded method to map an entity and create snapshot
    @SneakyThrows
    private <T> T mapEachField(ResultSet resultSetFromDB, EntryKey<T> key , Map<EntryKey<?>, Object[]> snapShotEntity)  {
        var entity = getEntity(key.type());
        Field[] fields = sortFieldsByName(key.type().getDeclaredFields());
        setAccessibleToAllFieldsTrue(fields);
        var snapshotCopy = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            String columnName = getColumnAnnotationName(fields[i]);
            var valueDB = resultSetFromDB.getObject(columnName);
            fields[i].set(entity, valueDB);
            snapshotCopy[i] = valueDB;
        }
        System.out.println(Arrays.toString(snapshotCopy));
        snapShotEntity.put(key, snapshotCopy);
        return entity;
    }

    // * overloaded method just to map an entity
    @SneakyThrows
    private <T> T mapEachField(ResultSet resultSetFromDB, Class<T> type)  {
        var entity = getEntity(type);
        Field[] fields = entity.getClass().getDeclaredFields();
        setAccessibleToAllFieldsTrue(fields);
        for (Field field : fields) {
            String columnName = getColumnAnnotationName(field);
            var valueDB = resultSetFromDB.getObject(columnName);
            field.set(entity, valueDB);
        }
        return entity;
    }




    private <T> T getEntity(Class<T> type) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return type.getConstructor().newInstance();
    }
    private <T extends Annotation> boolean hasAnnotation(Field field, Class<T> annotationType) {
        return Arrays.stream(field.getDeclaredAnnotations()).anyMatch(annotation -> annotation == field.getAnnotation(annotationType));
    }

    private String getColumnAnnotationName(Field field) {
        if (hasAnnotation(field, Id.class)) {
            return "id";
        } else if (hasAnnotation(field, Column.class)) {
            return field.getAnnotation(Column.class).name();
        } else {
            return field.getName();
        }
    }



    @SneakyThrows
    private void setUpdatesStatement(Map.Entry<EntryKey<?>, Object> objectEntry, PreparedStatement preparedStatement, Field[] fields) {
        Field[] fieldsWithoutId = filterWithoutId(fields);
        for (int i = 0; i < fields.length-1; i++) {
            var objectsFiledValue = fieldsWithoutId[i].get(objectEntry.getValue());
            preparedStatement.setObject(i+1, objectsFiledValue);
        }
        Field idField = Arrays.stream(fields).filter(field -> field.getName().equals("id")).findFirst().get();
        preparedStatement.setObject(fields.length, idField.get(objectEntry.getValue()));
    }

    /*-------------------------------------- static methods ------------------------------------------*/


    private static Field[] sortFieldsByName(Field[] fields) {
        return  Arrays.stream(fields).sorted(Comparator.comparing(Field::getName)).toArray(Field[]::new);
    }
    private static ArrayList<Object> createList(Map.Entry<EntryKey<?>, Object> objectEntry, Field[] fields) throws IllegalAccessException {
        ArrayList<Object> secondEntity = new ArrayList<>();
        setAccessibleToAllFieldsTrue(fields);
        for (Field field : fields) {
            Object value = field.get(objectEntry.getValue());
            secondEntity.add(value);
        }
        return secondEntity;
    }
    public static ArrayList<Object> createListFromObjectFields(Map.Entry<EntryKey<?>, Object> objectEntry) throws IllegalAccessException {
        Field[] fields = objectEntry.getValue().getClass().getDeclaredFields();
        fields = sortFieldsByName(fields);
        return createList(objectEntry, fields);
    }
    private static Field[] filterWithoutId(Field[] fields) {
        return Arrays.stream(fields).filter(field -> !field.getName().equals("id")).toArray(Field[]::new);
    }
    private static void setAccessibleToAllFieldsTrue(Field[] fields) {
        Arrays.stream(fields).forEach(field -> field.setAccessible(true));
    }


}
