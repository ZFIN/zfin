package org.zfin.analytics;

import java.util.List;

public class AnalyticsReportDefinitionList {
    private static final String PROPERTY_ID = "351335549";

    public static List<AnalyticsReportDefinition> getReportDefinitions() {
        return List.of(
            AnalyticsReportDefinition.builder()
                .applicationName("Hello Analytics Reporting")
                .propertyId(PROPERTY_ID)
                .reportName("countryByMonth")
                .dimensions(List.of("year", "month", "country"))
                .metrics(List.of("totalUsers","activeUsers","newUsers","sessions","screenPageViews"))
                .startDate("2023-03-01")
                .endDate("2023-03-02")
                .limit(100000)
                .sort(List.of("year", "month"))
                .build(),
            AnalyticsReportDefinition.builder()
                .applicationName("Hello Analytics Reporting")
                .propertyId(PROPERTY_ID)
                .reportName("countries")
                .dimensions(List.of("country"))
                .metrics(List.of("activeUsers", "newUsers", "totalUsers", "sessions", "screenPageViews"))
                .startDate("2023-03-01")
                .endDate("2023-03-02")
                .limit(100000)
                .sort(List.of("year", "month"))
                .build(),
            AnalyticsReportDefinition.builder()
                .applicationName("Hello Analytics Reporting")
                .propertyId(PROPERTY_ID)
                .reportName("pagePath")
                .dimensions(List.of("year", "month", "pagePath"))
                .metrics(List.of("totalUsers"))
                .startDate("2023-03-01")
                .endDate("2023-03-02")
                .limit(100000)
                .sort(List.of("year", "month"))
                .build(),
            AnalyticsReportDefinition.builder()
                .applicationName("Hello Analytics Reporting")
                .propertyId(PROPERTY_ID)
                .reportName("sources")
                .dimensions(List.of("date", "source", "medium"))
                .metrics(List.of("totalUsers", "sessions", "bounceRate"))
                .startDate("2023-03-01")
                .endDate("2023-03-02")
                .limit(100000)
                .sort(List.of("year", "month"))
                .build(),
            AnalyticsReportDefinition.builder()
                .applicationName("Hello Analytics Reporting")
                .propertyId(PROPERTY_ID)
                .reportName("totalUsers")
                .dimensions(List.of("year", "month"))
                .metrics(List.of("totalUsers"))
                .startDate("2023-03-01")
                .endDate("2023-03-02")
                .limit(100000)
                .sort(List.of("year", "month"))
                .build(),
            AnalyticsReportDefinition.builder()
                .applicationName("Hello Analytics Reporting")
                .propertyId(PROPERTY_ID)
                .reportName("visitorsByMonth")
                .dimensions(List.of("year", "month"))
                .metrics(List.of("totalUsers", "newUsers", "sessions", "screenPageViewsPerSession", "averageSessionDuration", "screenPageViews"))
                .startDate("2023-03-01")
                .endDate("2023-03-02")
                .limit(100000)
                .sort(List.of("year", "month"))
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
