package org.zfin.analytics;

import java.util.List;

public class AnalyticsReportDefinitionList {
    public static List<AnalyticsReportDefinition> getReportDefinitions() {
        return List.of(
            AnalyticsReportDefinition.builder()
                .applicationName("Hello Analytics Reporting")
                .propertyId("351335549")
                .viewId("N/A")
                .reportName("pagePath")
                .dimensions(List.of("year", "month", "pagePath"))
                .metrics(List.of("totalUsers"))
                .startDate("2023-03-01")
                .endDate("2023-03-02")
                .limit(100000)
                .sort(List.of("year", "month"))
                .nextPageToken(null)
                .build(),
            AnalyticsReportDefinition.builder()
                .applicationName("Hello Analytics Reporting")
                .propertyId("351335549")
                .viewId("N/A")
                .reportName("countryByMonth")
                .dimensions(List.of("year", "month", "country"))
                .metrics(List.of("totalUsers","activeUsers","newUsers","sessions","views"))
                .startDate("2023-03-01")
                .endDate("2023-03-02")
                .limit(100000)
                .sort(List.of("year", "month"))
                .nextPageToken(null)
                .build()
        );
    }

    public static List<String> getReportNames() {
        return getReportDefinitions().stream()
            .map(AnalyticsReportDefinition::getReportName)
            .toList();
    }

    public static AnalyticsReportDefinition get(String reportName) {
        return getReportDefinitions().stream()
            .filter(definition -> definition.getReportName().equals(reportName))
            .findFirst()
            .orElse(null);
    }
}
