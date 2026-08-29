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

    public int insertImage(int userId, String imageId) {
        String sql = "INSERT INTO user_images VALUES (?, ?)";
        try (PreparedStatement stmt = this.database.createPreparedStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, imageId);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int insertVideo(int userId, String videoId) {
        String sql = "INSERT INTO user_videos VALUES (?, ?)";
        try (PreparedStatement stmt = this.database.createPreparedStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, videoId);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

    }

    public int insertUser(String username, String passwordHash) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (PreparedStatement stmt = this.database.createPreparedStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
