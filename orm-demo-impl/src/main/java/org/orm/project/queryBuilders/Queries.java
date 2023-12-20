package org.orm.project.queryBuilders;

public class Queries {
    public static final String SELECT_BY_ID = "select * from %s where id = ?";
    public static final String SELECT_ALL = "select * from %s";
    public static final String INSERT = "insert into %s (%s) values (%s)";
    public static final String DELETE_BY_ID = "delete from %s where id = ?";
    public static final String UPDATE_BY_ID = "update %s set %s where id = ?";

    public static final String CREATE_TABLE = "create table %s (%s)";
}
