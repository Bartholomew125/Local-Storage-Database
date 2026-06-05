package com.homedb;

import java.nio.file.Path;

public class Config {

    public static final Path DATA_DIR = Path.of("data");
    public static final String USERNAME = System.getenv("USERNAME");
    public static final String PASSWORD = System.getenv("PASSWORD");

}
