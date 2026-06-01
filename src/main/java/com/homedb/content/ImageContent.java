package com.homedb.content;

import java.nio.file.Path;

import com.homedb.metadata.ContentMetaData;

public class ImageContent extends AbstractContent {

    public ImageContent(String id, Path path, ContentMetaData metaData) {
        super(id, path, metaData);
    }
}
