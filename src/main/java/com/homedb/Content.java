package com.homedb;

import java.nio.file.Path;

import com.homedb.metadata.ContentMetaData;

public interface Content {
    public String getId();
    public Path getPath();
    public ContentMetaData getMetaData();
    public byte[] getData();
    public byte[] getThumbnail();
}
