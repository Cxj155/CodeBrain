package com.codebrain.util;

import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class FileUtil {
    public static boolean isCodeFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".java") || name.endsWith(".py");
    }

    public static String calcSha256(Path path) throws IOException, NoSuchAlgorithmException {
        byte[] bytes = Files.readAllBytes(path);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
