package org.zfin.task.metricsmonitor;

import java.sql.*;
import java.util.Date;

public class HighLoadAction {

    public final double MS_PER_REQUEST_THRESHOLD_HIGH;
    public final double MS_PER_REQUEST_THRESHOLD_LOW;
    public final double LOAD_THRESHOLD_HIGH;
    public final double LOAD_THRESHOLD_LOW;
    public final long MS_TO_ENABLE;
    public final String DBHOST;
    public final String DBUSER;
    public final String DBNAME;
    public static final String TRIGGER_ON_SQL = "update zdb_feature_flag set zfeatflag_enabled=true, zfeatflag_last_modified=now() where zfeatflag_name in ('Enable Captcha', 'Use altcha')";
    public static final String TRIGGER_OFF_SQL = "update zdb_feature_flag set zfeatflag_enabled=false";

    public boolean isTriggered = false;
    public Date lastTriggered = new Date();

    public HighLoadAction() {
        if (System.getenv("MS_PER_REQUEST_THRESHOLD_HIGH") != null) {
            MS_PER_REQUEST_THRESHOLD_HIGH = Double.parseDouble(System.getenv("MS_PER_REQUEST_THRESHOLD_HIGH"));
        } else {
            MS_PER_REQUEST_THRESHOLD_HIGH = 1000.0;
        }

        if (System.getenv("MS_PER_REQUEST_THRESHOLD_LOW") != null) {
            MS_PER_REQUEST_THRESHOLD_LOW = Double.parseDouble(System.getenv("MS_PER_REQUEST_THRESHOLD_LOW"));
        } else {
            MS_PER_REQUEST_THRESHOLD_LOW = 300.0;
        }

        if (System.getenv("LOAD_THRESHOLD_HIGH") != null) {
            LOAD_THRESHOLD_HIGH = Double.parseDouble(System.getenv("LOAD_THRESHOLD_HIGH"));
        } else {
            LOAD_THRESHOLD_HIGH = 19.0;
        }

        if (System.getenv("LOAD_THRESHOLD_LOW") != null) {
            LOAD_THRESHOLD_LOW = Double.parseDouble(System.getenv("LOAD_THRESHOLD_LOW"));
        } else {
            LOAD_THRESHOLD_LOW = 15.0;
        }

        if (System.getenv("SECONDS_TO_ENABLE") != null) {
            MS_TO_ENABLE = Long.parseLong(System.getenv("SECONDS_TO_ENABLE"));
        } else {
            MS_TO_ENABLE = 1800 * 1000;
        }

        if (System.getenv("DBHOST") != null) {
            DBHOST = System.getenv("DBHOST");
        } else {
            DBHOST = "db";
        }

        if (System.getenv("DBUSER") != null) {
            DBUSER = System.getenv("DBUSER");
        } else {
            DBUSER = "postgres";
        }

        if (System.getenv("DBNAME") != null) {
            DBNAME = System.getenv("DBNAME");
        } else {
            DBNAME = "zfindb";
        }
    }

    public void trigger(SystemMetricsDTO metrics) {
        if (shouldTurnOn(metrics)) {
            turnOn();
        }
        if (shouldTurnOff(metrics)) {
            turnOff();
        }
    }

    private boolean shouldTurnOn(SystemMetricsDTO metrics) {
        return !isTriggered &&
                ((metrics.getLoad1min() > LOAD_THRESHOLD_HIGH) ||
                 (metrics.getIntervalAverage() > MS_PER_REQUEST_THRESHOLD_HIGH));
    }

    private void turnOn() {
        isTriggered = true;
        lastTriggered = new Date();
        int rowsAffected = executeUpdate(TRIGGER_ON_SQL);
        System.out.println("Captcha enabled: " + rowsAffected + " row(s) updated");
    }

    private boolean shouldTurnOff(SystemMetricsDTO metrics) {
        long timeSinceEnable = System.currentTimeMillis() - lastTriggered.getTime();
        return isTriggered &&
                ((metrics.getLoad1min() < LOAD_THRESHOLD_LOW) ||
                 (metrics.getIntervalAverage() < MS_PER_REQUEST_THRESHOLD_LOW)) &&
                (timeSinceEnable > MS_TO_ENABLE);
    }

    private void turnOff() {
        isTriggered = false;
        int rowsAffected = executeUpdate(TRIGGER_OFF_SQL);
        System.out.println("Captcha disabled: " + rowsAffected + " row(s) updated");
    }

    private int executeUpdate(String sql) {
        // Establish connection
        String url = "jdbc:postgresql://" + DBHOST + ":5432/" + DBNAME;
        String user = DBUSER;
        String password = "";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            return statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
