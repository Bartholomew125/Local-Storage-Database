package com.homedb.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.homedb.content.Tag;


public class TagsTable {

    private final Database database;

    public TagsTable(Database database) {
        this.database = database;
    }

    public List<Tag> search(String likeName) {
        String sql = "SELECT id, name FROM tag_ids WHERE name LIKE ?";
        try (PreparedStatement stmt = this.database.createPreparedStatement(sql)) {
            stmt.setString(1, "%"+likeName+"%");
            ResultSet res = stmt.executeQuery();
            List<Tag> result = new ArrayList<>();
            while (res.next())
                result.add(new Tag(
                    res.getInt("id"),
                    res.getString("name")
                ));
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Tag> select(String itemID) {
        String sql = "SELECT id, name"
            +" FROM tags"
            +" JOIN tag_ids"
            +" ON tag_id = id"
            +" WHERE content_id = ?";
        try (PreparedStatement stmt = this.database.createPreparedStatement(sql)) {
            stmt.setString(1, itemID);
            System.out.println(stmt);
            ResultSet res = stmt.executeQuery();
            List<Tag> result = new ArrayList<>();
            while (res.next())
                result.add(new Tag(
                    res.getInt("id"),
                    res.getString("name")
                ));
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int insert(Tag tag, String itemID) {
        String sql = "INSERT INTO tags"
            +" VALUES (?, ?)";
        try (PreparedStatement stmt = this.database.createPreparedStatement(sql)) {
            stmt.setInt(1, tag.id());
            stmt.setString(2, itemID);
        return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int insert(String tagName) {
        String sql = "INSERT INTO tag_ids (name)"
            +" VALUES (?)";
        try (PreparedStatement stmt = this.database.createPreparedStatement(sql)) {
            stmt.setString(1, tagName);
            System.out.println(stmt);
        return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int delete(String itemId, int tagId) {
        String sql = "DELETE FROM tags"
            +" WHERE content_id = ? AND tag_id = ?";
        try (PreparedStatement stmt = this.database.createPreparedStatement(sql)) {
            stmt.setString(1, itemId);
            stmt.setInt(2, tagId);
        return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getId(String tagName) {
        String sql = "SELECT id"
            +" FROM tag_ids"
            +" WHERE name = ?";
        try (PreparedStatement stmt = this.database.createPreparedStatement(sql)) {
            stmt.setString(1, tagName);
            ResultSet res = stmt.executeQuery();
            if (res.next()) {
                return res.getInt("id");
            }
            else {
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
