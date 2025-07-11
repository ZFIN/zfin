package org.zfin.datatransfer.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

/**
 * Represents the summary section of a ZFIN load report.
 * Contains descriptive text and an array of summary statistics tables.
 */
@Data
@JsonPropertyOrder({"description", "tables"})
public class LoadReportSummary {
    
    @JsonProperty(value = "description", required = true)
    private String description;
    
    @JsonProperty(value = "tables", required = true)
    private List<LoadReportSummaryTable> tables;
    
    public LoadReportSummary() {
    }
    
    public LoadReportSummary(String description, List<LoadReportSummaryTable> tables) {
        this.description = description;
        this.tables = tables;
    }
}