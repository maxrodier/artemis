package ca.artemis.engine.core.utils;


import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

public class FileUtils {
    
    public static List<String> readLines(String path) throws IOException {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(path)))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            return lines;
        }
    }

    public static String readString(String path) throws IOException {
        try(InputStream inputStream = ClassLoader.getSystemResourceAsStream(path)) {
            return new String(inputStream.readAllBytes());
        }
    }

    public static byte[] readBytes(String path) throws IOException {
        try(InputStream inputStream = ClassLoader.getSystemResourceAsStream(path)) {
            return inputStream.readAllBytes();
        }
    }

    public static BufferedImage getBufferedImage(String path) throws IOException {
        try(InputStream inputStream = ClassLoader.getSystemResourceAsStream(path)) {
            return ImageIO.read(inputStream);
        }
    }
}
