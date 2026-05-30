package com.homedb.metadata;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.homedb.GeoLocation;
import com.homedb.MimeType;

public class ContentMetaData {

    public String title;
    public String description;
    public int imageViews;
    public long creationTime;
    public long photoTakenTime;
    public GeoLocation geoData;
    public GeoLocation geoDataExif;
    public float length;
    public int width;
    public int height;
    public MimeType mimeType;

    public ContentMetaData() {
        this.title          = "";
        this.description    = "";
        this.imageViews     = 0;
        this.creationTime   = 0;
        this.photoTakenTime = 0;
        this.geoData        = null;
        this.geoDataExif    = null;
        this.width          = 0;
        this.height         = 0;
        this.mimeType       = null;
    }

    public boolean isValid() {
        return this.width > 0 
            && this.height > 0;
    }

    @Override
    public String toString() {
        return Stream.of(
                this.title,
                this.description,
                this.imageViews,
                this.creationTime,
                this.photoTakenTime,
                this.geoData,
                this.geoDataExif,
                this.length,
                this.width,
                this.height,
                this.mimeType
            )
            .map(String::valueOf)
            .map(c -> "(" + c + ")")
            .collect(Collectors.joining());
    }
}
