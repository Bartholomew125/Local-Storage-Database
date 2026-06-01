package com.homedb.database;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.homedb.content.ImageContent;
import com.homedb.GeoLocation;
import com.homedb.MimeType;
import com.homedb.metadata.ContentMetaData;

public class ImagesTable extends AbstractTable<ImageContent> {

    private static final String INSERT_SQL = 
        "INSERT INTO images (id, title, taken_at, path, width, height, mimetype, views, latitude, latitudeSpan, longitude, longitudeSpan, altitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_SQL = 
        "SELECT * FROM images WHERE id=?";

    private static final String SELECT_ALL_SQL = 
        "SELECT * FROM images ORDER BY %s DESC NULLS LAST LIMIT ? OFFSET ?";

    public ImagesTable(Database database) {
        super(database, "images");
    }

    @Override
    public int insert(ImageContent item) {
        try(PreparedStatement stmt = this.createPreparedStatement(INSERT_SQL)) {
            stmt.setString(1, item.getId());
            stmt.setString(2, item.getMetaData().title);
            stmt.setLong  (3, item.getMetaData().takenAt);
            stmt.setString(4, item.getPath().toString());
            stmt.setInt   (5, item.getMetaData().width);
            stmt.setInt   (6, item.getMetaData().height);
            stmt.setString(7, item.getMetaData().mimeType.toString());
            stmt.setInt   (8, item.getMetaData().views);
            stmt.setFloat (9, item.getMetaData().geoData.latitude());
            stmt.setFloat (10, item.getMetaData().geoData.latitudeSpan());
            stmt.setFloat (11, item.getMetaData().geoData.longitude());
            stmt.setFloat (12, item.getMetaData().geoData.longitudeSpan());
            stmt.setFloat (13, item.getMetaData().geoData.altitude());
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
        return items.stream()
            .map(item -> this.insert(item))
            .reduce(0, (a,b) -> a+b);
    }

    @Override
    public ImageContent select(String itemID) {
        try(PreparedStatement stmt = this.createPreparedStatement(SELECT_SQL)) {
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
                Path path = Path.of(res.getString("path"));
                ImageContent image = new ImageContent(itemID, path, metaData);
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
    public Set<ImageContent> select(int limit, int offset, String sortBy) {
        Set<ImageContent> images = new HashSet<>();
        String sql = SELECT_ALL_SQL.formatted(sortBy);
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

    // @Override
    // public ResultSet select(List<String> columns, Set<Condition> conditions) {
    //     if (!ImagesTable.COLUMNS.containsAll(columns)) {
    //         throw new SQLException("Invalid columns");
    //     }
    //     // Create columns argument
    //     StringBuilder sbColumns = new StringBuilder();
    //     sbColumns.append("(");
    //     for (String col : columns) {
    //         sbColumns.append(col);
    //         sbColumns.append(",");
    //     }
    //     sbColumns.deleteCharAt(sbColumns.length()-1);
    //     sbColumns.append(")");
    //
    //     // Create conditions argument
    //     StringBuilder sbConditions = new StringBuilder();
    //     for (Condition cond : conditions) {
    //         sbConditions.append(cond.toString());
    //     }
    //
    //     try(PreparedStatement stmt = this.createPreparedStatement(SELECT_SQL)) {
    //         stmt.setString(1, sb.toString());
    //         stmt.setString(2, , x);
    //     } catch (Exception e) {
    //         // TODO: handle exception
    //     }
    // }
}
