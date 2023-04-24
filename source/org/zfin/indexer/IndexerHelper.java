package org.zfin.indexer;

import lombok.extern.log4j.Log4j2;
import org.zfin.framework.api.Duration;

import java.time.LocalDateTime;

@Log4j2
public class IndexerHelper {

    private LocalDateTime startTime;

    protected void addQuickReport(String message, Integer numberOfRecords) {
        if (message != null) {
            message += " Duration: " + getDuration();
            message += ", Records: " + String.format("%,d", numberOfRecords);
            log.info(message);
        }
    }

    protected void startProcess(String message) {
        if (message != null)
            log.info(message);
        this.startTime = LocalDateTime.now();
    }

    public String getDuration() {
        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = new Duration(startTime, endTime);
        return duration.toString();
    }

}
