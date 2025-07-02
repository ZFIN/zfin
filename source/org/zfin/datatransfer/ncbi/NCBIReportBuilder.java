package org.zfin.datatransfer.ncbi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.zfin.datatransfer.report.model.*;
import org.zfin.datatransfer.report.util.ZfinReportSerializationUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NCBIReportBuilder {

    private String title;
    private String releaseID;
    private List<SummaryTableBuilder> summaryTables;
    private List<LoadReportAction> actions;

    public NCBIReportBuilder() {
        this.title = "NCBI Load Report";
        this.releaseID = "";
        this.summaryTables = new ArrayList<>();
        this.actions = new ArrayList<>();
    }

    /**
     * Factory method to create a new NCBIReportBuilder instance.
     * Provides a fluent starting point for report construction.
     */
    public static NCBIReportBuilder create() {
        return new NCBIReportBuilder();
    }

    public NCBIReportBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public NCBIReportBuilder setReleaseID(String releaseID) {
        this.releaseID = releaseID;
        return this;
    }

    public SummaryTableBuilder addSummaryTable(String description) {
        SummaryTableBuilder table = new SummaryTableBuilder(description);
        summaryTables.add(table);
        return table;
    }

    public NCBIReportBuilder addAction(LoadReportAction action) {
        actions.add(action);
        return this;
    }

    public NCBIReportBuilder addActions(List<LoadReportAction> actions) {
        this.actions.addAll(actions);
        return this;
    }

    /**
     * Builds the complete ZfinReport and returns it as an ObjectNode for backward compatibility.
     * This method creates a schema-compliant ZfinReport internally and serializes it to JSON.
     */
    public ObjectNode build() {
        // Create ZfinReport using schema-compliant classes
        ZfinReport report = buildZfinReport();
        
        try {
            // Convert to JSON string then parse back to ObjectNode for compatibility
            String jsonString = ZfinReportSerializationUtil.toJson(report);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(jsonString).deepCopy();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build NCBI report", e);
        }
    }

    /**
     * Builds and returns the complete ZfinReport object using schema-compliant classes.
     * This method provides access to the strongly-typed report object.
     */
    public ZfinReport buildZfinReport() {
        // Create metadata
        LoadReportMeta meta = new LoadReportMeta(title, releaseID, System.currentTimeMillis());
        
        // Build summary tables
        List<LoadReportSummaryTable> tables = new ArrayList<>();
        for (SummaryTableBuilder tableBuilder : summaryTables) {
            tables.add(tableBuilder.buildTable());
        }
        
        // Create summary
        LoadReportSummary summary = new LoadReportSummary("Summary of NCBI Load Report", tables);
        
        // Create supplemental data (empty for now, but required by schema)
        Map<String, Object> supplementalData = new HashMap<>();
        
        // Build the complete report
        ZfinReport report = new ZfinReport();
        report.setMeta(meta);
        report.setSummary(summary);
        report.setSupplementalData(supplementalData);
        report.setActions(actions);
        
        return report;
    }

    /**
     * Builder class for creating LoadReportSummaryTable objects using the ZfinReport schema.
     */
    public class SummaryTableBuilder {
        private String description;
        private List<LoadReportTableHeader> headers;
        private List<Map<String, Object>> rows;

        public SummaryTableBuilder(String description) {
            this.description = description;
            this.headers = new ArrayList<>();
            this.rows = new ArrayList<>();
        }

        public SummaryTableBuilder setHeaders(String[] keys, String[] titles) {
            if (keys.length != titles.length) {
                throw new IllegalArgumentException("Keys and titles arrays must have the same length");
            }
            this.headers.clear();
            for (int i = 0; i < keys.length; i++) {
                this.headers.add(new LoadReportTableHeader(keys[i], titles[i]));
            }
            return this;
        }

        public SummaryTableBuilder setHeaders(List<String> keys) {
            this.headers.clear();
            for (String key : keys) {
                this.headers.add(new LoadReportTableHeader(key, key)); // Default title is the same as key
            }
            return this;
        }

        public SummaryTableBuilder addBeforeAfterCountSummaryRow(String title, Integer beforeCount, Integer afterCount) {
            if (headers.isEmpty()) {
                throw new IllegalStateException("Headers must be set before adding rows");
            }
            if (beforeCount == null) {
                beforeCount = 0; // Default to 0 if null
            }
            if (afterCount == null) {
                afterCount = 0; // Default to 0 if null
            }

            Map<String, Object> row = new LinkedHashMap<>();
            row.put(headers.get(0).getKey(), title);
            row.put(headers.get(1).getKey(), beforeCount);
            row.put(headers.get(2).getKey(), afterCount);
            row.put(headers.get(3).getKey(), percentageDisplay(beforeCount, afterCount));
            return addObjectRow(row);
        }

        public SummaryTableBuilder addSummaryRow(List<String> row) {
            // Ensure the row matches the header size
            if (row.size() != headers.size()) {
                throw new IllegalArgumentException("Row size must match header size");
            }
            Map<String, Object> rowMap = new LinkedHashMap<>();
            for (int i = 0; i < row.size(); i++) {
                rowMap.put(headers.get(i).getKey(), row.get(i));
            }
            rows.add(rowMap);
            return this;
        }

        public SummaryTableBuilder addSummaryRow(Map<String, String> row) {
            // Convert String values to Object for schema compatibility
            Map<String, Object> objectRow = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : row.entrySet()) {
                objectRow.put(entry.getKey(), entry.getValue());
            }
            rows.add(objectRow);
            return this;
        }

        private SummaryTableBuilder addObjectRow(Map<String, Object> row) {
            rows.add(row);
            return this;
        }

        /**
         * Builds a LoadReportSummaryTable using the ZfinReport schema-compliant classes.
         */
        LoadReportSummaryTable buildTable() {
            LoadReportSummaryTable table = new LoadReportSummaryTable();
            table.setDescription(description);
            table.setHeaders(headers);
            table.setRows(rows);
            return table;
        }

        public ObjectNode build(ObjectMapper objectMapper) {
            LoadReportSummaryTable table = buildTable();
            try {
                String json = ZfinReportSerializationUtil.toJson(table);
                return (ObjectNode) objectMapper.readTree(json);
            } catch (Exception e) {
                throw new RuntimeException("Failed to build table", e);
            }
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
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writer().writeValue(filePath, jsonData);
    }

    public void writeJsonToFile(ZfinReport report, File filePath) throws IOException {
        String json = ZfinReportSerializationUtil.toPrettyJson(report);
        java.nio.file.Files.write(filePath.toPath(), json.getBytes());
    }

    public String getJsonString(ObjectNode jsonData) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonData);
    }

    public String getJsonString(ZfinReport report) throws IOException {
        return ZfinReportSerializationUtil.toPrettyJson(report);
    }

}