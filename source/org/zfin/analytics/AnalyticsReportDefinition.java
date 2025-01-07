package org.zfin.analytics;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Builder
public class AnalyticsReportDefinition {
    public String applicationName;
    public String propertyId;
    public String reportName;
    public List<String> dimensions;
    public List<String> metrics;
    public String startDate;
    public String endDate;
    public Integer limit;
    public List<String> sort;

    public Config convertToAnalyticsReportRunnerConfig() {
        Config config = new Config();
        config.applicationName = applicationName;
        config.propertyId = propertyId;
        config.reportName = reportName;
        config.dimensions = dimensions;
        config.metrics = metrics;
        config.startDate = startDate;
        config.endDate = endDate;
        config.limit = limit;
        config.sort = sort;

        return config;
    }
}
