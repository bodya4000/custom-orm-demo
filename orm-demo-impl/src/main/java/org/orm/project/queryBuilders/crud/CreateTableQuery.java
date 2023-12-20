package org.orm.project.queryBuilders.crud;
import org.orm.project.core.annotations.Column;
import org.orm.project.core.annotations.Table;
import org.orm.project.queryBuilders.Queries;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.StringJoiner;

public class CreateTableQuery {
    public static void createTable(Class<?> type, Connection connection) throws SQLException {
        Field[] fields = type.getDeclaredFields();
        String sqlToCrateTable = Queries.CREATE_TABLE;
        StringJoiner joiner = new StringJoiner(",");
        for (var field: fields) {
            String str;
            if (field.getType() == Integer.class || field.getType() == Long.class) {
                if (field.getDeclaredAnnotation(Column.class) != null){
                    str = field.getDeclaredAnnotation(Column.class).name() + " " + "bigint";
                } else{
                    str = "id" + " " + "bigserial";
                }

            } else {
                str = field.getDeclaredAnnotation(Column.class).name() + " " + "text";
            }
            joiner.add(str);
        }
        var finalizedSql = String.format(sqlToCrateTable, type.getDeclaredAnnotation(Table.class).name(), joiner);
        System.out.println(finalizedSql);
        var preparedStatement = connection.prepareStatement(finalizedSql);
        preparedStatement.executeUpdate();
    }
}
