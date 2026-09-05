package com.homedb;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;

public class Utils {

    public static String hashSHA256(String input) {
        MessageDigest alg = null;
        try {
            alg = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ignored) {}

        byte[] hash = alg.digest(input.getBytes());
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) hex.append(String.format("%02x", b));
        return hex.toString();
    }
    
}

