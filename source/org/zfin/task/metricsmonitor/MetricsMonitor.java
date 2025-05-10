package org.zfin.task.metricsmonitor;

import lombok.extern.log4j.Log4j2;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Uses jmx to connect to tomcat for various tomcat metrics and
 * Reads /proc/loadavg to get the cpu load averages.
 * Once the "1 minute load average" exceeds the LOAD_THRESHOLD_HIGH or
 * the "average number of milliseconds processing time per request" from tomcat exceeds MS_PER_REQUEST_THRESHOLD_HIGH,
 * it will turn on captcha.
 *
 * If the captcha is enabled, it will disable when the relevant metrics drop below the corresponding "_LOW" thresholds
 * and a certain amount of time has passed (SECONDS_TO_ENABLE)
 */
@Log4j2
public class MetricsMonitor {
    private static final String TOMCAT_CONNECTOR_NAME = "https-openssl-nio-8443";

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private HighLoadAction triggerAction = new HighLoadAction();
    private JMXConnector jmxConnector;
    private MBeanServerConnection mbeanConn;
    private SystemMetricsDTO previousSystemMetrics;
    private long rowNumber = 0;
    private MetricsMonitorConfig configuration;

    private void run() throws IOException {
        this.init();
        this.connect();
        this.startMonitoring();
    }

    private void init() {
        this.configuration = MetricsMonitorConfig.getInstance();
    }

    public void connect() throws IOException {
        log.info("Connecting to " + configuration.jmxUrl());
        JMXServiceURL url = new JMXServiceURL(configuration.jmxUrl());
        jmxConnector = JMXConnectorFactory.connect(url);
        mbeanConn = jmxConnector.getMBeanServerConnection();
    }

    public void startMonitoring() {
        while (true) {
            try {
                SystemMetricsDTO metrics = collectMetrics();
                printMetrics(metrics);
                triggerActionForMetrics(metrics);
                Thread.sleep(this.configuration.getPollingIntervalSeconds() * 1000);
            } catch (Exception e) {
                System.err.println("Error collecting metrics: " + e.getMessage());
                e.printStackTrace();

                // Try to reconnect
                try {
                    System.out.println("Attempting to reconnect...");
                    disconnect();
                    connect();
                } catch (IOException reconnectEx) {
                    System.err.println("Failed to reconnect: " + reconnectEx.getMessage());
                }
            }
        }
    }

    private void triggerActionForMetrics(SystemMetricsDTO metrics) {
        triggerAction.trigger(metrics);
    }

    private SystemMetricsDTO collectMetrics() {
        SystemMetricsDTO metricsRow = new SystemMetricsDTO();
        metricsRow.setCurrentTime(timeFormat.format(new Date()));
        collectResponseTimeMetrics(metricsRow);
        collectLoadAverageMetrics(metricsRow);
        return metricsRow;
    }

    private void printMetrics(SystemMetricsDTO metrics) {
        if (rowNumber == 0) {
            System.out.println(metrics.getHeader());
        }
        System.out.println(metrics.getRow());
        rowNumber++;
    }

    private void collectResponseTimeMetrics(SystemMetricsDTO metricsRow) {
        try {
            Long totalRequestCount = getTomcatRequestCount();
            Long totalProcessingTime = getTomcatProcessingTime();

            //get interval values
            long intervalRequestCount = 0;
            long intervalProcessingTime = 0;
            if (previousSystemMetrics != null) {
                intervalRequestCount = totalRequestCount - previousSystemMetrics.getTotalRequestCount();
                intervalProcessingTime = totalProcessingTime - previousSystemMetrics.getTotalProcessingTime();
            }

            //seconds per request
            double intervalAverage = (double) intervalProcessingTime / intervalRequestCount;
            double totalAverage = (double) totalProcessingTime / totalRequestCount;

            metricsRow.setTotalRequestCount(totalRequestCount);
            metricsRow.setTotalProcessingTime(totalProcessingTime);
            metricsRow.setTotalAverage(totalAverage);
            metricsRow.setIntervalRequestCount(intervalRequestCount);
            metricsRow.setIntervalProcessingTime(intervalProcessingTime);
            metricsRow.setIntervalAverage(intervalAverage);
            previousSystemMetrics = metricsRow.duplicate();
        } catch (MalformedObjectNameException | MBeanException | AttributeNotFoundException |
                 InstanceNotFoundException | ReflectionException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void collectLoadAverageMetrics(SystemMetricsDTO metrics) {
        try {
            // Path to the loadavg file
            String filePath = "/proc/loadavg";

            // Read the file
            BufferedReader reader = null;
            reader = new BufferedReader(new FileReader(filePath));
            String line = reader.readLine();
            reader.close();

            if (line != null) {
                // Parse the content
                // Format: load1 load5 load15 nr_running/nr_threads last_pid
                String[] parts = line.split(" ");

                double load1min = Double.parseDouble(parts[0]);
                double load5min = Double.parseDouble(parts[1]);
                double load15min = Double.parseDouble(parts[2]);

                String[] processInfo = parts[3].split("/");
                Integer runningProcesses = Integer.parseInt(processInfo[0]);
                Integer totalProcesses = Integer.parseInt(processInfo[1]);

                Long lastPid = Long.parseLong(parts[4]);

                metrics.setLoad1min(load1min);
                metrics.setLoad5min(load5min);
                metrics.setLoad15min(load15min);
                metrics.setRunningProcesses(runningProcesses);
                metrics.setTotalProcesses(totalProcesses);
                metrics.setLastPid(lastPid);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Long getTomcatRequestCount() throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException, MalformedObjectNameException {
        ObjectName httpsName = new ObjectName("Catalina:name=\"" + TOMCAT_CONNECTOR_NAME + "\",type=GlobalRequestProcessor");
        return ((Integer)mbeanConn.getAttribute(httpsName, "requestCount")).longValue();
    }

    private Long getTomcatProcessingTime() throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException, MalformedObjectNameException {
        ObjectName httpsName = new ObjectName("Catalina:name=\"" + TOMCAT_CONNECTOR_NAME + "\",type=GlobalRequestProcessor");
        return ((Long)mbeanConn.getAttribute(httpsName, "processingTime"));
    }

    public void disconnect() throws IOException {
        if (jmxConnector != null) {
            jmxConnector.close();
            log.error("Disconnected from Tomcat JMX");
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            System.out.println("Usage: java MetricsMonitor");
            System.out.println("Use environment variables to change default values: JMX_HOST JMX_PORT POLLING_INTERVAL_SECONDS MS_PER_REQUEST_THRESHOLD_HIGH MS_PER_REQUEST_THRESHOLD_LOW LOAD_THRESHOLD_HIGH LOAD_THRESHOLD_LOW MS_TO_ENABLE DBHOST DBUSER DBNAME");
            System.exit(1);
        }

        MetricsMonitor monitor = new MetricsMonitor();
        monitor.run();
    }
}
