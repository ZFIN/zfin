package org.zfin.task.metricsmonitor;

import java.sql.*;
import java.util.Date;

public class HighLoadAction {

    public static final String TRIGGER_ON_SQL = "update zdb_feature_flag set zfeatflag_enabled=true, zfeatflag_last_modified=now() where zfeatflag_name in ('Enable Captcha', 'Use altcha')";
    public static final String TRIGGER_OFF_SQL = "update zdb_feature_flag set zfeatflag_enabled=false";
    private final MetricsMonitorConfig configuration;

    public boolean isTriggered = false;
    public Date lastTriggered = new Date();

    public HighLoadAction() {
        this.configuration = MetricsMonitorConfig.getInstance();
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
                ((metrics.getLoad1min() > configuration.getLoadThresholdHigh()) ||
                 (metrics.getIntervalAverage() > configuration.getMsPerRequestThresholdHigh()));
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
                ((metrics.getLoad1min() < configuration.getLoadThresholdLow()) ||
                 (metrics.getIntervalAverage() < configuration.getMsPerRequestThresholdLow())) &&
                (timeSinceEnable > configuration.getMillisToEnable());
    }

    private void turnOff() {
        isTriggered = false;
        int rowsAffected = executeUpdate(TRIGGER_OFF_SQL);
        System.out.println("Captcha disabled: " + rowsAffected + " row(s) updated");
    }

    private int executeUpdate(String sql) {
        // Establish connection
        String url = "jdbc:postgresql://" + configuration.getDbHost() + ":5432/" + configuration.getDbName();
        String user = configuration.getDbUser();
        String password = "";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            return statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
