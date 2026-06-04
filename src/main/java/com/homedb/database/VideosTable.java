package com.homedb.database;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.homedb.GeoLocation;
import com.homedb.MimeType;
import com.homedb.content.VideoContent;
import com.homedb.metadata.ContentMetaData;

public class VideosTable extends AbstractTable<VideoContent> {

    public VideosTable(Database database) {
        super(database, "videos", List.of(
            "id", 
            "title", 
            "taken_at", 
            "path", 
            "width", 
            "height", 
            "duration",
            "mimetype", 
            "views", 
            "latitude", 
            "latitudeSpan", 
            "longitude", 
            "longitudeSpan", 
            "altitude"
        ));
    }

    @Override
    public int insert(VideoContent item) {
        try(PreparedStatement stmt = this.createPreparedStatement(this.INSERT_SQL)) {
            stmt.setString(1, item.getId());
            stmt.setString(2, item.getMetaData().title);
            stmt.setLong  (3, item.getMetaData().takenAt);
            stmt.setString(4, item.getPath().toString());
            stmt.setInt   (5, item.getMetaData().width);
            stmt.setInt   (6, item.getMetaData().height);
            stmt.setFloat (7, item.getMetaData().duration);
            stmt.setString(8, item.getMetaData().mimeType.toString());
            stmt.setInt   (9, item.getMetaData().views);
            stmt.setFloat (10, item.getMetaData().geoData.latitude());
            stmt.setFloat (11, item.getMetaData().geoData.latitudeSpan());
            stmt.setFloat (12, item.getMetaData().geoData.longitude());
            stmt.setFloat (13, item.getMetaData().geoData.longitudeSpan());
            stmt.setFloat (14, item.getMetaData().geoData.altitude());
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
    public int insert(Set<VideoContent> items) {
        String INSERT_ALL_SQL = "INSERT INTO "+this.tableName+" VALUES ";
        List<String> placeholders = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) placeholders.add(this.INSERT_SQL_PLACEHOLDER);
        INSERT_ALL_SQL = INSERT_ALL_SQL.concat(String.join(", ", placeholders));

        try(PreparedStatement stmt = this.createPreparedStatement(INSERT_ALL_SQL)) {
            int i = 0;
            for (VideoContent item : items) {
                stmt.setString(i+1, item.getId());
                stmt.setString(i+2, item.getMetaData().title);
                stmt.setLong  (i+3, item.getMetaData().takenAt);
                stmt.setString(i+4, item.getPath().toString());
                stmt.setInt   (i+5, item.getMetaData().width);
                stmt.setInt   (i+6, item.getMetaData().height);
                stmt.setFloat (i+7, item.getMetaData().duration);
                stmt.setString(i+8, item.getMetaData().mimeType.toString());
                stmt.setInt   (i+9, item.getMetaData().views);
                stmt.setFloat (i+10, item.getMetaData().geoData.latitude());
                stmt.setFloat (i+11, item.getMetaData().geoData.latitudeSpan());
                stmt.setFloat (i+12, item.getMetaData().geoData.longitude());
                stmt.setFloat (i+13, item.getMetaData().geoData.longitudeSpan());
                stmt.setFloat (i+14, item.getMetaData().geoData.altitude());
                i = i + this.columns.size();
            }
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
    public VideoContent select(String itemID) {
        try(PreparedStatement stmt = this.createPreparedStatement(this.SELECT_SQL)) {
            stmt.setString(1, itemID);
            ResultSet res = stmt.executeQuery();
            if (res.next()) {
                ContentMetaData metaData = new ContentMetaData();
                metaData.title = res.getString("title");
                metaData.takenAt = res.getDate("taken_at").getTime();
                metaData.width = res.getInt("width");
                metaData.height = res.getInt("height");
                metaData.duration = res.getFloat("duration");
                metaData.mimeType = MimeType.of(res.getString("mimetype"));
                metaData.views = res.getInt("views");
                metaData.geoData = new GeoLocation(
                    res.getFloat("latitude"),
                    res.getFloat("longitude"),
                    res.getFloat("altitude"),
                    res.getFloat("latitudeSpan"),
                    res.getFloat("longitudeSpan")
                );
                Path path = Path.of(res.getString("path"));
                VideoContent video = new VideoContent(itemID, path, metaData);
                return video;
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
    public List<VideoContent> select(int limit, int offset, String sortBy) {
        List<VideoContent> videos = new ArrayList<>();
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
                metaData.duration = res.getInt("duration");
                metaData.mimeType = MimeType.of(res.getString("mimetype"));
                String id = res.getString("id");
                Path path = Path.of(res.getString("path"));
                VideoContent video = new VideoContent(id, path, metaData);
                videos.add(video);
            }
            return videos;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }
}
