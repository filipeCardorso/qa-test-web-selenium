package com.qatest.web.config;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;

public final class ConfigManager {

    private static ConfigManager instance;
    private final Map<String, Object> config;

    @SuppressWarnings("unchecked")
    private ConfigManager() {
        Yaml yaml = new Yaml();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.yml")) {
            Map<String, Object> root = yaml.load(input);
            this.config = (Map<String, Object>) root.get("web");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load application.yml", e);
        }
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public String getBaseUrl() {
        return (String) config.get("base-url");
    }

    public String getBrowser() {
        return (String) config.get("browser");
    }

    public boolean isHeadless() {
        String ci = System.getProperty("CI", "false");
        if ("true".equalsIgnoreCase(ci)) {
            return true;
        }
        return (boolean) config.get("headless");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getTimeoutConfig() {
        return (Map<String, Object>) config.get("timeout");
    }

    public int getImplicitTimeout() {
        return (int) getTimeoutConfig().get("implicit");
    }

    public int getExplicitTimeout() {
        return (int) getTimeoutConfig().get("explicit");
    }

    public int getPageLoadTimeout() {
        return (int) getTimeoutConfig().get("page-load");
    }
}
