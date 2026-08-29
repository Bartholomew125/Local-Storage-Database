package com.homedb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONObject;

public class Manifest implements Iterable<Manifest.Entry> { 

    private final List<Entry> entries;

    public Manifest(List<Entry> entries) {
        this.entries = entries;
    }

    public Manifest(Path file) {
        this.entries = readManifest(file);
    }

    public static class Entry {
        
        public final String username;
        public final String passwordHash;
        public final List<Path> paths;

        public Entry(String username, String passwordHash, List<Path> paths) {
            this.username = username;
            this.passwordHash = passwordHash;
            this.paths = paths;
        }

        public boolean contains(Path file) {
            return this.paths.stream()
                .anyMatch(path -> file.toString().contains(path.toString()));
        }

    }

    private static List<Entry> readManifest(Path file) {
        String string = null;
        try {
            string = Files.readString(file);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("FAILED TO READ MANIFEST");
            System.exit(1);
        }
        JSONObject json = new JSONObject(string);
        List<Manifest.Entry> manifest = new ArrayList<>();
        json.getJSONArray("manifest").forEach(ruleObject -> {
            JSONObject rule = (JSONObject) ruleObject;
            String username = (String) rule.get("username");
            String passwordHash;
            if (rule.get("password_hash") == JSONObject.NULL) {
                System.out.println("FAILED TO READ MANIFEST ENTRY WITH USERNAME "+username+". THE PASSWORD HASH WAS NULL");
                System.exit(1);
            }
            else {
                passwordHash = (String) rule.get("password_hash");
                List<Path> paths = ((JSONArray) rule.get("folders_access")).toList().stream()
                    .map(val -> Path.of(val.toString())).toList();
                manifest.add(new Manifest.Entry(username, passwordHash, paths));
            }
        });        
        return manifest;
    }

    @Override
    public Iterator<Entry> iterator() {
        return this.entries.iterator();
    }
}
