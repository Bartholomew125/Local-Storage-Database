package com.homedb.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsersTable {

    private final Database database;

    public UsersTable(Database database) {
        this.database = database;
    }

    public Integer getId(String username, String password_hash) {
        String sql = "SELECT id FROM users WHERE username = ? AND password_hash = ?";
        try (PreparedStatement stmt = this.database.createPreparedStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password_hash);
            System.out.println(stmt);
            ResultSet res = stmt.executeQuery();
            int user_id = res.getInt("id");
            if (user_id > 0) {
                return user_id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
