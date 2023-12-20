package org.orm.project.queryBuilders.crud;

import org.orm.project.core.annotations.Column;
import org.orm.project.core.annotations.Table;
import org.orm.project.entity.record.EntryKey;
import org.orm.project.queryBuilders.Queries;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;

public class UpdateQuery {

    public static PreparedStatement getUpdateStatement(Map.Entry<EntryKey<?>, Object> objectEntry, Connection connection) throws SQLException {
        var type = objectEntry.getKey().type();
        var tableName = type.getDeclaredAnnotation(Table.class).name();
        var fields = type.getDeclaredFields();
        var values = generateValuesSql(fields);
        var sql = String.format(Queries.UPDATE_BY_ID, tableName, values);
        System.out.println(sql);
        return connection.prepareStatement(sql);
    }

    private static String generateValuesSql(Field[] fields) {
        var joiner = new StringJoiner(", ");
        Arrays.stream(fields)
                .filter(field ->!field.getName().equals("id"))
                .forEach(field -> {
                    var columnName = field.getDeclaredAnnotation(Column.class).name();
                    joiner.add(columnName + "=?");
                });
        return joiner.toString();
    }

}
