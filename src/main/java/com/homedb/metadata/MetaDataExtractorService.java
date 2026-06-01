package com.homedb.metadata;

import java.util.List;
import java.nio.file.Path;
import java.util.ArrayList;

public class MetaDataExtractorService extends AbstractMetaDataExtractor {

    private List<MetaDataExtractor> extractors;

    public MetaDataExtractorService(Path file) {
        this.extractors = new ArrayList<>();
        super(file);
    }

    public void addExtractor(MetaDataExtractor extractor) {
        this.extractors.add(extractor);
    }

    @Override
    public ContentMetaData extract() {
        ContentMetaDataBuilder builder = new ContentMetaDataBuilder();
        extractors.forEach( extractor -> {
            extractor.setFile(this.getFile());
            ContentMetaData subMetaData = extractor.extract();
            builder.append(subMetaData);
        });
        return builder.get();
    }
}
