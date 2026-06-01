package com.homedb.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import com.homedb.content.VideoContent;

public class VideosTable extends AbstractTable<VideoContent> {

    public static final String COLUMN_ID         = "id";
    public static final String COLUMN_TITLE      = "title";
    public static final String COLUMN_TAKEN_AT   = "taken_at";
    public static final String COLUMN_DURATION   = "duration";
    public static final String COLUMN_VIDEO_DATA = "video_data";
    public static final String COLUMN_WIDTH      = "width";
    public static final String COLUMN_HEIGHT     = "height";
    public static final String COLUMN_THUMBNAIL  = "thumbnail";
    public static final String COLUMN_MIMETYPE   = "mimetype";
    public static final List<String> COLUMNS = List.of(
        COLUMN_ID, COLUMN_TITLE, COLUMN_TAKEN_AT, COLUMN_DURATION, 
        COLUMN_VIDEO_DATA, COLUMN_WIDTH, COLUMN_HEIGHT, 
        COLUMN_THUMBNAIL, COLUMN_MIMETYPE);
    public static final int COLUMN_COUNT = COLUMNS.size();

    private static final String INSERT_SQL = 
        "INSERT INTO videos (id, title, taken_at, duration, video_data, width, height, thumbnail, mimetype) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public VideosTable(Database database) {
        super(database, "videos");
    }

    @Override
    public int insert(VideoContent item) {
        try {
            PreparedStatement stmt = this.createPreparedStatement(INSERT_SQL);
            stmt.setString(1, item.getId());
            stmt.setString(2, item.getMetaData().title);
            stmt.setLong  (3, item.getMetaData().takenAt);
            stmt.setFloat (4, item.getMetaData().length);
            stmt.setInt   (6, item.getMetaData().width);
            stmt.setInt   (7, item.getMetaData().height);
            stmt.setString(9, item.getMetaData().mimeType.toString());
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
        return items.stream()
            .map(item -> this.insert(item))
            .reduce(0, (a,b) -> a+b);
    }

    @Override
    public VideoContent select(String itemID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<VideoContent> select(int limit, int offset, String sortBy) {
        // TODO Auto-generated method stub
        return null;
    }


    // @Override
    // public ResultSet select(List<Integer> columns, Set<Condition> conditions) {
    //     // TODO Auto-generated method stub
    //     return null;
    // }
}
