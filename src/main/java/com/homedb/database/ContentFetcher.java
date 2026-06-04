package com.homedb.database;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.homedb.MimeType;
import com.homedb.content.Content;
import com.homedb.content.GeneralContent;
import com.homedb.metadata.ContentMetaData;

public class ContentFetcher {

    private final Database database;

    private static final String FETCH_ALL_SQL = "SELECT"
        +" id,title,taken_at,path,width,height,duration,mimetype,views,latitude,latitudeSpan,longitude,longitudeSpan,altitude"
        +" FROM videos UNION SELECT"
        +" id,title,taken_at,path,width,height,NULL,mimetype,views,latitude,latitudeSpan,longitude,longitudeSpan,altitude"
        +" FROM images"
        +" ORDER BY %s DESC NULLS LAST"
        +" LIMIT ? OFFSET ?";

    public ContentFetcher(Database database) {
        this.database = database;
    }

    public List<Content> fetch(int limit, int offset, String sortBy) {
        List<Content> content = new ArrayList<>();
        String sql = FETCH_ALL_SQL.formatted(sortBy);
        try(PreparedStatement stmt = this.database.createPreparedStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            ResultSet res = stmt.executeQuery();
            while (res.next()) {
                ContentMetaData metaData = new ContentMetaData();
                metaData.title = res.getString("title");
                metaData.takenAt = res.getDate("taken_at").getTime();
                metaData.width = res.getInt("width");
                metaData.height = res.getInt("height");
                metaData.duration = res.getFloat("duration");
                metaData.mimeType = MimeType.of(res.getString("mimetype"));
                String id = res.getString("id");
                Path path = Path.of(res.getString("path"));
                Content c = new GeneralContent(id, path, metaData);
                content.add(c);
            }
            return content;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

}

