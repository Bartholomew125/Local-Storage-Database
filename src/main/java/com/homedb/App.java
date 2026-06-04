package com.homedb;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.homedb.content.Content;
import com.homedb.content.ContentInputReader;
import com.homedb.content.ContentWriter;
import com.homedb.content.ImageContent;
import com.homedb.content.VideoContent;
import com.homedb.database.Database;
import com.homedb.database.ImagesTable;
import com.homedb.database.VideosTable;

public class App {

    public static void main(String[] args) throws Exception {
        Stream<Content> content = ContentInputReader.getContent(Path.of("input"));
        Set<ImageContent> imageContent = new HashSet<>();
        Set<VideoContent> videoContent = new HashSet<>();
        content.forEach( c -> {
            if (ContentWriter.write(c)) {
                if (ImageContent.class.isInstance(c)) {
                    ImageContent image = (ImageContent) c;
                    synchronized(imageContent) {
                        imageContent.add(image);
                    }
                }
                else if (VideoContent.class.isInstance(c)) {
                    VideoContent video = (VideoContent) c;
                    synchronized(videoContent) {
                        videoContent.add(video);
                    }
                }
            }
        });
        System.out.println("Adding content to index");
        Database db = new Database();
        ImagesTable imagesTable = new ImagesTable(db);
        imagesTable.insert(imageContent);
        VideosTable videosTable = new VideosTable(db);
        videosTable.insert(videoContent);
    }

}
