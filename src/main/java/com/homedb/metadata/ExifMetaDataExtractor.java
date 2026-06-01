package com.homedb.metadata;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.homedb.MimeType;

public class ExifMetaDataExtractor extends AbstractMetaDataExtractor {

    public ExifMetaDataExtractor(Path file) {
        super(file);
    }

    public ExifMetaDataExtractor() {
        super();
    }

    private static <T> Optional<T> findFirstTag(Metadata metadata, String tagName, Class<T> type) {
        for (Directory dir : metadata.getDirectories()) {
            //System.out.println("Directory: "+dir.toString());
            for (Tag tag : dir.getTags()) {
                //System.out.println("\tTAG: "+tag.getTagName() + " = " + tag.getDescription());
                if (tag.getTagName().equals(tagName)) {
                    Object value = dir.getObject(tag.getTagType());
                    if (type.isInstance(value)) {
                        //System.out.println("YES IT IS INSTANCE");
                        return Optional.of(type.cast(value));
                    }
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public ContentMetaData extract() {

        Path file = this.getFile();
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(Files.newInputStream(file));

            int width = 0;
            int height = 0;
            float length = 0;

            MimeType mimeType = MimeType.of(
                findFirstTag(metadata, "Detected MIME Type", String.class)
                .orElseThrow()
            );

            switch (mimeType) {
                case JPG:
                case PNG:
                case WEBP:
                    width = findFirstTag(metadata, "Image Width", Integer.class).orElseThrow();
                    height = findFirstTag(metadata, "Image Height", Integer.class).orElseThrow();
                    break;
                case MP4:
                    width = findFirstTag(metadata, "Width", Integer.class).orElseThrow();
                    height = findFirstTag(metadata, "Height", Integer.class).orElseThrow();
                    length = (float) findFirstTag(metadata, "Duration", Long.class).orElseThrow();
                    float mediaTimeScale = (float) findFirstTag(metadata, "Media Time Scale", Long.class).orElseThrow();
                    length = length/mediaTimeScale;
                    break;
                default:
                    throw new RuntimeException("Unknown mime type: "+mimeType);
            }

            ContentMetaData contentMetaData = new ContentMetaData();
            contentMetaData.width = width;
            contentMetaData.height = height;
            contentMetaData.length = length;
            contentMetaData.mimeType = mimeType;
            return contentMetaData;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
