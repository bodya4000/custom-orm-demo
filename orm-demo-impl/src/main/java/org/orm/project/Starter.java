package org.orm.project;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.orm.project.core.persistence.EntityManagerFactory;
import org.orm.project.core.persistence.Persistence;
import org.orm.project.entity.Person;

import java.io.IOException;
import java.sql.SQLException;

public class Starter {

    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
        HikariDataSource dataSource = initializeDataSource();


        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(dataSource);

        var em = entityManagerFactory.createEntityManger();

        var person = em.find(Person.class, 1L);

        person.setFirstName("Bodya");
        person.setLastName("Shraier");
        System.out.println(person);
        em.close();

    }


    private static HikariDataSource initializeDataSource() {

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:postgresql://localhost:5432/hibernate-demo-db");
        config.setUsername("postgres");
        config.setPassword("postgres");
        config.setMaximumPoolSize(20);

        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );

        return new HikariDataSource(config);
    }
}
