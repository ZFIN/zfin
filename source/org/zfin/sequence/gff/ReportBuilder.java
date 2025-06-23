package org.zfin.sequence.gff;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//TODO: replace this with a class that matches the new json schema in home/uniprot/zfin-report-schema.json
public class ReportBuilder {

    private ObjectMapper objectMapper;
    private String title;
    private String releaseID;
    private List<SummaryTable> summaryTables;
    private ArrayNode actions;

    public ReportBuilder() {
        this.objectMapper = new ObjectMapper();
        this.title = "NCBI Load Report";
        this.releaseID = "";
        this.summaryTables = new ArrayList<>();
        this.actions = objectMapper.createArrayNode();
    }

    public ReportBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public ReportBuilder setReleaseID(String releaseID) {
        this.releaseID = releaseID;
        return this;
    }

    public SummaryTable addSummaryTable(String description) {
        SummaryTable table = new SummaryTable(description);
        summaryTables.add(table);
        return table;
    }

    public ObjectNode build() {
        // Create root object
        ObjectNode jsonReportData = objectMapper.createObjectNode();

        // Create meta section
        ObjectNode meta = objectMapper.createObjectNode();
        meta.put("title", title);
        meta.put("releaseID", releaseID);
        meta.put("creationDate", System.currentTimeMillis());
        jsonReportData.set("meta", meta);

        // Create summary section
        ObjectNode summary = objectMapper.createObjectNode();
        summary.put("description", "Summary of NCBI Load Report");

        // Create tables array
        ArrayNode tables = objectMapper.createArrayNode();
        for (SummaryTable summaryTable : summaryTables) {
            tables.add(summaryTable.build(objectMapper));
        }

        summary.set("tables", tables);
        jsonReportData.set("summary", summary);

        // Add actions
        jsonReportData.set("actions", actions);

        return jsonReportData;
    }

    public class SummaryTable {
        private String description;
        private List<String> headerKeys;
        private List<String> headerTitles;
        private List<SummaryRow> rows;

        public SummaryTable(String description) {
            this.description = description;
            this.headerKeys = new ArrayList<>();
            this.headerTitles = new ArrayList<>();
            this.rows = new ArrayList<>();

        }

        public SummaryTable setHeaders(String[] keys, String[] titles) {
            if (keys.length != titles.length) {
                throw new IllegalArgumentException("Keys and titles arrays must have the same length");
            }
            this.headerKeys.clear();
            this.headerTitles.clear();
            for (int i = 0; i < keys.length; i++) {
                this.headerKeys.add(keys[i]);
                this.headerTitles.add(titles[i]);
            }
            return this;
        }

        public SummaryTable setHeaders(List<String> keys) {
            this.headerKeys.clear();
            this.headerTitles.clear();
            for (String key : keys) {
                this.headerKeys.add(key);
                this.headerTitles.add(key); // Default title is the same as key
            }
            return this;
        }

        public SummaryTable addBeforeAfterCountSummaryRow(String title, int beforeCount, int afterCount) {
            if (headerKeys.isEmpty()) {
                throw new IllegalStateException("Headers must be set before adding rows");
            }
            Map<String, String> row = new LinkedHashMap<>();
            row.put(headerKeys.get(0), title);
            row.put(headerKeys.get(1), String.valueOf(beforeCount));
            row.put(headerKeys.get(2), String.valueOf(afterCount));
            row.put(headerKeys.get(3), percentageDisplay(beforeCount, afterCount));
            return addSummaryRow(row);

        }
        public SummaryTable addSummaryRow(List<String> row) {
            // Ensure the row matches the header size
            if (row.size() != headerKeys.size()) {
                throw new IllegalArgumentException("Row size must match header size");
            }
            Map<String, String> rowMap = new LinkedHashMap<>();
            for (int i = 0; i < row.size(); i++) {
                rowMap.put(headerKeys.get(i), row.get(i));
            }
            rows.add(new SummaryRow(rowMap));
            return this;
        }

        public SummaryTable addSummaryRow(Map<String, String> row) {
            rows.add(new SummaryRow(row));
            return this;
        }

        public ObjectNode build(ObjectMapper objectMapper) {
            ObjectNode table = objectMapper.createObjectNode();
            table.put("description", description);

            // Create headers array
            ArrayNode headers = objectMapper.createArrayNode();
            for (int i = 0; i < headerKeys.size(); i++) {
                headers.add(createHeader(objectMapper, headerKeys.get(i), headerTitles.get(i)));
            }
            table.set("headers", headers);

            // Create rows array
            ArrayNode rowsArray = objectMapper.createArrayNode();
            for (SummaryRow row : rows) {
                rowsArray.add(row.build(objectMapper));
            }
            table.set("rows", rowsArray);

            return table;
        }

        private ObjectNode createHeader(ObjectMapper objectMapper, String key, String title) {
            ObjectNode header = objectMapper.createObjectNode();
            header.put("key", key);
            header.put("title", title);
            return header;
        }
    }

    public class SummaryRow {
        private final Map<String, String> values = new LinkedHashMap<>();

        public SummaryRow(Map<String, String> row) {
            this.values.putAll(row);
        }

        public SummaryRow put(String key, String value) {
            values.put(key, value);
            return this;
        }

        public ObjectNode build(ObjectMapper objectMapper) {
            ObjectNode row = objectMapper.createObjectNode();
            for (Map.Entry<String, String> entry : values.entrySet()) {
                row.put(entry.getKey(), entry.getValue());
            }
            return row;
        }
    }

    private String percentageDisplay(int before, int after) {
        if (before == 0) {
            return after == 0 ? "0%" : "N/A";
        }
        double percentage = ((double)(after - before) / before) * 100;
        return String.format("%.2f%%", percentage);
    }

    public void writeJsonToFile(ObjectNode jsonData, File filePath) throws IOException {
        objectMapper.writer().writeValue(filePath, jsonData);
    }

    public String getJsonString(ObjectNode jsonData) throws IOException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonData);
    }

}