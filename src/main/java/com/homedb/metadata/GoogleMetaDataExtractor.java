package com.homedb.metadata;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;

import org.json.JSONException;
import org.json.JSONObject;

import com.homedb.GeoLocation;
import com.homedb.RegEx;


public class GoogleMetaDataExtractor extends AbstractMetaDataExtractor {

    public GoogleMetaDataExtractor(Path file) {
        super(file);
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
        metaData.imageViews = metaJson.getInt("imageViews");
        metaData.creationTime = metaJson.getJSONObject("creationTime").getLong("timestamp");
        metaData.photoTakenTime = metaJson.getJSONObject("photoTakenTime").getLong("timestamp");

        JSONObject geoDataJson = metaJson.getJSONObject("geoData");
        metaData.geoData = new GeoLocation(
                geoDataJson.getFloat("latitude"), 
                geoDataJson.getFloat("longitude"), 
                geoDataJson.getFloat("altitude"), 
                geoDataJson.getFloat("latitudeSpan"), 
                geoDataJson.getFloat("longitudeSpan")
        );

        // Try to get geoDataExif. Not all files have it.
        try {
            JSONObject geoDataExifJson = metaJson.getJSONObject("geoDataExif");
            metaData.geoDataExif = new GeoLocation(
                    geoDataExifJson.getFloat("latitude"), 
                    geoDataExifJson.getFloat("longitude"), 
                    geoDataExifJson.getFloat("altitude"), 
                    geoDataExifJson.getFloat("latitudeSpan"), 
                    geoDataExifJson.getFloat("longitudeSpan")
            );
        } catch (JSONException ignored) {}

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
