package com.homedb.web;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.homedb.LimitedInputStream;
import com.homedb.MyDate;
import com.homedb.content.Content;
import com.homedb.content.ImageContent;
import com.homedb.content.VideoContent;
import com.homedb.database.ContentFetcher;
import com.homedb.database.Database;
import com.homedb.database.ImagesTable;
import com.homedb.database.Table;
import com.homedb.database.VideosTable;

import io.javalin.Javalin;

public class App {

    private static final int PORT = 8080;

    public static void main(String[] args) {

        Database database = new Database();
        Table<ImageContent> imagesTable = new ImagesTable(database);
        Table<VideoContent> videosTable = new VideosTable(database);
        ContentFetcher cf = new ContentFetcher(database);

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");  // matches resources/public
        }).start(PORT);

        // Your API routes sit alongside the static files
        app.get("/api/hello", ctx -> ctx.json(Map.of("message", "Hello!")));

        app.get("/api/gallery", ctx -> {
            int page   = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
            int limit  = 20;
            int offset = page * limit;

            List<Content> content = cf.fetch(limit, offset, "taken_at");

            ctx.json(content.stream()
                    .map(img -> Map.of(
                        "id",       img.getId(),
                        "title",    img.getMetaData().title,
                        "taken_at", new MyDate(img.getMetaData().takenAt, TimeUnit.SECONDS).toString(),
                        "width",    img.getMetaData().width,
                        "height",   img.getMetaData().height,
                        "duration", img.getMetaData().duration
            )).toList());
        });

        app.get("/api/images", ctx -> {
            int page   = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
            int limit  = 20;
            int offset = page * limit;

            List<ImageContent> images = imagesTable.select(limit, offset, "taken_at");

            ctx.json(images.stream()
                    .map(img -> Map.of(
                        "id",       img.getId(),
                        "title",    img.getMetaData().title,
                        "taken_at", new MyDate(img.getMetaData().takenAt, TimeUnit.SECONDS).toString(),
                        "width",    img.getMetaData().width,
                        "height",   img.getMetaData().height
            )).toList());
        });

        app.get("/api/videos", ctx -> {
            int page   = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
            int limit  = 20;
            int offset = page * limit;

            List<VideoContent> videos = videosTable.select(limit, offset, "taken_at");

            ctx.json(videos.stream()
                    .map(vid -> Map.of(
                        "id",       vid.getId(),
                        "title",    vid.getMetaData().title,
                        "taken_at", new MyDate(vid.getMetaData().takenAt, TimeUnit.SECONDS).toString(),
                        "width",    vid.getMetaData().width,
                        "height",   vid.getMetaData().height
            )).toList());
        });

        app.get("api/images/{id}", ctx -> {
            String imageid = ctx.pathParam("id");
            ImageContent image = imagesTable.select(imageid);
            if (image != null) {
                ctx.result(image.readFile());
            }
        });
        
        app.get("/api/videos/{id}", ctx -> {
            VideoContent video = videosTable.select(ctx.pathParam("id"));
            if (video == null) { ctx.status(404); return; }

            Path path = video.getPath();
            long fileSize = Files.size(path);

            ctx.header("Accept-Ranges", "bytes");
            ctx.contentType("video/mp4");

            String rangeHeader = ctx.header("Range");
            if (rangeHeader != null) {
                String[] parts = rangeHeader.replace("bytes=", "").split("-");
                long start = Long.parseLong(parts[0]);
                long end = parts.length > 1 && !parts[1].isEmpty()
                    ? Long.parseLong(parts[1])
                    : fileSize - 1;
                long length = end - start + 1;

                InputStream is = Files.newInputStream(path);
                is.skip(start);

                ctx.status(206)
                   .header("Content-Range", "bytes " + start + "-" + end + "/" + fileSize)
                   .header("Content-Length", String.valueOf(length))
                   .result(new LimitedInputStream(is, length));
            } else {
                ctx.header("Content-Length", String.valueOf(fileSize))
                   .result(Files.newInputStream(path));
            }
        });

        // app.get("api/videos/{id}", ctx -> {
        //     String videoid = ctx.pathParam("id");
        //     VideoContent video = videosTable.select(videoid);
        //     if (video != null) {
        //         ctx.result(video.readFile());
        //     }
        // });

        app.get("api/images/{id}/thumbnail", ctx -> {
            String imageid = ctx.pathParam("id");
            ImageContent image = imagesTable.select(imageid);
            if (image != null) {
                ctx.result(image.readThumbnailFile());
            }
        });

        app.get("api/videos/{id}/thumbnail", ctx -> {
            String videoid = ctx.pathParam("id");
            VideoContent video = videosTable.select(videoid);
            if (video != null) {
                ctx.result(video.readThumbnailFile());
            }
        });

        app.get("/json", ctx -> {
            ctx.json(Map.of("status", "ok", "message", "Hello"));
        });
    }
}
