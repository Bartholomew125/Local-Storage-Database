package com.homedb.content;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import com.homedb.Config;
import com.homedb.PathComparator;
import com.homedb.metadata.ContentMetaData;

import net.coobird.thumbnailator.Thumbnails;

public abstract class AbstractContent implements Content {

    private String id;
    private Path path;
    private Path thumbnailPath;
    private ContentMetaData metaData;

    public AbstractContent(String id, Path path, ContentMetaData metaData) {
        if (!metaData.isValid()) {
            throw new RuntimeException("Metadata is not valid");
        }
        this.metaData = metaData;
        this.path = path;
        this.id = id;
        this.thumbnailPath = Path.of(path.toString() + ".thumbnail");
        int thumbnailWidth =  (int) (metaData.width  * 0.4);
        int thumbnailHeight = (int) (metaData.height * 0.4);
        try {
            this.thumbnailPath = AbstractContent.generateThumbnail(id, path, thumbnailWidth, thumbnailHeight);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
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
    public InputStream readFile() throws IOException {
        return Files.newInputStream(this.path);
    }

    @Override
    public InputStream readThumbnailFile() throws IOException {
        return Files.newInputStream(this.thumbnailPath);
    }

    @Override
    public String toString() {
        return this.getPath() + " - " + this.getMetaData().toString();
    }

    // public static String generateId(Path path) throws IOException, NoSuchAlgorithmException {
    //     byte[] hash = MessageDigest
    //         .getInstance("SHA-1")
    //         .digest(Files.readAllBytes(path));
    //     StringBuilder hex = new StringBuilder();
    //     for (byte b : hash) hex.append(String.format("%02x", b));
    //     return hex.toString();
    // }
    public static String generateId(Path file) throws IOException, NoSuchAlgorithmException {
        BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
        String input = file.toAbsolutePath().toString() + attrs.size() + attrs.lastModifiedTime();
        byte[] hash = MessageDigest
            .getInstance("SHA-1")
            .digest(input.getBytes());
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) hex.append(String.format("%02x", b));
        return hex.toString();
    }

    // public static Path generateThumbnail(String id, Path source, int width, int height) throws IOException {
    //     Path thumbnail = Config.DATA_DIR.resolve(id + ".thumbnail");
    //     if (Files.exists(thumbnail)) return thumbnail;
    //
    //     List<String> cmd = new ArrayList<>(List.of(
    //         "ffmpeg", "-i", source.toString()
    //     ));
    //     if (PathComparator.isMP4(source)) {
    //         cmd.addAll(List.of("-ss", "00:00:01", "-vframes", "1"));
    //     }
    //     cmd.addAll(List.of(
    //         "-vf",    "scale=" + width + ":-1,format=yuvj420p",
    //         "-q:v",   "2",
    //         "-f",     "mjpeg",
    //         thumbnail.toString()
    //     ));
    //
    //     ProcessBuilder pb = new ProcessBuilder(cmd);
    //     pb.redirectErrorStream(true);
    //     Process process = pb.start();
    //     String output = new String(process.getInputStream().readAllBytes());
    //
    //     try {
    //         int[] exitCode = {0};
    //         ForkJoinPool.managedBlock(new ForkJoinPool.ManagedBlocker() {
    //             boolean done = false;
    //             public boolean block() throws InterruptedException {
    //                 exitCode[0] = process.waitFor();
    //                 done = true;
    //                 return true;
    //             }
    //             public boolean isReleasable() { return done; }
    //         });
    //
    //         if (exitCode[0] != 0) throw new IOException("FFmpeg failed (exit " + exitCode[0] + ") for: " + source + "\n" + output);
    //     } catch (InterruptedException e) {
    //         throw new IOException("FFmpeg interrupted", e);
    //     }
    //
    //     return thumbnail;
    // }

    public static Path generateThumbnail(String id, Path source, int width, int height) throws IOException {
        Path thumbnail = Config.DATA_DIR.resolve(id + ".thumbnail");
        if (Files.exists(thumbnail)) return thumbnail;

        List<String> cmd = new ArrayList<>(List.of(
            "ffmpeg", "-i", source.toString()
        ));
        if (PathComparator.isMP4(source)) {
            cmd.addAll(List.of("-ss", "00:00:01", "-vframes", "1"));
        }
        cmd.addAll(List.of(
            "-vf",    "scale=" + width + ":-1,format=yuvj420p",
            "-q:v",   "2",
            "-f",     "mjpeg",
            thumbnail.toString()
        ));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = new String(process.getInputStream().readAllBytes());
        try {
            int exit = process.waitFor();
            if (exit != 0) throw new IOException("FFmpeg failed (exit " + exit + ") for: " + source + "\n" + output);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("FFmpeg interrupted", e);
        }

        return thumbnail;
    }
}
