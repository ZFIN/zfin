package org.zfin.report;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tabular data attached to a {@link ReportNode}.
 *
 * <p>Either references a shared schema by key ({@link #schemaRef}) or carries
 * inline {@link #columns}. {@link #rows} are always present and shaped as
 * {column-key -> value} maps.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportTable {

    private String schemaRef;
    private String title;
    private String description;
    private List<Column> columns;
    private List<Map<String, Object>> rows;

    public String getSchemaRef()             { return schemaRef; }
    public String getTitle()                 { return title; }
    public String getDescription()           { return description; }
    public List<Column> getColumns()         { return columns; }
    public List<Map<String, Object>> getRows() { return rows; }

    public ReportTable schemaRef(String v)   { this.schemaRef = v; return this; }
    public ReportTable title(String v)       { this.title = v; return this; }
    public ReportTable description(String v) { this.description = v; return this; }

    public ReportTable addColumn(Column c) {
        if (columns == null) columns = new ArrayList<>();
        columns.add(c);
        return this;
    }

    public ReportTable addRow(Map<String, Object> row) {
        if (rows == null) rows = new ArrayList<>();
        rows.add(row);
        return this;
    }

    /**
     * Convenience for adding a row from alternating key/value pairs.
     * <pre>{@code table.addRow("what", "db_link records", "before", 40824, "after", 48703);}</pre>
     */
    public ReportTable addRow(Object... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("addRow requires an even number of arguments (key/value pairs)");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            row.put((String) keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return addRow(row);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Column {
        private String key;
        private String title;
        /** Key into Definitions.fields, controls cell rendering. */
        private String fieldRef;

        public String getKey()      { return key; }
        public String getTitle()    { return title; }
        public String getFieldRef() { return fieldRef; }

        public Column key(String v)      { this.key = v; return this; }
        public Column title(String v)    { this.title = v; return this; }
        public Column fieldRef(String v) { this.fieldRef = v; return this; }

        public static Column of(String key, String title) {
            return new Column().key(key).title(title);
        }

        public static Column of(String key, String title, String fieldRef) {
            return new Column().key(key).title(title).fieldRef(fieldRef);
        }
    }
}
