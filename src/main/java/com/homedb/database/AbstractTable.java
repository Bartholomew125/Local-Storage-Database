package com.homedb.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTable<T> implements Table<T> {

    private final Database database;
    protected final String tableName;
    protected final List<String> columns;

    protected final String INSERT_SQL_PLACEHOLDER;
    protected final String INSERT_SQL;
    protected final String SELECT_SQL;
    protected final String SELECT_ALL_SQL;
    protected final String RENAME_SQL;
    protected final String DELETE_SQL;

    protected AbstractTable(Database database, String tableName, List<String> columns) {
        this.database = database;
        this.tableName = tableName;
        this.columns = columns;
        List<String> questionMarks = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) questionMarks.add("?");
        this.INSERT_SQL_PLACEHOLDER = "("+String.join(", ", questionMarks)+")";
        this.INSERT_SQL = "INSERT INTO "+tableName
            +" ("+String.join(", ", columns)+") "
            +" VALUES"
            +" "+this.INSERT_SQL_PLACEHOLDER;
        this.SELECT_SQL = "SELECT * FROM "+tableName
            +" WHERE id=?"
            +" AND deleted_at IS NULL";
        this.SELECT_ALL_SQL = "SELECT * FROM "+tableName
            +" WHERE deleted_at IS NULL"
            +" ORDER BY %s DESC NULLS LAST"
            +" LIMIT ? OFFSET ?";
        this.RENAME_SQL = "UPDATE "+tableName
            +" SET title = ?"
            +" WHERE id = ?";
        this.DELETE_SQL = "UPDATE "+tableName
            +" SET deleted_at = datetime('now') WHERE id = ?";
    }

    @Override
    public String getName() {
        return this.tableName;
    }

    protected PreparedStatement createPreparedStatement(String sql) throws SQLException {
        return this.database.createPreparedStatement(sql);
    }

    @Override
    public int rename(String itemID, String title) {
        try (PreparedStatement stmt = createPreparedStatement(RENAME_SQL)) {
            stmt.setString(1, title);
            stmt.setString(2, itemID);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int delete(String itemID) {
        try (PreparedStatement stmt = createPreparedStatement(DELETE_SQL)) {
            stmt.setString(1, itemID);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
