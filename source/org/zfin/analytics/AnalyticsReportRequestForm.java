package org.zfin.analytics;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalyticsReportRequestForm {
    private String credentials;
    private String reportName;
    private String start;
    private String end;
}
