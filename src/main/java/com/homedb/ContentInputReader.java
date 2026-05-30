package com.homedb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.homedb.metadata.ContentMetaData;
import com.homedb.metadata.ExifMetaDataExtractor;
import com.homedb.metadata.GoogleMetaDataExtractor;
import com.homedb.metadata.MetaDataExtractorService;

public class ContentInputReader {

    public static Stream<Content> getContent(Path root) {
        Stream<Content> content = Stream.empty();
        Map<String, Integer> files = new ConcurrentHashMap<>();
        try {
            content = Files.walk(Paths.get("Google Fotos"))
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
                    service.addExtractor(new ExifMetaDataExtractor(file));
                    service.addExtractor(new GoogleMetaDataExtractor(file));
                    ContentMetaData metaData = service.extract();
                    if (metaData.photoTakenTime == 0) {
                    }
                    else {
                    }
                    if (metaData.mimeType.isVideo()) {
                        return new VideoContent(file, metaData);
                    }
                    else {
                        return new ImageContent(file, metaData);
                    }
                });
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;

    }
}
