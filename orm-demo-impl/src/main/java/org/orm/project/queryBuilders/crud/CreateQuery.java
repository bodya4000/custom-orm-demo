package org.orm.project.queryBuilders.crud;

import lombok.SneakyThrows;
import org.orm.project.core.annotations.Column;
import org.orm.project.core.annotations.Id;
import org.orm.project.core.annotations.Table;
import org.orm.project.queryBuilders.Queries;
import org.orm.project.utilities.FieldHelper.FieldHelper;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.StringJoiner;

public class CreateQuery {

    @SneakyThrows
    public static  <T> PreparedStatement getCreateStatement(T entity, Connection connection) {
        var entitiesFields = entity.getClass().getDeclaredFields();
        var filteredFields = FieldHelper.filterFieldsOnSpecificName(entitiesFields, entity, "id");
        var stringSql = getFinishedSql(entity);

        var statement = connection.prepareStatement(stringSql);
        fillStatement(statement, entity, filteredFields);
        return statement;
    }

    @SneakyThrows
    private static <T> void fillStatement(PreparedStatement statement, T entity, List<Object> values) {
        for (int i = 0; i <entity.getClass().getDeclaredFields().length-1; i++) {
            var fieldValue = values.get(i);
            statement.setObject(i+1, fieldValue);
        }
    }

    @SneakyThrows
    private static <T> String getFinishedSql(T entity) {
        var joiner1 = new StringJoiner(", ");
        var joiner2 = new StringJoiner(", ");

        var tableName = entity.getClass().getDeclaredAnnotation(Table.class).name();

        Field[] fields = FieldHelper.getFieldWithoutAnnotation(entity, Id.class);

        for (Field field: fields) {
            field.setAccessible(true);
            var columnName = field.getAnnotation(Column.class).name();
            joiner1.add(columnName);
            joiner2.add("?");
        }
        return String.format(Queries.INSERT, tableName, joiner1.toString(), joiner2.toString());
    }

}
