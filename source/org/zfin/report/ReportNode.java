package org.zfin.report;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Recursive node in a {@link Report} tree.
 *
 * <p>Every node carries the same shape: an id (required), and any of: title,
 * structured fields, body, tables, tags, links, blob references, child nodes.
 * The renderer treats intermediate nodes (those with children) as drill-down
 * targets and leaves as detail items.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportNode {

    private String id;
    private String title;
    private String categoryRef;
    /** Optional explicit count shown in the tree. If null, the renderer sums children's counts. */
    private Long count;
    private Map<String, Object> fields;
    private Report.Body body;
    private List<ReportTable> tables;
    private List<String> tags;
    private List<Report.Link> links;
    private List<String> blobRefs;
    private List<ReportNode> children;

    public String getId()                     { return id; }
    public String getTitle()                  { return title; }
    public String getCategoryRef()            { return categoryRef; }
    public Long getCount()                    { return count; }
    public Map<String, Object> getFields()    { return fields; }
    public Report.Body getBody()              { return body; }
    public List<ReportTable> getTables()      { return tables; }
    public List<String> getTags()             { return tags; }
    public List<Report.Link> getLinks()       { return links; }
    public List<String> getBlobRefs()         { return blobRefs; }
    public List<ReportNode> getChildren()     { return children; }

    public ReportNode id(String v)             { this.id = v; return this; }
    public ReportNode title(String v)          { this.title = v; return this; }
    public ReportNode categoryRef(String v)    { this.categoryRef = v; return this; }
    public ReportNode count(long v)            { this.count = v; return this; }
    public ReportNode body(Report.Body v)      { this.body = v; return this; }

    public ReportNode field(String key, Object value) {
        if (fields == null) fields = new LinkedHashMap<>();
        fields.put(key, value);
        return this;
    }

    public ReportNode addTable(ReportTable t) {
        if (tables == null) tables = new ArrayList<>();
        tables.add(t);
        return this;
    }

    public ReportNode addTag(String tagKey) {
        if (tags == null) tags = new ArrayList<>();
        tags.add(tagKey);
        return this;
    }

    public ReportNode addLink(Report.Link link) {
        if (links == null) links = new ArrayList<>();
        links.add(link);
        return this;
    }

    public ReportNode addLink(String title, String href) {
        return addLink(Report.Link.of(title, href));
    }

    public ReportNode addBlobRef(String blobId) {
        if (blobRefs == null) blobRefs = new ArrayList<>();
        blobRefs.add(blobId);
        return this;
    }

    public ReportNode addChild(ReportNode child) {
        if (children == null) children = new ArrayList<>();
        children.add(child);
        return this;
    }
}
