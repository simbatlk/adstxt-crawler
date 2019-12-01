package com.thunderlane.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigImpl implements Config {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigImpl.class);
    static final String CONFIGURATIONS = "config.properties";
    private final Properties properties = new Properties();
    private static Config configInstance = null;

    private ConfigImpl() {
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(CONFIGURATIONS);
            this.properties.load(inputStream);
        } catch (IOException exception) {
            LOG.error("Could not load configuration.", exception);
            System.exit(2);
        }
    }

    public static Config getInstance()
    {
        if (configInstance == null) {
            configInstance = new ConfigImpl();
        }

        return configInstance;
    }

    public final String getProperty(String propertyKey) {
        return properties.getProperty(propertyKey);
    }
}
