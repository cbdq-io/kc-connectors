package io.cbdq;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VersionUtil {

    private static String version;

    static {
        try (InputStream input = VersionUtil.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (input == null) {
                throw new IOException("Version properties file not found");
            }
            Properties prop = new Properties();
            prop.load(input);
            version = prop.getProperty("version", "unknown");
        } catch (IOException ex) {
            version = "unknown";
            ex.printStackTrace();
        }
    }

    public static String getVersion() {
        return version;
    }
}
