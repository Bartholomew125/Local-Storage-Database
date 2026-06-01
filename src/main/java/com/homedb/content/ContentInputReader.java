package com.homedb.content;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.homedb.RegEx;
import com.homedb.metadata.ContentMetaData;
import com.homedb.metadata.ExifMetaDataExtractor;
import com.homedb.metadata.GoogleMetaDataExtractor;
import com.homedb.metadata.MetaDataExtractorService;

public class ContentInputReader {

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
                    String extension = "";
                    int i = file.toString().lastIndexOf('.');
                    if (i > 0) {
                        extension = file.toString().substring(i+1);
                    }
                    files.merge(extension, 1, Integer::sum);
                    System.out.println(files);

                    MetaDataExtractorService service = new MetaDataExtractorService(file);
                    service.addExtractor(new ExifMetaDataExtractor());
                    service.addExtractor(new GoogleMetaDataExtractor());
                    ContentMetaData metaData = service.extract();

                    String id = "";
                    try {
                        id = VideoContent.generateId(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(1);
                    }

                    if (metaData.mimeType.isVideo()) {
                        return new VideoContent(id, file, metaData);
                    }
                    else {
                        return new ImageContent(id, file, metaData);
                    }
                });
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
}
