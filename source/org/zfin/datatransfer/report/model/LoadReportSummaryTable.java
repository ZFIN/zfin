package org.zfin.datatransfer.report.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Represents a summary statistics table in a ZFIN load report.
 * Contains optional description, optional headers, and required rows.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoadReportSummaryTable {
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("headers")
    private List<LoadReportTableHeader> headers;
    
    @JsonProperty(value = "rows", required = true)
    private List<Map<String, Object>> rows;
    
    public LoadReportSummaryTable() {
    }
    
    public LoadReportSummaryTable(List<Map<String, Object>> rows) {
        this.rows = rows;
    }
    
    public LoadReportSummaryTable(String description, List<LoadReportTableHeader> headers, List<Map<String, Object>> rows) {
        this.description = description;
        this.headers = headers;
        this.rows = rows;
    }
}