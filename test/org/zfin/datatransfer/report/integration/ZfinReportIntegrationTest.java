package org.zfin.datatransfer.report.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.zfin.datatransfer.report.model.*;
import org.zfin.datatransfer.report.util.ZfinReportSerializationUtil;
import org.zfin.datatransfer.report.util.ZfinReportValidator;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test demonstrating the complete ZFIN report workflow:
 * 1. Create a complete ZfinReport with all required and optional fields
 * 2. Serialize to JSON
 * 3. Validate against schema
 * 4. Verify the complete process works end-to-end
 * 
 * This test serves as both a validation of the complete system and
 * an example of how to use the ZFIN report serialization functionality.
 */
public class ZfinReportIntegrationTest {

    @Test
    @DisplayName("Complete ZFIN report workflow - creation, serialization, and validation")
    void testCompleteZfinReportWorkflow() throws Exception {
        // 1. Create a comprehensive ZfinReport with all fields populated
        ZfinReport report = createCompleteReport();
        
        // 2. Serialize the report to JSON
        String jsonString = ZfinReportSerializationUtil.toJson(report);
        assertNotNull(jsonString, "JSON serialization should not be null");
        assertFalse(jsonString.trim().isEmpty(), "JSON serialization should not be empty");
        
        // 3. Serialize to pretty JSON for readability
        String prettyJson = ZfinReportSerializationUtil.toPrettyJson(report);
        assertNotNull(prettyJson, "Pretty JSON serialization should not be null");
        assertTrue(prettyJson.length() > jsonString.length(), "Pretty JSON should be longer than compact JSON");
        
        // 4. Validate the report object against the schema
        ZfinReportValidator.ValidationResult reportValidation = ZfinReportValidator.validate(report);
        assertTrue(reportValidation.isValid(), 
            "Complete report should be valid. Errors: " + reportValidation.getErrorMessage());
        assertTrue(reportValidation.getErrors().isEmpty(), "Valid report should have no errors");
        
        // 5. Validate the JSON string against the schema
        ZfinReportValidator.ValidationResult jsonValidation = ZfinReportValidator.validateJson(jsonString);
        assertTrue(jsonValidation.isValid(), 
            "Serialized JSON should be valid. Errors: " + jsonValidation.getErrorMessage());
        assertTrue(jsonValidation.getErrors().isEmpty(), "Valid JSON should have no errors");
        
        // 6. Validate pretty JSON as well
        ZfinReportValidator.ValidationResult prettyJsonValidation = ZfinReportValidator.validateJson(prettyJson);
        assertTrue(prettyJsonValidation.isValid(), 
            "Pretty JSON should be valid. Errors: " + prettyJsonValidation.getErrorMessage());
        
        // 7. Verify JSON contains expected content
        assertTrue(jsonString.contains("UniProt Load Report"), "JSON should contain report title");
        assertTrue(jsonString.contains("2024.1"), "JSON should contain release ID");
        assertTrue(jsonString.contains("supplementalData"), "JSON should contain supplementalData");
        assertTrue(jsonString.contains("actions"), "JSON should contain actions");
        
        // 8. Verify the JSON is valid JSON syntax
        assertTrue(ZfinReportValidator.isValidJson(jsonString), "Generated JSON should have valid syntax");
        
        System.out.println("=== Complete ZFIN Report Integration Test Results ===");
        System.out.println("Report validation: " + reportValidation.isValid());
        System.out.println("JSON validation: " + jsonValidation.isValid());
        System.out.println("JSON length: " + jsonString.length() + " characters");
        System.out.println("Pretty JSON length: " + prettyJson.length() + " characters");
        System.out.println("=== End Integration Test Results ===");
    }

    @Test
    @DisplayName("Minimal valid report should pass validation")
    void testMinimalValidReport() throws Exception {
        // Create minimal report with only required fields
        ZfinReport minimalReport = createMinimalReport();
        
        // Validate the minimal report
        ZfinReportValidator.ValidationResult result = ZfinReportValidator.validate(minimalReport);
        assertTrue(result.isValid(), 
            "Minimal report should be valid. Errors: " + result.getErrorMessage());
        
        // Serialize and validate JSON
        String json = ZfinReportSerializationUtil.toJson(minimalReport);
        assertTrue(ZfinReportValidator.isValid(json), "Minimal report JSON should be valid");
    }

