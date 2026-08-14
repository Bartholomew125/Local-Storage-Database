package com.homedb.web;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.homedb.LimitedInputStream;
import com.homedb.MyDate;
import com.homedb.content.Content;
import com.homedb.content.ImageContent;
import com.homedb.content.Tag;
import com.homedb.content.VideoContent;
import com.homedb.database.ContentFetcher;
import com.homedb.database.Database;
import com.homedb.database.ImagesTable;
import com.homedb.database.Table;
import com.homedb.database.TagsTable;
import com.homedb.database.VideosTable;

import io.javalin.Javalin;
import io.javalin.http.UnauthorizedResponse;

public class App {

    private static final int PORT = 8080;

    private static final Set<String> SESSIONS = ConcurrentHashMap.newKeySet();
    private static final String USERNAME = "andreas";
    private static final String PASSWORD_HASH = sha256("1234");

    private static String sha256(String input) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(input.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

        Database database = new Database();
        Table<ImageContent> imagesTable = new ImagesTable(database);
        Table<VideoContent> videosTable = new VideosTable(database);
        TagsTable tagsTable = new TagsTable(database);
        ContentFetcher cf = new ContentFetcher(database);

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");  // matches resources/public
        }).start(PORT);

        // Protect all pages and API routes
        // app.before(ctx -> {
        //     String path = ctx.path();
        //     if (path.equals("/login")          ||
        //         path.equals("/login.html")     ||
        //         path.equals("/api/login")      ||
        //         path.endsWith(".css")          ||
        //         path.endsWith(".js")           ||
        //         path.startsWith("/components")) return;
        //
        //     String token = ctx.cookie("session");
        //     if (token == null || !SESSIONS.contains(token)) {
        //         if (path.startsWith("/api")) {
        //             throw new UnauthorizedResponse();
        //         } else {
        //             ctx.redirect("/login.html");
        //         }
        //     }
        // });

        app.post("/api/login", ctx -> {
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");
            if (USERNAME.equals(username) && PASSWORD_HASH.equals(sha256(password))) {
                String token = UUID.randomUUID().toString();
                SESSIONS.add(token);
                ctx.cookie("session", token);
                ctx.status(200);
            } else {
                ctx.status(401).result("Invalid credentials");
            }
        });

        app.post("/api/logout", ctx -> {
            SESSIONS.remove(ctx.cookie("session"));
            ctx.removeCookie("session");
            ctx.redirect("/login.html");
        });

        app.get("/api/gallery", ctx -> {
            int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
            String sortBy = ctx.queryParamAsClass("sortBy", String.class).getOrDefault("taken_at");
            String ordering = ctx.queryParamAsClass("ordering", String.class).getOrDefault("DESC");
            int limit  = 20;
            int offset = page * limit;

            List<Content> content = cf.fetch(limit, offset, sortBy, ordering);

            ctx.json(content.stream()
                    .map(img -> Map.of(
                        "id",       img.getId(),
                        "title",    img.getMetaData().title,
                        "taken_at", new MyDate(img.getMetaData().takenAt, TimeUnit.SECONDS).toString(),
                        "width",    img.getMetaData().width,
                        "height",   img.getMetaData().height,
                        "duration", img.getMetaData().duration,
                        "type",     img.getMetaData().mimeType.isVideo() ? "video" : "image"
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

        app.patch("/api/images/{id}/rename", ctx -> {
            String id = ctx.pathParam("id");
            String title = ctx.bodyAsClass(Map.class).get("title").toString();
            imagesTable.rename(id, title);
            ctx.status(200);
        });

        app.patch("/api/videos/{id}/rename", ctx -> {
            String id = ctx.pathParam("id");
            String title = ctx.bodyAsClass(Map.class).get("title").toString();
            videosTable.rename(id, title);
            ctx.status(200);
        });

        app.get("/api/images/{id}/delete", ctx -> {
            String id = ctx.pathParam("id");
            imagesTable.delete(id);
        });

        app.get("/api/videos/{id}/delete", ctx -> {
            String id = ctx.pathParam("id");
            videosTable.delete(id);
        });

        app.get("/api/tags", ctx -> {
            String q = ctx.queryParamAsClass("q", String.class).getOrDefault("");
            List<Tag> tags = tagsTable.search(q);
            ctx.json(tags.stream()
                .map(tag -> Map.of(
                    "id", tag.id(),
                    "name", tag.name()
                )).toList()
            );
        });

        app.get("/api/tags/{id}", ctx -> {
            String id = ctx.pathParam("id");
            List<Tag> tags = tagsTable.select(id);
            ctx.json(tags.stream()
                .map(tag -> Map.of(
                    "id", tag.id(),
                    "name", tag.name()
                )).toList()
            );
        });

        app.post("/api/tags/{id}", ctx -> {
            String tagName = ctx.bodyAsClass(Map.class).get("tag").toString();
            String id = ctx.pathParam("id");
            int tag_id = tagsTable.getId(tagName);
            if (tag_id == -1) {
                tagsTable.insert(tagName);
            }
            tag_id = tagsTable.getId(tagName);
            if (tag_id == -1)
                throw new RuntimeException("NOT SUPPOSE TO HAPPEN");
            Tag tag = new Tag(tag_id, tagName);
            tagsTable.insert(tag, id);
            ctx.status(200);
        });

        app.delete("/api/tags/{itemId}/{tagId}", ctx -> {
            String itemId = ctx.pathParam("itemId");
            int tagId = Integer.valueOf(ctx.pathParam("tagId"));
            tagsTable.delete(itemId, tagId);
            ctx.status(200);
        });
    }
}
