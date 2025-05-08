package org.zfin.task.metricsmonitor;

import lombok.Data;

/**
 * Configuration class for the MetricsMonitor
 */
@Data
public class MetricsMonitorConfig {
    private static final String JMX_URL_FORMAT = "service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi";

    // JMX Connection Settings
    private String jmxHost;
    private long jmxPort;
    private long pollingIntervalSeconds;
    private String tomcatConnectorName;

    // Performance Thresholds
    private double msPerRequestThresholdHigh;
    private double msPerRequestThresholdLow;
    private double loadThresholdHigh;
    private double loadThresholdLow;
    private long millisToEnable;

    // Database Connection Settings
    private String dbHost;
    private String dbName;
    private String dbUser;

    // Singleton instance
    private static MetricsMonitorConfig instance;

    public static synchronized MetricsMonitorConfig getInstance() {
        if (instance == null) {
            instance = new MetricsMonitorConfig();
            instance.loadConfiguration();
        }
        return instance;
    }

    private void loadConfiguration() {
        // Set defaults
        setDefaults();

        // Override with environment variables
        loadFromEnvironment();
    }

    /**
     * Set default values for all configuration properties
     */
    private void setDefaults() {
        // JMX Connection Settings
        jmxHost = "localhost";
        jmxPort = 9012;
        pollingIntervalSeconds = 60;
        tomcatConnectorName = "https-openssl-nio-8443";

        // Performance Thresholds
        msPerRequestThresholdHigh = 1000.0;
        msPerRequestThresholdLow = 300.0;
        loadThresholdHigh = 19.0;
        loadThresholdLow = 15.0;
        millisToEnable = 1800 * 1000; // 30 minutes

        // Database Connection Settings
        dbHost = "db";
        dbName = "zfindb";
        dbUser = "postgres";
    }

    /**
     * Load configuration values from environment variables
     */
    private void loadFromEnvironment() {
        // JMX Connection Settings
        jmxHost = getEnv("JMX_HOST", jmxHost);
        jmxPort = getEnvAsLong("JMX_PORT", jmxPort);
        pollingIntervalSeconds = getEnvAsLong("POLLING_INTERVAL_SECONDS", pollingIntervalSeconds);
        tomcatConnectorName = getEnv("TOMCAT_CONNECTOR_NAME", tomcatConnectorName);

        // Performance Thresholds
        msPerRequestThresholdHigh = getEnvAsDouble("MS_PER_REQUEST_THRESHOLD_HIGH", msPerRequestThresholdHigh);
        msPerRequestThresholdLow = getEnvAsDouble("MS_PER_REQUEST_THRESHOLD_LOW", msPerRequestThresholdLow);
        loadThresholdHigh = getEnvAsDouble("LOAD_THRESHOLD_HIGH", loadThresholdHigh);
        loadThresholdLow = getEnvAsDouble("LOAD_THRESHOLD_LOW", loadThresholdLow);
        millisToEnable = getEnvAsLong("SECONDS_TO_ENABLE", millisToEnable / 1000) * 1000;

        // Database Connection Settings
        dbHost = getEnv("DBHOST", dbHost);
        dbName = getEnv("DBNAME", dbName);
        dbUser = getEnv("DBUSER", dbUser);
    }

    public String dbUrl() {
        // Construct DB URL
        return String.format("jdbc:postgresql://%s:5432/%s", dbHost, dbName);
    }

    public String jmxUrl() {
        return String.format(JMX_URL_FORMAT, jmxHost, jmxPort);
    }


    // Helper methods for environment variable loading
    private String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    private long getEnvAsLong(String name, long defaultValue) {
        String value = System.getenv(name);
        if (value != null && !value.isEmpty()) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
            }
        }
        return defaultValue;
    }

    private double getEnvAsDouble(String name, double defaultValue) {
        String value = System.getenv(name);
        if (value != null && !value.isEmpty()) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
            }
        }
        return defaultValue;
    }
}