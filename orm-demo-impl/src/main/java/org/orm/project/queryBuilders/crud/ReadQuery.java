package org.orm.project.queryBuilders.crud;

import org.orm.project.core.annotations.Table;
import org.orm.project.queryBuilders.Queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReadQuery {
    public static <T> PreparedStatement getFindByIdStatement(Class<T> type, Long id, Connection connection) throws SQLException {
        var tableName = type.getDeclaredAnnotation(Table.class).name();
        var sql = String.format(Queries.SELECT_BY_ID, tableName);
        var statement = connection.prepareStatement(sql);
        statement.setLong(1, id);
        System.out.println(sql);
        return statement;
    }

    public static  <T> PreparedStatement getFindAllStatement(Class<T> type, Connection connection) throws SQLException {
        var tableName = type.getDeclaredAnnotation(Table.class).name();
        var sql = String.format(Queries.SELECT_ALL, tableName);
        var statement = connection.prepareStatement(sql);
        System.out.println(sql);
        return statement;
    }
}
