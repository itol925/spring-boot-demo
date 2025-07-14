package org.itol.demo.http.client.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ResourceReader {
    public static String read(String filePath) {
        // 获取 ClassLoader
        ClassLoader classLoader = ResourceReader.class.getClassLoader();

        // 使用 try-with-resources 确保流关闭
        try (InputStream inputStream = classLoader.getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("文件未找到: " + filePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
