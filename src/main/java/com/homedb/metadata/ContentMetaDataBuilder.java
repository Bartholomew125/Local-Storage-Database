package com.homedb.metadata;

public class ContentMetaDataBuilder {

    private ContentMetaData contentMetaData;

    public ContentMetaDataBuilder() {
        this.contentMetaData = new ContentMetaData();
    }

    public ContentMetaData get() {
        return this.contentMetaData;
    }

    public void append(ContentMetaData newContentMetaData) {
        if (newContentMetaData != null) {
            if (this.contentMetaData.title== "")
                this.contentMetaData.title = newContentMetaData.title;
            if (this.contentMetaData.description == "")
                this.contentMetaData.description = newContentMetaData.description;
            if (this.contentMetaData.views == 0) 
                this.contentMetaData.views = newContentMetaData.views;
            if (this.contentMetaData.takenAt == 0)
                this.contentMetaData.takenAt = newContentMetaData.takenAt;
            if (this.contentMetaData.geoData == null)
                this.contentMetaData.geoData = newContentMetaData.geoData;
            if (this.contentMetaData.width == 0)
                this.contentMetaData.width = newContentMetaData.width;
            if (this.contentMetaData.height == 0)
                this.contentMetaData.height = newContentMetaData.height;
            if (this.contentMetaData.duration == 0)
                this.contentMetaData.duration = newContentMetaData.duration;
            if (this.contentMetaData.mimeType == null)
                this.contentMetaData.mimeType = newContentMetaData.mimeType;
        }
    }
}
