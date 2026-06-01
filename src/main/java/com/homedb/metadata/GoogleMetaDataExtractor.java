package com.homedb.metadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.json.JSONObject;

import com.homedb.GeoLocation;


public class GoogleMetaDataExtractor extends AbstractMetaDataExtractor {

    public GoogleMetaDataExtractor(Path file) {
        super(file);
    }

    public GoogleMetaDataExtractor() {
        super();
    }

    @Override
    public ContentMetaData extract() {

        Path jsonPath = findMetaFile(this.getFile());
        if (jsonPath == null) {
            return null;
        }
        String json;
            
        try {
            json = Files.readString(jsonPath);
        } catch (IOException e) {
            return null;
        }

        ContentMetaData metaData = new ContentMetaData();
        
        JSONObject metaJson = new JSONObject(json);

        metaData.title = metaJson.getString("title");
        metaData.description = metaJson.getString("description");
        metaData.views = metaJson.getInt("imageViews");
        metaData.takenAt = metaJson.getJSONObject("photoTakenTime").getLong("timestamp");

        JSONObject geoDataJson = metaJson.getJSONObject("geoData");
        metaData.geoData = new GeoLocation(
                geoDataJson.getFloat("latitude"), 
                geoDataJson.getFloat("longitude"), 
                geoDataJson.getFloat("altitude"), 
                geoDataJson.getFloat("latitudeSpan"), 
                geoDataJson.getFloat("longitudeSpan")
        );
        return metaData;
    }

    public static Path findMetaFile(Path file) {
        String fileName = file.getFileName().toString();
        String baseName = fileName.contains(".") 
            ? fileName.substring(0, fileName.lastIndexOf('.')) 
            : fileName;

        try {
            return Files.list(file.getParent())
                .filter(f -> f.getFileName().toString().endsWith(".json"))
                .max(Comparator
                    .comparingDouble((Path json) -> score(fileName, baseName, json))
                    .thenComparingInt(json -> -Math.abs(jsonBase(json).length() - baseName.length()))
                )
                .filter(json -> score(fileName, baseName, json) >= 0.3)
                .orElse(null);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static double score(String imageFileName, String imageBase, Path json) {
        String jBase = jsonBase(json);
        // Compare full filenames (handles appended pattern: img.jpg.meta.json)
        double s1 = (double) commonPrefixLength(imageFileName, jBase) / imageFileName.length();
        // Compare bases without extensions (handles replaced extension: img.json)
        double s2 = (double) commonPrefixLength(imageBase, jBase) / imageBase.length();
        return Math.max(s1, s2);
    }

    private static String jsonBase(Path json) {
        String name = json.getFileName().toString();
        return name.substring(0, name.lastIndexOf('.'));
    }

    private static int commonPrefixLength(String a, String b) {
        int len = Math.min(a.length(), b.length());
        for (int i = 0; i < len; i++) {
            if (a.charAt(i) != b.charAt(i)) return i;
        }
        return len;
    }
}
