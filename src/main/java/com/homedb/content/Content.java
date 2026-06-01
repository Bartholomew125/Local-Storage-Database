package com.homedb.content;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import com.homedb.metadata.ContentMetaData;

public interface Content {
    public String getId();
    public Path getPath();
    public ContentMetaData getMetaData();
    public InputStream readFile() throws IOException;
    public InputStream readThumbnailFile() throws IOException;
}