    @Test
    @DisplayName("Report with complex supplemental data should validate correctly")
    void testReportWithComplexSupplementalData() throws Exception {
        ZfinReport report = createReportWithComplexSupplementalData();
        
        // Serialize and validate
        String json = ZfinReportSerializationUtil.toPrettyJson(report);
        ZfinReportValidator.ValidationResult result = ZfinReportValidator.validateJson(json);
        
        assertTrue(result.isValid(), 
            "Report with complex supplemental data should be valid. Errors: " + result.getErrorMessage());
        
        // Verify supplemental data is included in JSON
        assertTrue(json.contains("\"databases\""), "JSON should contain supplemental data");
        assertTrue(json.contains("\"statistics\""), "JSON should contain nested supplemental data");
    }

    /**
     * Creates a complete ZfinReport with all required and optional fields populated.
     * This serves as an example of how to create a comprehensive report.
     */
    private ZfinReport createCompleteReport() {
        // Create metadata with all fields
        LoadReportMeta meta = new LoadReportMeta(
            "UniProt Load Report", 
            "2024.1", 
            System.currentTimeMillis()
        );
        
        // Create summary tables with headers and data
        LoadReportTableHeader countHeader = new LoadReportTableHeader("count", "Count");
        LoadReportTableHeader typeHeader = new LoadReportTableHeader("type", "Data Type");
        LoadReportTableHeader statusHeader = new LoadReportTableHeader("status", "Status");
        
        List<LoadReportTableHeader> headers = Arrays.asList(countHeader, typeHeader, statusHeader);
        
        // Create table rows
        Map<String, Object> row1 = new HashMap<>();
        row1.put("count", 1250);
        row1.put("type", "Proteins");
        row1.put("status", "Updated");
        
        Map<String, Object> row2 = new HashMap<>();
        row2.put("count", 850);
        row2.put("type", "Genes");
        row2.put("status", "Mapped");
        
        Map<String, Object> row3 = new HashMap<>();
        row3.put("count", 45);
        row3.put("type", "Sequences");
        row3.put("status", "Added");
        
        List<Map<String, Object>> rows = Arrays.asList(row1, row2, row3);
        
        LoadReportSummaryTable summaryTable = new LoadReportSummaryTable(
            "Load Statistics", 
            headers, 
            rows
        );
        
        LoadReportSummary summary = new LoadReportSummary(
            "UniProt load completed successfully with 2,145 total records processed",
            Arrays.asList(summaryTable)
        );
        
        // Create supplemental data
        Map<String, Object> supplementalData = new HashMap<>();
        supplementalData.put("loadDuration", "45 minutes");
        supplementalData.put("sourceFile", "uniprot_sprot.dat.gz");
        supplementalData.put("fileSize", "2.3 GB");
        
        Map<String, Object> errorStats = new HashMap<>();
        errorStats.put("parseErrors", 3);
        errorStats.put("validationErrors", 1);
        errorStats.put("skipCount", 12);
        supplementalData.put("errorStatistics", errorStats);
        
        // Create actions with all possible fields
        List<LoadReportAction> actions = new ArrayList<>();
        
        // Action 1: Complete action with all fields
        LoadReportAction action1 = new LoadReportAction();
        action1.setId("1");
        action1.setType("UPDATE");
        action1.setSubType("SEQUENCE");
        action1.setAccession("P12345");
        action1.setGeneZdbID("ZDB-GENE-123456-1");
        action1.setDetails("Updated protein sequence and annotations");
        action1.setLength("567");
        action1.setSupplementalDataKeys(Arrays.asList("sequenceData", "annotationChanges"));
        action1.setUniprotAccessions(Arrays.asList("P12345", "Q67890"));
        action1.setRelatedEntityID("ZDB-PROTEIN-123456-1");
        action1.setDbName("UniProt");
        action1.setMd5("a1b2c3d4e5f6789012345678901234567890abcd");
        
        Map<String, Object> relatedFields = new HashMap<>();
        relatedFields.put("previousLength", 555);
        relatedFields.put("confidence", "high");
        action1.setRelatedEntityFields(relatedFields);
        
        action1.setRelatedActionsKeys(Arrays.asList("relatedUpdate1", "relatedUpdate2"));
        
        LoadReportActionLink link1 = new LoadReportActionLink("UniProt Entry", "https://www.uniprot.org/uniprot/P12345");
        LoadReportActionLink link2 = new LoadReportActionLink("ZFIN Gene", "https://zfin.org/ZDB-GENE-123456-1");
        action1.setLinks(Arrays.asList(link1, link2));
        
        LoadReportActionTag tag1 = new LoadReportActionTag("priority", "high");
        LoadReportActionTag tag2 = new LoadReportActionTag("source", "automated");
        action1.setTags(Arrays.asList(tag1, tag2));
        
        actions.add(action1);
        
        // Action 2: Minimal action with only required fields
        LoadReportAction action2 = new LoadReportAction();
        action2.setId("2");
        action2.setType("INSERT");
        action2.setSubType("MAPPING");
        action2.setAccession("Q98765");
        action2.setGeneZdbID("ZDB-GENE-789012-2");
        action2.setDetails("New gene mapping created");
        action2.setLength("423");
        action2.setSupplementalDataKeys(Arrays.asList("mappingData"));
        
        actions.add(action2);
        
        // Action 3: Action with integer ID
        LoadReportAction action3 = new LoadReportAction();
        action3.setId(3); // Using integer ID
        action3.setType("DELETE");
        action3.setSubType("OBSOLETE");
        action3.setAccession("O11111");
        action3.setGeneZdbID("ZDB-GENE-111111-3");
        action3.setDetails("Removed obsolete entry");
        action3.setLength("0");
        action3.setSupplementalDataKeys(Arrays.asList("deletionReason"));
        
        actions.add(action3);
        
        // Assemble the complete report
        ZfinReport report = new ZfinReport();
        report.setMeta(meta);
        report.setSummary(summary);
        report.setSupplementalData(supplementalData);
        report.setActions(actions);
        
        return report;
    }
    
