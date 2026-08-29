package com.homedb.database;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.homedb.content.ImageContent;
import com.homedb.GeoLocation;
import com.homedb.MimeType;
import com.homedb.metadata.ContentMetaData;

public class ImagesTable extends AbstractTable<ImageContent> {

    public ImagesTable(Database database) {
        super(database, "images", List.of(
            "id", 
            "title", 
            "taken_at", 
            "path", 
            "width", 
            "height", 
            "mimetype", 
            "views", 
            "latitude", 
            "latitudeSpan", 
            "longitude", 
            "longitudeSpan", 
            "altitude",
            "deleted_at"
        ));
    }

    private static void addToStatement(PreparedStatement stmt, ImageContent item, int offset) throws SQLException {
        stmt.setString(offset+1, item.getId());
        stmt.setString(offset+2, item.getMetaData().title);
        stmt.setLong  (offset+3, item.getMetaData().takenAt);
        stmt.setString(offset+4, item.getPath().toString());
        stmt.setInt   (offset+5, item.getMetaData().width);
        stmt.setInt   (offset+6, item.getMetaData().height);
        stmt.setString(offset+7, item.getMetaData().mimeType.toString());
        stmt.setInt   (offset+8, item.getMetaData().views);
        if (item.getMetaData().geoData != null) {
            stmt.setFloat (offset+9, item.getMetaData().geoData.latitude());
            stmt.setFloat (offset+10, item.getMetaData().geoData.latitudeSpan());
            stmt.setFloat (offset+11, item.getMetaData().geoData.longitude());
            stmt.setFloat (offset+12, item.getMetaData().geoData.longitudeSpan());
            stmt.setFloat (offset+13, item.getMetaData().geoData.altitude());
        }
        else {
            stmt.setNull (offset+9, Types.FLOAT);
            stmt.setNull (offset+10, Types.FLOAT);
            stmt.setNull (offset+11, Types.FLOAT);
            stmt.setNull (offset+12, Types.FLOAT);
            stmt.setNull (offset+13, Types.FLOAT);
        }
        if (item.getDeletedAt() == null) {
            stmt.setNull(offset+14, Types.TIMESTAMP);
        }
        else {
            stmt.setLong(offset+14, item.getDeletedAt());
        }
    }

    @Override
    public int insert(ImageContent item) {
        try(PreparedStatement stmt = this.createPreparedStatement(this.INSERT_SQL)) {
            addToStatement(stmt, item, 0);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().toString().startsWith("[SQLITE_CONSTRAINT_PRIMARYKEY]")) {
                System.out.println("DUPLICATE KEY, SKIPPING.");
                return 0;
            }
            else {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
    }

    @Override
    public int insert(Set<ImageContent> items) {
        if (items.size() == 0) {
            return 0;
        }
        String INSERT_ALL_SQL = "INSERT INTO "+this.tableName+" VALUES ";
        List<String> placeholders = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) placeholders.add(this.INSERT_SQL_PLACEHOLDER);
        INSERT_ALL_SQL = INSERT_ALL_SQL.concat(String.join(", ", placeholders));

        try(PreparedStatement stmt = this.createPreparedStatement(INSERT_ALL_SQL)) {
            int i = 0;
            for (ImageContent item : items) {
                addToStatement(stmt, item, i);
                i = i + this.columns.size();
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().toString().startsWith("[SQLITE_CONSTRAINT_PRIMARYKEY]")) {
                System.out.println("DUPLICATE KEY, SKIPPING.");
                return 0;
            }
            else {
                System.out.println(INSERT_ALL_SQL);
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
    }

    @Override
    public ImageContent select(String itemID) {
        try(PreparedStatement stmt = this.createPreparedStatement(this.SELECT_SQL)) {
            stmt.setString(1, itemID);
            ResultSet res = stmt.executeQuery();
            if (res.next()) {
                ContentMetaData metaData = new ContentMetaData();
                metaData.title = res.getString("title");
                metaData.takenAt = res.getDate("taken_at").getTime();
                metaData.width = res.getInt("width");
                metaData.height = res.getInt("height");
                metaData.mimeType = MimeType.of(res.getString("mimetype"));
                metaData.views = res.getInt("views");
                metaData.geoData = new GeoLocation(
                    res.getFloat("latitude"),
                    res.getFloat("longitude"),
                    res.getFloat("altitude"),
                    res.getFloat("latitudeSpan"),
                    res.getFloat("longitudeSpan")
                );
                Long deletedAt = res.getLong("deleted_at");
                Path path = Path.of(res.getString("path"));
                ImageContent image = new ImageContent(itemID, path, metaData, deletedAt);
                return image;
            }
            else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<ImageContent> select(int limit, int offset, String sortBy) {
        List<ImageContent> images = new ArrayList<>();
        String sql = this.SELECT_ALL_SQL.formatted(sortBy);
        try(PreparedStatement stmt = this.createPreparedStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            ResultSet res = stmt.executeQuery();
            while (res.next()) {
                ContentMetaData metaData = new ContentMetaData();
                metaData.title = res.getString("title");
                metaData.takenAt = res.getDate("taken_at").getTime();
                metaData.width = res.getInt("width");
                metaData.height = res.getInt("height");
                metaData.mimeType = MimeType.of(res.getString("mimetype"));
                String id = res.getString("id");
                Path path = Path.of(res.getString("path"));
                ImageContent image = new ImageContent(id, path, metaData);
                images.add(image);
            }
            return images;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
