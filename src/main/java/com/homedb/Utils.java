package com.homedb;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    public static String hashSHA1(String input) {
        MessageDigest alg = null;
        try {
            alg = MessageDigest.getInstance("SHA-3");
        } catch (NoSuchAlgorithmException ignored) {}

        byte[] hash = alg.digest(input.getBytes());
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) hex.append(String.format("%02x", b));
        return hex.toString();
    }
    
}

