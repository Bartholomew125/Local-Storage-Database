package com.homedb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.homedb.metadata.ContentMetaData;

import net.coobird.thumbnailator.Thumbnails;

public abstract class AbstractContent implements Content {

    private String id;
    private Path path;
    private ContentMetaData metaData;
    private byte[] data;
    private byte[] thumbnail;

    public AbstractContent(Path path, ContentMetaData metaData) {
        this.path = path;
        byte[] data;
        try {
            data = Files.readAllBytes(path);
        } catch (IOException ignored) {
            throw new RuntimeException("Unable to read file, and generate id.");
        }
        int thumbnailWidth =  (int) (metaData.width  * 0.4);
        int thumbnailHeight = (int) (metaData.height * 0.4);
        byte[] thumbnail = generateThumbnail(path, thumbnailWidth, thumbnailHeight);
        String id = AbstractContent.generateId(data);
        this(id, data, thumbnail, metaData);
    }

    public AbstractContent(String id, byte[] data, byte[] thumbnail, ContentMetaData metaData) {
        if (!metaData.isValid()) {
            throw new RuntimeException("Metadata is not valid");
        }
        this.data = data;
        this.thumbnail = thumbnail;
        this.metaData = metaData;
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Path getPath() {
        return this.path;
    }

    @Override
    public ContentMetaData getMetaData() {
        return metaData;
    }

    @Override
    public byte[] getData() {
        return this.data;
    }

    @Override
    public byte[] getThumbnail() {
        return this.thumbnail;
    }

    @Override
    public String toString() {
        return this.getPath() + " - " + this.getMetaData().toString();
    }

    private static String generateId(byte[] data) {
        if (data == null) {
            System.out.println("ERROR CANT GENERATE ID FROM NULL");
            throw new RuntimeException();
        }
        byte[] hashBytes = {};
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            hashBytes = md.digest(data);
        } catch (NoSuchAlgorithmException ignored) {}

        StringBuilder hex = new StringBuilder();
        for (byte b : hashBytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    private static byte[] generateThumbnail(Path path, int width, int height) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Thumbnails.of(path.toString())
                .size(width, height)
                .toOutputStream(out);
        } catch (IOException ignored) {}
        return out.toByteArray();
    }

    
}
