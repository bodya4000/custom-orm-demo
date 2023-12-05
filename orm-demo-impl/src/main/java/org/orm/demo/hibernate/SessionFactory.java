package org.orm.demo.hibernate;

import org.orm.demo.hibernate.db.QueryManager;
import org.orm.demo.hibernate.db.QueryProcessor;

import javax.sql.DataSource;

public class SessionFactory {
    private final DataSource dataSource;

    public SessionFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Session createSession() {
        return new Session(dataSource);
    }}
