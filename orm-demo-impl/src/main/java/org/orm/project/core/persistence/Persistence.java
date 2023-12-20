package org.orm.project.core.persistence;

import com.zaxxer.hikari.HikariDataSource;
import org.orm.project.core.annotations.Table;
import org.orm.project.queryBuilders.crud.CreateTableQuery;
import org.orm.project.utilities.reflectionHelper.ClassScanner;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class Persistence {

    private static HikariDataSource dataSource;

    public static EntityManagerFactory createEntityManagerFactory(HikariDataSource currentDataSource) throws IOException, ClassNotFoundException, SQLException {
        dataSource = currentDataSource;
        generateSchema();
        return new EntityManagerFactory(dataSource);
    }

    private static void generateSchema() throws SQLException, IOException, ClassNotFoundException {
        List<Class<?>> entityClasses = ClassScanner.getEntityClasses();
        for (var type : entityClasses) {
            var connection = dataSource.getConnection();
            var tableName = type.getDeclaredAnnotation(Table.class).name();
            ResultSet resultSet = connection.getMetaData().getTables(null, null, tableName, null);
            if (!resultSet.next()) {
                createTable(type, connection);
            }
        }
    }

    private static void createTable(Class<?> type, Connection connection) throws SQLException {
        CreateTableQuery.createTable(type, connection);
    }
}