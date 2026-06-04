package com.homedb.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.homedb.Config;

public class Database {

    private static final Path FILE = Config.DATA_DIR.resolve("index.db");

    private final Connection conn;

    public Database() {
        try {
            this.conn = DriverManager.getConnection("jdbc:sqlite:" + Database.FILE);
            if (Files.size(Database.FILE) == 0) {
                initSchema();
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private void initSchema() throws SQLException {
        String schema = readSchema();
        try (Statement stmt = this.conn.createStatement()) {
            stmt.executeUpdate(schema);
        }
    }

    private static String readSchema() {
        try (InputStream is = Database.class.getResourceAsStream("/sql/schema.sql")) {
            if (is == null) throw new RuntimeException("Schema file not found on classpath");
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read schema file", e);
        }
    }

    public PreparedStatement createPreparedStatement(String sql) throws SQLException {
        return this.conn.prepareStatement(sql);   
    }

}

