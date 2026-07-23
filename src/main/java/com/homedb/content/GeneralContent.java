package com.homedb.content;

import java.nio.file.Path;

import com.homedb.metadata.ContentMetaData;

public class GeneralContent extends AbstractContent {

    public GeneralContent(
            String id, 
            Path path, 
            ContentMetaData metaData,
            Long deletedAt
    ) {
        super(id, path, metaData, deletedAt);
    }

    public GeneralContent(
            String id, 
            Path path, 
            ContentMetaData metaData
    ) {
        this(id, path, metaData, null);
    }
}
