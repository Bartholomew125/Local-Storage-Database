package com.homedb.metadata;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.homedb.GeoLocation;
import com.homedb.MimeType;

public class ContentMetaData {

    public String title;
    public String description;
    public long takenAt;
    public int width;
    public int height;
    public float length;
    public MimeType mimeType;
    public int views;
    public GeoLocation geoData;

    public ContentMetaData() {
        this.title          = "";
        this.description    = "";
        this.takenAt = 0;
        this.width          = 0;
        this.height         = 0;
        this.mimeType       = null;
        this.views     = 0;
        this.geoData        = null;
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
                this.takenAt,
                this.width,
                this.height,
                this.length,
                this.mimeType,
                this.views,
                this.geoData
            )
            .map(String::valueOf)
            .map(c -> "(" + c + ")")
            .collect(Collectors.joining());
    }
}
