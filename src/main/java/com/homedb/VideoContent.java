package com.homedb;

import java.nio.file.Path;

import com.homedb.metadata.ContentMetaData;

public class VideoContent extends AbstractContent {

    public VideoContent(Path path, ContentMetaData metaData) {
        super(path, metaData);
    }
    
}
