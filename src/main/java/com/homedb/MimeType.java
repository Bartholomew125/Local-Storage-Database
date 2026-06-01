package com.homedb;

public enum MimeType {
    PNG, JPG, WEBP, MP4;

    public static MimeType of(String name) throws IllegalArgumentException {
        switch (name) {
            case "image/png":
                return MimeType.PNG;
            case "image/jpg":
            case "image/jpeg":
                return MimeType.JPG;
            case "image/webp":
                return MimeType.WEBP;
            case "video/mp4":
                return MimeType.MP4;
            default:
                throw new IllegalArgumentException("Unkwnown mimetype: "+name);
        }
    }

    public boolean isVideo() {
        return this.equals(MP4);
    }

    @Override
    public String toString() {
        switch (this) {
            case PNG:
                return "image/png";
            case JPG:
                return "image/jpg";
            case WEBP:
                return "image/webp";
            case MP4:
                return "video/mp4";
        }
        return "UNKOWN MIME TYPE";
    }
}
