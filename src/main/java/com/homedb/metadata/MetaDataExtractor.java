package com.homedb.metadata;

import java.nio.file.Path;

public interface MetaDataExtractor {
    public ContentMetaData extract();
    public Path getFile();
    public void setFile(Path file);
}
