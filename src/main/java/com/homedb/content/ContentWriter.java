package com.homedb.content;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.homedb.Config;

public class ContentWriter {

    public static boolean write(Content content) {
        Path outputPath = Config.DATA_DIR.resolve(content.getId());
        try {
            Files.copy(content.readFile(), outputPath);
            return true;
        } catch (IOException e) {
            if (FileAlreadyExistsException.class.isInstance(e)) {
                System.out.println("FILE ALREADY EXISTS: "+content.getId());
                return false;
            }
            else {
                e.printStackTrace();
                System.exit(1);
                return false;
            }
        }
    }
}
