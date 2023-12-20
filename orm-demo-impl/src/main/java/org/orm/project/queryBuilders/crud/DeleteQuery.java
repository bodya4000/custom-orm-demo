package org.orm.project.queryBuilders.crud;

import org.orm.project.core.annotations.Table;
import org.orm.project.queryBuilders.Queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeleteQuery {
    public static  <T> PreparedStatement getDeleteStatement(Class<T> type, Connection connection) throws SQLException {
        var tableName = type.getDeclaredAnnotation(Table.class).name();
        var sql = String.format(Queries.DELETE_BY_ID, tableName);
        System.out.println(sql);
        return connection.prepareStatement(sql);
    }
}
