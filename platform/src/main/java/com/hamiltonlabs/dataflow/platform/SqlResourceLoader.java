package com.hamiltonlabs.dataflow.platform;
import java.util.Properties;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

public class SqlResourceLoader {
    private final Properties sqlProps = new Properties();

    public SqlResourceLoader(String platform) throws IOException {
        String fileName = platform.toLowerCase() + ".sql.properties";

        // 1. Check working directory
        File localFile = new File(fileName);
        if (localFile.exists()) {
            try (InputStream in = new FileInputStream(localFile)) {
                sqlProps.load(in);
                return;
            } 
        }

        // 2. Fallback to classpath resource
        String resourceName = fileName;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IllegalArgumentException("No SQL resource found for platform: " + platform);
            }
            sqlProps.load(in);
        }
    }

    public String getSql(String key) {
        String sql = sqlProps.getProperty(key);
        if (sql == null) {
            throw new IllegalArgumentException("No SQL found for key: " + key);
        }
        return sql;
    }
}

