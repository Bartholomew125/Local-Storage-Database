package com.homedb.content;

import java.nio.file.Path;

import com.homedb.metadata.ContentMetaData;

public class GeneralContent extends AbstractContent {

    public GeneralContent(String id, Path path, ContentMetaData metaData) {
        super(id, path, metaData);
    }
}
