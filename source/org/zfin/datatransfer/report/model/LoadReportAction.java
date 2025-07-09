package org.zfin.datatransfer.report.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Represents an action performed during a ZFIN load process.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "type", "subType", "accession", "geneZdbID", "details", "length", 
                   "uniprotAccessions", "relatedEntityID", "dbName", "md5", "relatedEntityFields", 
                   "supplementalDataKeys", "relatedActionsKeys", "links", "tags"})
public class LoadReportAction {

    public void addTag(LoadReportActionTag tag) {
        if (this.tags == null) {
            this.tags = new java.util.ArrayList<>();
        }
        this.tags.add(tag);
    }

    public enum Type {LOAD, INFO, WARNING, ERROR, DELETE, IGNORE, DUPES, UPDATE}

    // Required fields
    @JsonProperty(value = "id", required = true)
    private Object id; // Can be string or integer
    
    @JsonProperty(value = "type", required = true)
    private Type type;
    
    @JsonProperty(value = "subType", required = true)
    private String subType;
    
    @JsonProperty(value = "accession", required = false)
    private String accession;
    
    @JsonProperty(value = "geneZdbID", required = false)
    private String geneZdbID;
    
    @JsonProperty(value = "details", required = false)
    private String details;
    
    @JsonProperty(value = "length", required = false)
    private String length;
    
    @JsonProperty(value = "supplementalDataKeys", required = false)
    private List<String> supplementalDataKeys;
    
    // Optional fields
    @JsonProperty("uniprotAccessions")
    private List<String> uniprotAccessions;
    
    @JsonProperty("relatedEntityID")
    private String relatedEntityID;
    
    @JsonProperty("dbName")
    private String dbName;
    
    @JsonProperty("md5")
    private String md5;
    
    @JsonProperty("relatedEntityFields")
    private Map<String, Object> relatedEntityFields;
    
    @JsonProperty("relatedActionsKeys")
    private List<String> relatedActionsKeys;
    
    @JsonProperty("links")
    private List<LoadReportActionLink> links;
    
    @JsonProperty("tags")
    private List<LoadReportActionTag> tags;
    
    public LoadReportAction() {
    }
    
    public LoadReportAction(Object id, Type type, String subType, String accession,
                           String geneZdbID, String details, String length, 
                           List<String> supplementalDataKeys) {
        this.id = id;
        this.type = type;
        this.subType = subType;
        this.accession = accession;
        this.geneZdbID = geneZdbID;
        this.details = details;
        this.length = length;
        this.supplementalDataKeys = supplementalDataKeys;
    }
}