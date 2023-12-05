package org.orm.demo;

import org.orm.demo.entity.Person;
import org.orm.demo.hibernate.SessionFactory;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class StartDemo {

    public static void main(String[] args) {
        DataSource dataSource = initializeDataSource();

        SessionFactory sessionFactory = new SessionFactory(dataSource);
        var session = sessionFactory.createSession();
        var person = session.find(Person.class, 37L);
        person.setFirstName("axaxaaxxaaaxaxxa");
        session.close();
    }


    private static PGSimpleDataSource initializeDataSource() {
        PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
        pgSimpleDataSource.setURL("jdbc:postgresql://localhost:5432/hibernate-demo-db");
        pgSimpleDataSource.setUser("postgres");
        pgSimpleDataSource.setPassword("postgres");

        return pgSimpleDataSource;
    }
}
