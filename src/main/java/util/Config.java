package util;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

public class Config {
    public static Properties properties;
    // Url
    public final static String URL_ROOT = "http://m.cgv.co.kr";
    public final static String LINE_URL = "https://notify-api.line.me/api/notify";
    // System
    public final static String SYSTEM_ROOT = System.getProperty("user.dir");

    public final static JTextPane panel = new JTextPane();

    public static boolean authInit(String resource) {
        Properties properties = new Properties();
        try {
            FileInputStream fis = new FileInputStream(resource);
            properties.load(new java.io.BufferedInputStream(fis));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        Enumeration propList = properties.elements();
        while (propList.hasMoreElements()) {
            if (propList.nextElement().toString().equalsIgnoreCase("")) {
                return false;
            }
        }
        Config.properties = properties;
        return true;
    }

    public static void authUpdate(String key, String value, String resource) {
        Properties properties = Config.properties;
        try {
            FileOutputStream fos = new FileOutputStream(resource);
            properties.setProperty(key, value);
            properties.store(fos, null);
        } catch (IOException e) {
            e.printStackTrace();

        }
        Config.properties = properties;
    }
}
