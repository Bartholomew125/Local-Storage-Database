package com.homedb.content;

import java.nio.file.Path;

import com.homedb.metadata.ContentMetaData;

public class VideoContent extends AbstractContent {

    public VideoContent(String id, Path path, ContentMetaData metaData) {
        super(id, path, metaData);
    }
    
}
