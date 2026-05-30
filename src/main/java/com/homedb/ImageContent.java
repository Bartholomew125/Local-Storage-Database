package com.homedb;

import java.nio.file.Path;

import com.homedb.metadata.ContentMetaData;

public class ImageContent extends AbstractContent {

    public ImageContent(Path path, ContentMetaData metaData) {
        super(path, metaData);
    }

    public ImageContent(String id, byte[] data, byte[] thumbnail, ContentMetaData metaData) {
        super(id, data, thumbnail, metaData);
    }
    
}
