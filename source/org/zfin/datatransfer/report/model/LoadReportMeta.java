package org.zfin.datatransfer.report.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

/**
 * Represents the metadata section of a ZFIN load report.
 * Contains title, optional release ID, and creation timestamp.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"title", "releaseID", "creationDate"})
public class LoadReportMeta {
    
    @JsonProperty(value = "title", required = true)
    private String title;
    
    @JsonProperty("releaseID")
    private String releaseID;
    
    @JsonProperty(value = "creationDate", required = true)
    private Long creationDate;
    
    public LoadReportMeta() {
    }
    
    public LoadReportMeta(String title, Long creationDate) {
        this.title = title;
        this.creationDate = creationDate;
    }
    
    public LoadReportMeta(String title, String releaseID, Long creationDate) {
        this.title = title;
        this.releaseID = releaseID;
        this.creationDate = creationDate;
    }
}