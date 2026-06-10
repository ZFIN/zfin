package org.zfin.report;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory model for a generic nested report, matching the schema in
 * {@code home/uniprot/reportmaker/report-schema.json}.
 *
 * <p>A report is a tree of {@link ReportNode}s. Reusable definitions for fields,
 * table schemas, tags and categories live in {@link Definitions}. Long-form
 * content (logs, raw text) lives in {@link #blobs} keyed by id and referenced
 * from nodes via {@code blobRefs}. Cross-tree relationships live in
 * {@link #edges}.
 *
 * <p>Built incrementally with mutable POJOs and fluent setters; serialized to
 * JSON by {@link ReportWriter} which inlines it into the report template.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Report {

    private Meta meta;
    private Definitions definitions;
    private ReportNode root;
    private Map<String, String> blobs;
    private List<Edge> edges;

    public Meta getMeta()                   { return meta; }
    public Definitions getDefinitions()     { return definitions; }
    public ReportNode getRoot()             { return root; }
    public Map<String, String> getBlobs()   { return blobs; }
    public List<Edge> getEdges()            { return edges; }

    public Report meta(Meta m)              { this.meta = m; return this; }
    public Report definitions(Definitions d){ this.definitions = d; return this; }
    public Report root(ReportNode n)        { this.root = n; return this; }

    public Report addBlob(String id, String content) {
        if (blobs == null) blobs = new LinkedHashMap<>();
        blobs.put(id, content);
        return this;
    }

    public Report addEdge(Edge e) {
        if (edges == null) edges = new ArrayList<>();
        edges.add(e);
        return this;
    }

    public Report addEdge(String from, String to, String type) {
        return addEdge(new Edge().from(from).to(to).type(type));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Meta {
        private String title;
        private String subtitle;
        /** Unix epoch milliseconds. */
        private Long createdAt;
        private String schemaVersion;

        public String getTitle()         { return title; }
        public String getSubtitle()      { return subtitle; }
        public Long getCreatedAt()       { return createdAt; }
        public String getSchemaVersion() { return schemaVersion; }

        public Meta title(String v)         { this.title = v; return this; }
        public Meta subtitle(String v)      { this.subtitle = v; return this; }
        public Meta createdAt(Long v)       { this.createdAt = v; return this; }
        public Meta schemaVersion(String v) { this.schemaVersion = v; return this; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Definitions {
        private Map<String, FieldDef> fields;
        private Map<String, TableSchema> tableSchemas;
        private Map<String, TagDef> tags;
        private Map<String, CategoryDef> categories;

        public Map<String, FieldDef> getFields()           { return fields; }
        public Map<String, TableSchema> getTableSchemas()  { return tableSchemas; }
        public Map<String, TagDef> getTags()               { return tags; }
        public Map<String, CategoryDef> getCategories()    { return categories; }

        public Definitions field(String key, FieldDef def) {
            if (fields == null) fields = new LinkedHashMap<>();
            fields.put(key, def);
            return this;
        }

        public Definitions tableSchema(String key, TableSchema def) {
            if (tableSchemas == null) tableSchemas = new LinkedHashMap<>();
            tableSchemas.put(key, def);
            return this;
        }

        public Definitions tag(String key, TagDef def) {
            if (tags == null) tags = new LinkedHashMap<>();
            tags.put(key, def);
            return this;
        }

        public Definitions category(String key, CategoryDef def) {
            if (categories == null) categories = new LinkedHashMap<>();
            categories.put(key, def);
            return this;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FieldDef {
        private String label;
        /** One of: text, number, link, date, boolean, html. */
        private String type;
        /** For type=link, supports {value} placeholder. */
        private String hrefTemplate;
        private String description;

        public String getLabel()        { return label; }
        public String getType()         { return type; }
        public String getHrefTemplate() { return hrefTemplate; }
        public String getDescription()  { return description; }

        public FieldDef label(String v)        { this.label = v; return this; }
        public FieldDef type(String v)         { this.type = v; return this; }
        public FieldDef hrefTemplate(String v) { this.hrefTemplate = v; return this; }
        public FieldDef description(String v)  { this.description = v; return this; }

        public static FieldDef text(String label) {
            return new FieldDef().label(label).type("text");
        }
        public static FieldDef number(String label) {
            return new FieldDef().label(label).type("number");
        }
        public static FieldDef bool(String label) {
            return new FieldDef().label(label).type("boolean");
        }
        public static FieldDef link(String label, String hrefTemplate) {
            return new FieldDef().label(label).type("link").hrefTemplate(hrefTemplate);
        }
        public static FieldDef html(String label) {
            return new FieldDef().label(label).type("html");
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TableSchema {
        private String description;
        private List<ReportTable.Column> columns;

        public String getDescription()              { return description; }
        public List<ReportTable.Column> getColumns(){ return columns; }

        public TableSchema description(String v)            { this.description = v; return this; }
        public TableSchema columns(List<ReportTable.Column> c) { this.columns = c; return this; }

        public TableSchema addColumn(ReportTable.Column c) {
            if (columns == null) columns = new ArrayList<>();
            columns.add(c);
            return this;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TagDef {
        private String label;
        /** Any CSS color. */
        private String color;
        private String description;

        public String getLabel()       { return label; }
        public String getColor()       { return color; }
        public String getDescription() { return description; }

        public TagDef label(String v)       { this.label = v; return this; }
        public TagDef color(String v)       { this.color = v; return this; }
        public TagDef description(String v) { this.description = v; return this; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CategoryDef {
        private String label;
        /** Sort order among siblings carrying this category. */
        private Integer order;
        /** Short emoji or text shown next to the category. */
        private String icon;
        private String color;
        private String description;

        public String getLabel()       { return label; }
        public Integer getOrder()      { return order; }
        public String getIcon()        { return icon; }
        public String getColor()       { return color; }
        public String getDescription() { return description; }

        public CategoryDef label(String v)       { this.label = v; return this; }
        public CategoryDef order(Integer v)      { this.order = v; return this; }
        public CategoryDef icon(String v)        { this.icon = v; return this; }
        public CategoryDef color(String v)       { this.color = v; return this; }
        public CategoryDef description(String v) { this.description = v; return this; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Body {
        /** "text" (preserves whitespace) or "html" (trusted markup). */
        private String format;
        private String content;

        public String getFormat()  { return format; }
        public String getContent() { return content; }

        public Body format(String v)  { this.format = v; return this; }
        public Body content(String v) { this.content = v; return this; }

        public static Body text(String content) {
            return new Body().format("text").content(content);
        }
        public static Body html(String content) {
            return new Body().format("html").content(content);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Link {
        private String title;
        private String href;

        public String getTitle() { return title; }
        public String getHref()  { return href; }

        public Link title(String v) { this.title = v; return this; }
        public Link href(String v)  { this.href = v; return this; }

        public static Link of(String title, String href) {
            return new Link().title(title).href(href);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Edge {
        private String from;
        private String to;
        private String type;

        public String getFrom() { return from; }
        public String getTo()   { return to; }
        public String getType() { return type; }

        public Edge from(String v) { this.from = v; return this; }
        public Edge to(String v)   { this.to = v; return this; }
        public Edge type(String v) { this.type = v; return this; }
    }
}
