package org.orm.demo.hibernate.db;


import lombok.SneakyThrows;
import org.orm.demo.hibernate.annotations.Column;
import org.orm.demo.hibernate.annotations.Id;
import org.orm.demo.hibernate.annotations.Table;
import org.orm.demo.hibernate.record.EntryKey;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class QueryProcessor {

    private final QueryManager queryManager;

    public QueryProcessor(QueryManager queryManager) {
        this.queryManager = queryManager;
    }


    public <T> PreparedStatement getFindByIdStatement(Class<T> type, Long id, Connection connection) throws SQLException {
        var tableName = type.getDeclaredAnnotation(Table.class).name();
        var sql = String.format(queryManager.getSelectByIdQuery(), tableName);
        var statement = connection.prepareStatement(sql);
        statement.setLong(1, id);

        System.out.println(sql);

        return statement;
    }

    public <T> PreparedStatement getFindAllStatement(Class<T> type, Connection connection) throws SQLException {
        var tableName = type.getDeclaredAnnotation(Table.class).name();
        var sql = String.format(queryManager.getSelectAllQuery(), tableName);
        var statement = connection.prepareStatement(sql);
        System.out.println(sql);
        return statement;
//        return statement.executeQuery();
    }

    public <T> PreparedStatement getDeleteStatement(Class<T> type, Connection connection) throws SQLException {
        var tableName = type.getDeclaredAnnotation(Table.class).name();
        var sql = String.format(queryManager.getDeleteByIdQuery(), tableName);
        System.out.println(sql);
        return connection.prepareStatement(sql);
    }

    @SneakyThrows
    public <T> PreparedStatement getInsertStatement(T entity, Connection connection) {
        var fields = entity.getClass().getDeclaredFields();
        var stringSql = getSQLInsertForEntity(entity);
        var statement = connection.prepareStatement(stringSql);
        var listOfFields = filterFieldsOnId(fields, entity);
        insertStatement(statement, entity, listOfFields);
        return statement;
    }

    public PreparedStatement getUpdateStatement(Map.Entry<EntryKey<?>, Object> objectEntry, Connection connection) throws SQLException {
        var type = objectEntry.getKey().type();
        var tableName = type.getDeclaredAnnotation(Table.class).name();
        var fields = type.getDeclaredFields();
        var values = generateValuesSql(fields);
        var sql = String.format(queryManager.getUpdateByIdQuery(), tableName, values);
        System.out.println(sql);
        return connection.prepareStatement(sql);
    }

    private String generateValuesSql(Field[] fields) {
        var joiner = new StringJoiner(", ");
        Arrays.stream(fields)
                .filter(field ->!field.getName().equals("id"))
                .forEach(field -> {
                    var columnName = field.getDeclaredAnnotation(Column.class).name();
                    joiner.add(columnName + "=?");
                });
        return joiner.toString();
    }

    @SneakyThrows
    private  <T>void insertStatement(PreparedStatement statement, T entity, List<Object> values) {
        for (int i = 0; i <entity.getClass().getDeclaredFields().length-1; i++) {
            statement.setObject(i+1, values.get(i));
        }
    }



    @SneakyThrows
    private <T> String getSQLInsertForEntity(T entity) {
        var joiner1 = new StringJoiner(", ");
        var joiner2 = new StringJoiner(", ");

        var tableName = entity.getClass().getDeclaredAnnotation(Table.class).name();

        Field[] fields = getFieldWithoutIdAnnotation(entity);

        for (Field field: fields) {
            field.setAccessible(true);
            var columnName = field.getAnnotation(Column.class).name();
            joiner1.add(columnName);
            joiner2.add("?");
        }
        return String.format(queryManager.getInsertQuery(), tableName, joiner1.toString(), joiner2.toString());
    }

    private <T> Field[] getFieldWithoutIdAnnotation(T entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> field.getDeclaredAnnotation(Id.class) == null)
                .toArray(Field[]::new);
    }

    private <T> List<Object> filterFieldsOnId(Field[] fields, T entity) {
        return Arrays.stream(fields)
                .filter(field -> !field.getName().equals("id"))
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
