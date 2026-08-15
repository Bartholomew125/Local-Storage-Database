package com.homedb;

import java.nio.file.Path;

public class Config {

    public static final Path DATA_DIR = Path.of("data");
    public static final String USERNAME = System.getenv("USERNAME");
    public static final String PASSWORD = System.getenv("PASSWORD");
    public static final Path RESOURCES_DIR = Path.of("src/main/resources");
    public static final Path FETCH_ALL_PATH = RESOURCES_DIR.resolve("sql/fetch_all.sql");

}
