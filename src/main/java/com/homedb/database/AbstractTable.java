package com.homedb.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class AbstractTable<T> implements Table<T> {

    private final Database database;
    private final String tableName;

    protected AbstractTable(Database database, String tableName) {
        this.database = database;
        this.tableName = tableName;
    }

    @Override
    public String getName() {
        return this.tableName;
    }

    protected PreparedStatement createPreparedStatement(String sql) throws SQLException {
        return this.database.createPreparedStatement(sql);
    }
}
