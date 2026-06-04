package com.homedb.content;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.homedb.RegEx;
import com.homedb.metadata.ContentMetaData;
import com.homedb.metadata.ExifMetaDataExtractor;
import com.homedb.metadata.GoogleMetaDataExtractor;
import com.homedb.metadata.MetaDataExtractorService;

public class ContentInputReader {

    // private static Logger logger = Logger.getGlobal();

    public static Stream<Content> getContent(Path root) {
        Stream<Content> content = Stream.empty();
        Map<String, Integer> files = new ConcurrentHashMap<>();
        try {
            content = Files.walk(Paths.get("input"))
                .parallel()
                .filter( Files::isRegularFile )
                .filter( f -> f.toString().matches(RegEx.contentFileRegEx) )
                .map( file -> {
                    // Source - https://stackoverflow.com/a/3571239
                    // Posted by EboMike, modified by community. See post 'Timeline' for change history
                    // Retrieved 2026-05-24, License - CC BY-SA 3.0
                    // logger.log(Level.INFO, "LOGGING: "+Thread.currentThread().getName());
                    String extension = "";
                    int i = file.toString().lastIndexOf('.');
                    if (i > 0) {
                        extension = file.toString().substring(i+1);
                    }
                    files.merge(extension, 1, Integer::sum);
                    System.out.println(files);
                    // logger.log(Level.INFO, "DONE LOGGING: "+Thread.currentThread().getName());
                    
                    // logger.log(Level.INFO, "EXTRACTING: "+Thread.currentThread().getName());
                    MetaDataExtractorService service = new MetaDataExtractorService(file);
                    service.addExtractor(new ExifMetaDataExtractor());
                    service.addExtractor(new GoogleMetaDataExtractor());
                    ContentMetaData metaData = service.extract();
                    // logger.log(Level.INFO, "DONE EXTRACTING: "+Thread.currentThread().getName());

                    // logger.log(Level.INFO, "GENERATING ID: "+Thread.currentThread().getName());
                    String id = "";
                    try {
                        id = VideoContent.generateId(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                    // logger.log(Level.INFO, "DONE GENERATING ID: "+Thread.currentThread().getName());

                    // logger.log(Level.INFO, "CREATING CONTENT: "+Thread.currentThread().getName());
                    Content c;
                    if (metaData.mimeType.isVideo()) {
                        c = new VideoContent(id, file, metaData);
                    }
                    else {
                        c = new ImageContent(id, file, metaData);
                    }
                    // logger.log(Level.INFO, "DONE CREATING CONTENT: "+Thread.currentThread().getName());
                    return c;
                });
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
}
