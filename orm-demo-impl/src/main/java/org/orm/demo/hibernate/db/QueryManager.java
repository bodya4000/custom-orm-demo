package org.orm.demo.hibernate.db;

import lombok.SneakyThrows;

import java.io.FileInputStream;
import java.util.Properties;

public class QueryManager {
    private static final String SELECT_BY_ID = "select * from %s where id = ?";
    private static final String SELECT_ALL = "select * from %s";
    private static final String INSERT = "insert into %s (%s) values (%s)";
    private static final String DELETE_BY_ID = "delete from %s where id = ?";
    private static final String UPDATE_BY_ID = "update %s set %s where id = ?";


    @SneakyThrows
    public QueryManager() {
    }

    public String getSelectAllQuery() {
        return SELECT_ALL;
    }

    public String getSelectByIdQuery() {
        return SELECT_BY_ID;
    }

    public String getInsertQuery() {
        return INSERT;
    }

    public String getDeleteByIdQuery() {
        return DELETE_BY_ID;
    }

    public String getUpdateByIdQuery() {
        return UPDATE_BY_ID;
    }


}
