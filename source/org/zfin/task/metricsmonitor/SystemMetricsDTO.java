package org.zfin.task.metricsmonitor;
import lombok.*;

import java.util.List;

@Data
public class SystemMetricsDTO {

    private String currentTime;
    private Long totalRequestCount;
    private Long totalProcessingTime;
    private Double totalAverage;
    private Long intervalRequestCount;
    private Long intervalProcessingTime;
    private Double intervalAverage;
    private Double load1min;
    private Double load5min;
    private Double load15min;
    private Integer runningProcesses;
    private Integer totalProcesses;
    private Long lastPid;

    public String getHeader() {
        return "currentTime,totalRequestCount,totalProcessingTime,totalAverage,intervalRequestCount,intervalProcessingTime,intervalAverage,load1min,load5min,load15min,runningProcesses,totalProcesses,lastPid";
    }

    public String getRow() {
        List<String> values = List.of(
                currentTime,
                totalRequestCount.toString(),
                totalProcessingTime.toString(),
                String.format("%.3f", totalAverage),
                intervalRequestCount.toString(),
                intervalProcessingTime.toString(),
                String.format("%.3f", intervalAverage),
                load1min.toString(),
                load5min.toString(),
                load15min.toString(),
                runningProcesses.toString(),
                totalProcesses.toString(),
                lastPid.toString());
        return String.join(",", values);
    }

    public SystemMetricsDTO duplicate() {
        SystemMetricsDTO copy = new SystemMetricsDTO();
        copy.currentTime = currentTime;
        copy.totalRequestCount = totalRequestCount;
        copy.totalAverage = totalAverage;
        copy.intervalProcessingTime = intervalProcessingTime;
        copy.totalProcessingTime = totalProcessingTime;
        copy.intervalAverage = intervalAverage;
        copy.intervalRequestCount = intervalRequestCount;
        copy.load5min = load5min;
        copy.lastPid = lastPid;
        copy.load1min = load1min;
        copy.load15min = load15min;
        copy.runningProcesses = runningProcesses;
        copy.totalProcesses = totalProcesses;
        return copy;
    }

}
