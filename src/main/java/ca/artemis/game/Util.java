package ca.artemis.game;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

public class Util {
    
	public static String[] RemoveEmptyStrings(String[] data) {
		List<String> result = new ArrayList<String>();
		
		for(int i = 0; i < data.length; i++)
			if(!data[i].equals(""))
				result.add(data[i]);
		
		String[] res = new String[result.size()];
		result.toArray(res);
		
		return res;
	}

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

    public static BufferedImage getBufferedImage(String path) {
        try(InputStream inputStream = ClassLoader.getSystemResourceAsStream(path)) {
            return ImageIO.read(inputStream);
        } catch(IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load BufferedImage");
        }
    }
}
