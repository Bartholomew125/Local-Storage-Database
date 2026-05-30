package com.homedb.database;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.homedb.ImageContent;
import com.homedb.MimeType;
import com.homedb.metadata.ContentMetaData;

public class ImagesTable extends AbstractTable<ImageContent> {

    public static final String COLUMN_ID         = "id";
    public static final String COLUMN_TITLE      = "title";
    public static final String COLUMN_TAKEN_AT   = "taken_at";
    public static final String COLUMN_IMAGE_DATA = "image_data";
    public static final String COLUMN_WIDTH      = "width";
    public static final String COLUMN_HEIGHT     = "height";
    public static final String COLUMN_THUMBNAIL  = "thumbnail";
    public static final String COLUMN_MIMETYPE   = "mimetype";
    public static final List<String> COLUMNS = List.of(
        COLUMN_ID, COLUMN_TITLE, COLUMN_TAKEN_AT, COLUMN_IMAGE_DATA,
        COLUMN_WIDTH, COLUMN_HEIGHT, COLUMN_THUMBNAIL, COLUMN_MIMETYPE);
    public static final int COLUMN_COUNT = COLUMNS.size();

    private static final String INSERT_SQL = 
        "INSERT INTO images (id, title, taken_at, image_data, width, height, thumbnail, mimetype) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

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
            stmt.setLong  (3, item.getMetaData().photoTakenTime);
            stmt.setBytes (4, item.getData());
            stmt.setInt   (5, item.getMetaData().width);
            stmt.setInt   (6, item.getMetaData().height);
            stmt.setBytes (7, item.getThumbnail());
            stmt.setString(8, item.getMetaData().mimeType.toString());
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
                metaData.photoTakenTime = res.getDate("taken_at").getTime();
                metaData.width = res.getInt("width");
                metaData.height = res.getInt("height");
                metaData.mimeType = MimeType.of(res.getString("mimetype"));
                byte[] data = res.getBytes("image_data");
                byte[] thumbnail = res.getBytes("thumbnail");
                ImageContent image = new ImageContent(itemID, data, thumbnail, metaData);
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
        String sql = SELECT_ALL_SQL.formatted(sortBy);
        try(PreparedStatement stmt = this.createPreparedStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            ResultSet res = stmt.executeQuery();
            while (res.next()) {
                ContentMetaData metaData = new ContentMetaData();
                metaData.title = res.getString("title");
                metaData.photoTakenTime = res.getDate("taken_at").getTime();
                metaData.width = res.getInt("width");
                metaData.height = res.getInt("height");
                metaData.mimeType = MimeType.of(res.getString("mimetype"));
                byte[] data = res.getBytes("image_data");
                byte[] thumbnail = res.getBytes("thumbnail");
                String id = res.getString("id");
                ImageContent image = new ImageContent(id, data, thumbnail, metaData);
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
