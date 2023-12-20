package org.orm.project.core.persistence;

import javax.sql.DataSource;

public class EntityManagerFactory {
    private final DataSource dataSource;

    public EntityManagerFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public EntityManger createEntityManger() {
        return new EntityManger(dataSource);
    }}
