package org.zfin.datatransfer.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

/**
 * Represents a link associated with a LoadReportAction.
 * Contains title and href (URI) properties.
 */
@Data
@JsonPropertyOrder({"title", "href"})
public class LoadReportActionLink {
    
    @JsonProperty(value = "title", required = true)
    private String title;
    
    @JsonProperty(value = "href", required = true)
    private String href;
    
    public LoadReportActionLink() {
    }
    
    public LoadReportActionLink(String title, String href) {
        this.title = title;
        this.href = href;
    }
}