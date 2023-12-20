package org.orm.project.managers;

import lombok.SneakyThrows;

import org.orm.project.core.annotations.Id;
import org.orm.project.entity.record.EntryKey;
import org.orm.project.queryBuilders.crud.CreateQuery;
import org.orm.project.queryBuilders.crud.DeleteQuery;
import org.orm.project.queryBuilders.crud.ReadQuery;
import org.orm.project.queryBuilders.crud.UpdateQuery;
import org.orm.project.utilities.FieldHelper.FieldHelper;
import org.orm.project.utilities.mappingHelper.MappingHelper;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class DataAccessManager {

    /*-------------------------------------- ResultSets from db ------------------------------------------*/

    public <T> ResultSet findAllResultSet(Class<T> type, Connection connection) throws SQLException {
        PreparedStatement statement = ReadQuery.getFindAllStatement(type, connection);
        return statement.executeQuery();
    }
    public <T> ResultSet findByIdResultSet(Class<T> type, Long id, Connection connection) throws SQLException {
        var statement = ReadQuery.getFindByIdStatement(type, id, connection);
        return statement.executeQuery();
    }
    public <T> int insertResultSet(T entity, Connection connection) throws SQLException {
        var statement = CreateQuery.getCreateStatement(entity, connection);
        return statement.executeUpdate();
    }
    public <T> int deleteResultSet(Class<T> type, Long id, Connection connection) throws SQLException {
        var statement =  DeleteQuery.getDeleteStatement(type, connection);
        statement.setLong(1, id);
        return statement.executeUpdate();
    }
    @SneakyThrows
    public int updateResultSet(Map.Entry<EntryKey<?>, Object> objectEntry, Connection connection) {
        var statement = UpdateQuery.getUpdateStatement(objectEntry, connection);
        var entityType = objectEntry.getKey().type();
        Field[] fields = FieldHelper.sortFieldsByName(entityType.getDeclaredFields());
        FieldHelper.setAccessibleToAllFieldsTrue(fields);
        setUpdatesStatement(objectEntry, statement, fields);
        return statement.executeUpdate();
    }

    @SneakyThrows
    private void setUpdatesStatement(Map.Entry<EntryKey<?>, Object> objectEntry, PreparedStatement preparedStatement, Field[] fields) {
        Field[] fieldsWithoutId = Arrays.stream(fields).filter(field -> !field.getName().equals("id")).toArray(Field[]::new);
        FieldHelper.setAccessibleToAllFieldsTrue(fields);
        for (int i = 0; i < fieldsWithoutId.length; i++) {
            var objectsFiledValue = fieldsWithoutId[i].get(objectEntry.getValue());
            preparedStatement.setObject(i+1, objectsFiledValue);
        }
        Field idField = Arrays.stream(fields).filter(field -> field.getName().equals("id")).findFirst().get();
        preparedStatement.setObject(fields.length, idField.get(objectEntry.getValue()));
    }
    /*-------------------------------------- get entities logic ------------------------------------------*/

    @SneakyThrows
    public  <T> Set<Object> getMappedSetOfEntity(Class<T> type, ResultSet resultSetFromDB) {
        Set<Object> setOfEntity = new HashSet<>();
        while (resultSetFromDB.next()) {
            var entity = MappingHelper.mapFieldsFromResultSet(resultSetFromDB, type);
            setOfEntity.add(type.cast(entity));
        }
        return setOfEntity;
    }
    @SneakyThrows
    public  <T> T getMappedEntity(EntryKey<T> key, ResultSet resultSetFromDB, Map<EntryKey<?>, Object[]>  snapShotEntity){
        var type = key.type();
        var entity = MappingHelper.mapFieldsFromResultSetAndCreateSnapshot(resultSetFromDB, key, snapShotEntity);
        return type.cast(entity);
    }


    /*-------------------------------------- static methods ------------------------------------------*/





}
