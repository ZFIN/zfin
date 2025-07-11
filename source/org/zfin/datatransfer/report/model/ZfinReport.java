package org.zfin.datatransfer.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Root class representing a complete ZFIN report structure.
 * Contains metadata, summary information, supplemental data, and actions.
 * 
 * This class serializes to JSON according to the schema defined in zfin-report-schema.json.
 */
@Data
@JsonPropertyOrder({"meta", "summary", "supplementalData", "actions"})
public class ZfinReport {

    @JsonProperty(value = "meta", required = true)
    private LoadReportMeta meta;

    @JsonProperty(value = "summary", required = true)
    private LoadReportSummary summary;

    @JsonProperty(value = "supplementalData", required = true)
    private Map<String, Object> supplementalData;

    @JsonProperty(value = "actions", required = true)
    private List<LoadReportAction> actions;

    /**
     * Default constructor for Jackson deserialization.
     */
    public ZfinReport() {
    }

    /**
     * Constructor with all required fields.
     */
    public ZfinReport(LoadReportMeta meta, LoadReportSummary summary, 
                     Map<String, Object> supplementalData, List<LoadReportAction> actions) {
        this.meta = meta;
        this.summary = summary;
        this.supplementalData = supplementalData;
        this.actions = actions;
    }

}