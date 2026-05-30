package com.homedb;

public class Image {

    private Content imageContent;
    private byte[] data;

    public Image(Content imageContent, byte[] data) {
        this.imageContent = imageContent;
        this.data = data;
    }

    public Content getContent() {
        return this.imageContent;
    }

    public byte[] getData() {
        return this.data;
    }
}
