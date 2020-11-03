package io.mykidong.util;

import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtils {

    public static Properties readPropertiesFromClasspath(String path){
        Properties properties = new Properties();
        try {
            File file = ResourceUtils.getFile("classpath:" + path);
            InputStream in = new FileInputStream(file);
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static Properties readPropertiesFromFileSystem(String path){
        Properties properties = new Properties();
        try {
            File file = ResourceUtils.getFile( path);
            InputStream in = new FileInputStream(file);
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

}
