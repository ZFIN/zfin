package org.zfin.datatransfer.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

/**
 * Represents a table header definition with key and display title.
 * Used to define column structure in LoadReportSummaryTable.
 */
@Data
@JsonPropertyOrder({"key", "title"})
public class LoadReportTableHeader {
    
    @JsonProperty(value = "key", required = true)
    private String key;
    
    @JsonProperty(value = "title", required = true)
    private String title;
    
    public LoadReportTableHeader() {
    }
    
    public LoadReportTableHeader(String key, String title) {
        this.key = key;
        this.title = title;
    }
}