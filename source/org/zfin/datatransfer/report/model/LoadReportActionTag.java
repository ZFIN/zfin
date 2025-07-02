package org.zfin.datatransfer.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

/**
 * Represents a tag associated with a LoadReportAction.
 * Contains name and value properties.
 */
@Data
@JsonPropertyOrder({"name", "value"})
public class LoadReportActionTag {
    
    @JsonProperty(value = "name", required = true)
    private String name;
    
    @JsonProperty(value = "value", required = true)
    private String value;
    
    public LoadReportActionTag() {
    }
    
    public LoadReportActionTag(String name, String value) {
        this.name = name;
        this.value = value;
    }
}