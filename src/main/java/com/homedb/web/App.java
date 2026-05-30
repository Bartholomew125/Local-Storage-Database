package com.homedb.web;

import java.util.List;
import java.util.Map;

import com.homedb.ImageContent;
import com.homedb.database.Database;
import com.homedb.database.ImagesTable;
import com.homedb.database.Table;

import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {

        Database database = new Database();
        Table<ImageContent> imagesTable = new ImagesTable(database);

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");  // matches resources/public
        }).start(8080);

        // Your API routes sit alongside the static files
        app.get("/api/hello", ctx -> ctx.json(Map.of("message", "Hello!")));

        app.get("/api/images", ctx -> {
            int page   = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
            int limit  = 20;
            int offset = page * limit;

            List<ImageContent> images = imagesTable.select(limit, offset, "taken_at");

            ctx.json(images.stream()
                    .map(img -> Map.of(
                        "id",       img.getId(),
                        "title",    img.getMetaData().title,
                        "taken_at", img.getMetaData().photoTakenTime,
                        "width",    img.getMetaData().width,
                        "height",   img.getMetaData().height
            )).toList());
        });

        app.get("api/images/{id}", ctx -> {
            String imageid = ctx.pathParam("id");
            ImageContent image = imagesTable.select(imageid);
            if (image != null) {
                ctx.result(image.getData());
            }
        });

        app.get("api/images/{id}/thumbnail", ctx -> {
            String imageid = ctx.pathParam("id");
            ImageContent image = imagesTable.select(imageid);
            if (image != null) {
                ctx.result(image.getThumbnail());
            }
        });

        app.get("/json", ctx -> {
            ctx.json(Map.of("status", "ok", "message", "Hello"));
        });
    }
}
