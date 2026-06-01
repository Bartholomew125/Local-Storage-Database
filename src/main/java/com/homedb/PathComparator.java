package com.homedb;

import java.nio.file.Path;
import java.util.Set;

public class PathComparator {

    private static final Set<String> JPG_EXTENSIONS = 
        Set.of(".jpg", ".jpeg", ".JPG", ".JPEG");
    private static final Set<String> PNG_EXTENSIONS = 
        Set.of(".png", ".PNG");
    private static final Set<String> MP4_EXTENSIONS = 
        Set.of(".mp4");

    public static boolean compareExtension(Path file, String extension) {
        return file.getFileName().toString().endsWith(extension);
    }

    public static boolean isJPG(Path file) {
        return JPG_EXTENSIONS.stream().anyMatch(ex -> compareExtension(file, ex));
    }

    public static boolean isPNG(Path file) {
        return PNG_EXTENSIONS.stream().anyMatch(ex -> compareExtension(file, ex));
    }

    public static boolean isMP4(Path file) {
        return MP4_EXTENSIONS.stream().anyMatch(ex -> compareExtension(file, ex));
    }

}
