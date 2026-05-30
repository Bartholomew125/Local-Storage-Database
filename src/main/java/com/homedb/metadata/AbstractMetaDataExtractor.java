package com.homedb.metadata;

import java.nio.file.Path;

public abstract class AbstractMetaDataExtractor implements MetaDataExtractor {

    private Path file;

    protected AbstractMetaDataExtractor(Path file) {
        this.file = file;
    }

    public Path getFile() {
        return this.file;
    }

    public void setFile(Path file) {
        this.file = file;
    }
}