    /**
     * Creates a minimal valid report with only required fields.
     */
    private ZfinReport createMinimalReport() {
        LoadReportMeta meta = new LoadReportMeta("Minimal Report", System.currentTimeMillis());
        
        Map<String, Object> row = new HashMap<>();
        row.put("count", 1);
        
        LoadReportSummaryTable table = new LoadReportSummaryTable(
            null, // Optional description
            null, // Optional headers  
            Arrays.asList(row)
        );
        
        LoadReportSummary summary = new LoadReportSummary(
            "Minimal load summary",
            Arrays.asList(table)
        );
        
        LoadReportAction action = new LoadReportAction();
        action.setId("1");
        action.setType("UPDATE");
        action.setSubType("SEQUENCE");
        action.setAccession("P00001");
        action.setGeneZdbID("ZDB-GENE-000001-1");
        action.setDetails("Minimal action");
        action.setLength("100");
        action.setSupplementalDataKeys(Arrays.asList("minimal"));
        
        ZfinReport report = new ZfinReport();
        report.setMeta(meta);
        report.setSummary(summary);
        report.setSupplementalData(new HashMap<>());
        report.setActions(Arrays.asList(action));
        
        return report;
    }
    
    /**
     * Creates a report with complex nested supplemental data to test serialization.
     */
    private ZfinReport createReportWithComplexSupplementalData() {
        ZfinReport report = createMinimalReport();
        
        // Add complex nested supplemental data
        Map<String, Object> supplementalData = new HashMap<>();
        
        Map<String, Object> databases = new HashMap<>();
        databases.put("uniprot", "2024.1");
        databases.put("ensembl", "109");
        databases.put("ncbi", "2024.01");
        supplementalData.put("databases", databases);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalRecords", 5000);
        statistics.put("processedRecords", 4850);
        statistics.put("skippedRecords", 150);
        
        Map<String, Object> timing = new HashMap<>();
        timing.put("startTime", "2024-01-01T10:00:00Z");
        timing.put("endTime", "2024-01-01T10:45:00Z");
        timing.put("duration", "45 minutes");
        statistics.put("timing", timing);
        
        supplementalData.put("statistics", statistics);
        
        List<String> warnings = Arrays.asList(
            "3 records had missing gene mappings",
            "1 record had invalid sequence length",
            "2 records were duplicates"
        );
        supplementalData.put("warnings", warnings);
        
        report.setSupplementalData(supplementalData);
        
        return report;
    }
}