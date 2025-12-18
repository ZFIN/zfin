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
                   "supplementalDataKeys", "relatedActionsKeys", "links", "tags", "tables"})
public class LoadReportAction {

    public void addTag(LoadReportActionTag tag) {
        if (this.tags == null) {
            this.tags = new java.util.ArrayList<>();
        }
        this.tags.add(tag);
    }

    public void addLink(LoadReportActionLink link) {
        if (this.links == null) {
            this.links = new java.util.ArrayList<>();
        }
        if (link == null || link.getTitle() == null || link.getTitle().isEmpty()) {
            return;
        }
        if (this.links.stream().anyMatch(existingLink -> existingLink.getTitle().equals(link.getTitle()))) {
            return; // Avoid adding duplicate links
        }
        this.links.add(link);
    }

    public void addLinks(List<LoadReportActionLink> links) {
        if (this.links == null) {
            this.links = new java.util.ArrayList<>();
        }
        this.links.addAll(links);
    }

    public void addRefSeqLink(String refseq) {
        this.addLink(new LoadReportActionLink("RefSeq:" + refseq, "https://www.ncbi.nlm.nih.gov/nuccore/" + refseq));
    }

    public void addNcbiGeneIdLink(String ncbiId) {
        this.addLink(new LoadReportActionLink("NCBI:" + ncbiId, "https://www.ncbi.nlm.nih.gov/gene/" + ncbiId));
    }

    public void addZdbIdLink(String zdbId) {
        this.addLink(new LoadReportActionLink(zdbId, "https://zfin.org/" + zdbId));
    }

    public void addZdbIdLink(String zdbId, String abbreviation) {
        this.addLink(new LoadReportActionLink(zdbId + " (" + abbreviation + ")", "https://zfin.org/" + zdbId));
    }

    public void addDetails(String details) {
        if (this.details == null) {
            this.details = details;
        } else {
            this.details += "\n" + details;
        }
    }

    public void addRelatedActionsKeys(String key) {
        if (this.relatedActionsKeys == null) {
            this.relatedActionsKeys = new java.util.ArrayList<>();
        }
        if (!this.relatedActionsKeys.contains(key)) {
            this.relatedActionsKeys.add(key);
        }
        this.relatedActionsKeys.add(key);
    }

    public void generateId() {
        Object id = getId();
        String idString = (id != null) ? id.toString() : null;
        if (idString == null || idString.isEmpty()) {
            String randomId = java.util.UUID.randomUUID().toString();
            this.setId(randomId);
        }
    }

    public enum Type {LOAD, INFO, WARNING, ERROR, DELETE, IGNORE, DUPES, UPDATE, REPORTS}

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

    @JsonProperty("tables")
    private List<LoadReportSummaryTable> tables;
    
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

    public Object getRelatedEntityField(String fieldName, Object defaultValue) {
        if (relatedEntityFields != null && relatedEntityFields.containsKey(fieldName)) {
            return relatedEntityFields.get(fieldName);
        }
        return defaultValue;
    }
}