package com.homedb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.homedb.database.Database;
import com.homedb.database.ImagesTable;
import com.homedb.database.VideosTable;

public class App {

    public static void main(String[] args) throws Exception {
        Database db = new Database();
        ImagesTable imagesTable = new ImagesTable(db);
        VideosTable videosTable = new VideosTable(db);
        Stream<Content> content = ContentInputReader.getContent(Path.of("Google Fotos"));
        content.forEach( c -> {
            if (ImageContent.class.isInstance(c)) {
                ImageContent image = (ImageContent) c;
                imagesTable.insert(image);
            }
            else if (VideoContent.class.isInstance(c)) {
                VideoContent video = (VideoContent) c;
                videosTable.insert(video);
            }
        });
    }

}
