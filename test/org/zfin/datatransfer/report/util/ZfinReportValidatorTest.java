package org.zfin.datatransfer.report.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.zfin.datatransfer.report.model.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ZfinReportValidator.
 * 
 * Tests validation functionality against the zfin-report-schema.json schema,
 * including positive and negative test cases for all major schema requirements.
 */
public class ZfinReportValidatorTest {

    private ZfinReport validReport;
    private LoadReportMeta validMeta;
    private LoadReportSummary validSummary;
    private List<LoadReportAction> validActions;

    @BeforeEach
    void setUp() {
        // Create valid metadata
        validMeta = new LoadReportMeta("Test Report", "2024.1", System.currentTimeMillis());
        
        // Create valid summary with table
        LoadReportTableHeader header1 = new LoadReportTableHeader("count", "Count");
        LoadReportTableHeader header2 = new LoadReportTableHeader("type", "Type");
        List<LoadReportTableHeader> headers = Arrays.asList(header1, header2);
        
        Map<String, Object> row1 = new HashMap<>();
        row1.put("count", 100);
        row1.put("type", "proteins");
        
        Map<String, Object> row2 = new HashMap<>();
        row2.put("count", 50);
        row2.put("type", "genes");
        
        LoadReportSummaryTable table = new LoadReportSummaryTable("Statistics", headers, Arrays.asList(row1, row2));
        validSummary = new LoadReportSummary("Load completed successfully", Arrays.asList(table));
        
        // Create valid actions
        LoadReportAction action = new LoadReportAction();
        action.setId("1");
        action.setType("UPDATE");
        action.setSubType("SEQUENCE");
        action.setAccession("P12345");
        action.setGeneZdbID("ZDB-GENE-123456-1");
        action.setDetails("Updated protein sequence");
        action.setLength("250");
        action.setSupplementalDataKeys(Arrays.asList("key1", "key2"));
        
        validActions = Arrays.asList(action);
        
        // Create complete valid report
        validReport = new ZfinReport();
        validReport.setMeta(validMeta);
        validReport.setSummary(validSummary);
        validReport.setSupplementalData(new HashMap<>());
        validReport.setActions(validActions);
    }

    @Test
    @DisplayName("Valid complete report should pass validation")
    void testValidReportPassesValidation() {
        ZfinReportValidator.ValidationResult result = ZfinReportValidator.validate(validReport);
        
        assertTrue(result.isValid(), "Valid report should pass validation");
        assertTrue(result.getErrors().isEmpty(), "Valid report should have no errors");
        assertEquals("Validation passed", result.getErrorMessage());
        
        // Test convenience method
        assertTrue(ZfinReportValidator.isValid(validReport));
    }

    @Test
    @DisplayName("Valid JSON string should pass validation")
    void testValidJsonStringPassesValidation() throws Exception {
        String jsonString = ZfinReportSerializationUtil.toJson(validReport);
        
        ZfinReportValidator.ValidationResult result = ZfinReportValidator.validateJson(jsonString);
        
        assertTrue(result.isValid(), "Valid JSON should pass validation");
        assertTrue(result.getErrors().isEmpty(), "Valid JSON should have no errors");
        
        // Test convenience method
        assertTrue(ZfinReportValidator.isValid(jsonString));
    }

    @Test
    @DisplayName("Null report should fail validation")
    void testNullReportFailsValidation() {
        ZfinReportValidator.ValidationResult result = ZfinReportValidator.validate(null);
        
        assertFalse(result.isValid(), "Null report should fail validation");
        assertFalse(result.getErrors().isEmpty(), "Null report should have errors");
        assertTrue(result.getErrorMessage().contains("cannot be null"));
        
        // Test convenience method
        assertFalse(ZfinReportValidator.isValid((ZfinReport) null));
    }

    @Test
    @DisplayName("Null JSON string should fail validation")
    void testNullJsonStringFailsValidation() {
        ZfinReportValidator.ValidationResult result = ZfinReportValidator.validateJson(null);
        
        assertFalse(result.isValid(), "Null JSON should fail validation");
        assertFalse(result.getErrors().isEmpty(), "Null JSON should have errors");
        assertTrue(result.getErrorMessage().contains("cannot be null"));
        
        // Test convenience method
        assertFalse(ZfinReportValidator.isValid((String) null));
    }

    @Test
    @DisplayName("Empty JSON string should fail validation")
    void testEmptyJsonStringFailsValidation() {
        ZfinReportValidator.ValidationResult result = ZfinReportValidator.validateJson("");
        
        assertFalse(result.isValid(), "Empty JSON should fail validation");
        assertFalse(result.getErrors().isEmpty(), "Empty JSON should have errors");
        assertTrue(result.getErrorMessage().contains("cannot be null or empty"));
    }

    @Test
    @DisplayName("Invalid JSON syntax should fail validation")
    void testInvalidJsonSyntaxFailsValidation() {
        String invalidJson = "{ invalid json syntax }";
        
        ZfinReportValidator.ValidationResult result = ZfinReportValidator.validateJson(invalidJson);
        
        assertFalse(result.isValid(), "Invalid JSON syntax should fail validation");
        assertFalse(result.getErrors().isEmpty(), "Invalid JSON should have errors");
        assertTrue(result.getErrorMessage().contains("JSON parsing error"));
    }

    @Test
    @DisplayName("Report missing required fields should fail validation")
    void testReportMissingRequiredFieldsFailsValidation() {
        // Test missing meta
        ZfinReport reportWithoutMeta = new ZfinReport();
        reportWithoutMeta.setSummary(validSummary);
        reportWithoutMeta.setSupplementalData(new HashMap<>());
        reportWithoutMeta.setActions(validActions);
        
        ZfinReportValidator.ValidationResult result = ZfinReportValidator.validate(reportWithoutMeta);
        assertFalse(result.isValid(), "Report missing meta should fail validation");
        
        // Test missing summary
        ZfinReport reportWithoutSummary = new ZfinReport();
        reportWithoutSummary.setMeta(validMeta);
        reportWithoutSummary.setSupplementalData(new HashMap<>());
        reportWithoutSummary.setActions(validActions);
        
        result = ZfinReportValidator.validate(reportWithoutSummary);
        assertFalse(result.isValid(), "Report missing summary should fail validation");
        
        // Test missing supplementalData
        ZfinReport reportWithoutSupplementalData = new ZfinReport();
        reportWithoutSupplementalData.setMeta(validMeta);
        reportWithoutSupplementalData.setSummary(validSummary);
        reportWithoutSupplementalData.setActions(validActions);
        
        result = ZfinReportValidator.validate(reportWithoutSupplementalData);
        assertFalse(result.isValid(), "Report missing supplementalData should fail validation");
        
        // Test missing actions
        ZfinReport reportWithoutActions = new ZfinReport();
        reportWithoutActions.setMeta(validMeta);
        reportWithoutActions.setSummary(validSummary);
        reportWithoutActions.setSupplementalData(new HashMap<>());
        
        result = ZfinReportValidator.validate(reportWithoutActions);
        assertFalse(result.isValid(), "Report missing actions should fail validation");
    }

    @Test
    @DisplayName("Meta with missing required fields should fail validation")
    void testMetaMissingRequiredFieldsFailsValidation() {
        // Test missing title
        LoadReportMeta metaWithoutTitle = new LoadReportMeta(null, "2024.1", System.currentTimeMillis());
        validReport.setMeta(metaWithoutTitle);
        
        ZfinReportValidator.ValidationResult result = ZfinReportValidator.validate(validReport);
        assertFalse(result.isValid(), "Meta missing title should fail validation");
        
        // Test missing creationDate
        LoadReportMeta metaWithoutDate = new LoadReportMeta("Test Report", "2024.1", null);
        validReport.setMeta(metaWithoutDate);
        
        result = ZfinReportValidator.validate(validReport);
        assertFalse(result.isValid(), "Meta missing creationDate should fail validation");
    }

    @Test
    @DisplayName("Summary with missing required fields should fail validation")
    void testSummaryMissingRequiredFieldsFailsValidation() {
        // Test missing description
        LoadReportSummary summaryWithoutDescription = new LoadReportSummary(null, validSummary.getTables());
        validReport.setSummary(summaryWithoutDescription);
        
        ZfinReportValidator.ValidationResult result = ZfinReportValidator.validate(validReport);
        assertFalse(result.isValid(), "Summary missing description should fail validation");
        
        // Test missing tables
        LoadReportSummary summaryWithoutTables = new LoadReportSummary("Description", null);
        validReport.setSummary(summaryWithoutTables);
        
        result = ZfinReportValidator.validate(validReport);
        assertFalse(result.isValid(), "Summary missing tables should fail validation");
    }

    @Test
    @DisplayName("Action with missing required fields should fail validation")
    void testActionMissingRequiredFieldsFailsValidation() {
        LoadReportAction incompleteAction = new LoadReportAction();
        incompleteAction.setId("1");
        // Missing other required fields
        
        validReport.setActions(Arrays.asList(incompleteAction));
        
        ZfinReportValidator.ValidationResult result = ZfinReportValidator.validate(validReport);
        assertFalse(result.isValid(), "Action missing required fields should fail validation");
        assertFalse(result.getErrors().isEmpty(), "Should have validation errors");
    }

    @Test
    @DisplayName("Action with invalid type should fail validation")
    void testActionWithInvalidTypeFailsValidation() {
        LoadReportAction actionWithInvalidType = new LoadReportAction();
        actionWithInvalidType.setId("1");
        actionWithInvalidType.setType("INVALID_TYPE"); // Not in schema enum
        actionWithInvalidType.setSubType("SEQUENCE");
        actionWithInvalidType.setAccession("P12345");
        actionWithInvalidType.setGeneZdbID("ZDB-GENE-123456-1");
        actionWithInvalidType.setDetails("Test");
        actionWithInvalidType.setLength("100");
        actionWithInvalidType.setSupplementalDataKeys(Arrays.asList("key1"));
        
        validReport.setActions(Arrays.asList(actionWithInvalidType));
        
        ZfinReportValidator.ValidationResult result = ZfinReportValidator.validate(validReport);
        assertFalse(result.isValid(), "Action with invalid type should fail validation");
    }

    @Test
    @DisplayName("Validation result toString should provide useful information")
    void testValidationResultToString() {
        ZfinReportValidator.ValidationResult validResult = ZfinReportValidator.validate(validReport);
        String validString = validResult.toString();
        assertTrue(validString.contains("valid=true"), "Valid result should show valid=true");
        
        ZfinReportValidator.ValidationResult invalidResult = ZfinReportValidator.validate(null);
        String invalidString = invalidResult.toString();
        assertTrue(invalidString.contains("valid=false"), "Invalid result should show valid=false");
        assertTrue(invalidString.contains("errors="), "Invalid result should show errors");
    }

    @Test
    @DisplayName("isValidJson should correctly identify valid and invalid JSON")
    void testIsValidJson() {
        String validJson = "{\"key\": \"value\"}";
        assertTrue(ZfinReportValidator.isValidJson(validJson), "Valid JSON should be identified as valid");
        
        String invalidJson = "{invalid json}";
        assertFalse(ZfinReportValidator.isValidJson(invalidJson), "Invalid JSON should be identified as invalid");
        
        assertFalse(ZfinReportValidator.isValidJson(null), "Null should be identified as invalid JSON");
        assertFalse(ZfinReportValidator.isValidJson(""), "Empty string should be identified as invalid JSON");
    }

    @Test
    @DisplayName("Schema should be loaded and accessible")
    void testSchemaLoading() {
        // This will trigger schema loading
        ZfinReportValidator.ValidationResult result = ZfinReportValidator.validate(validReport);
        assertTrue(result.isValid());
        
        // Schema should now be loaded
        assertNotNull(ZfinReportValidator.getSchema(), "Schema should be loaded");
    }

    @Test
    @DisplayName("Multiple validation calls should reuse loaded schema")
    void testSchemaReuse() {
        // First validation loads schema
        ZfinReportValidator.ValidationResult result1 = ZfinReportValidator.validate(validReport);
        assertTrue(result1.isValid());
        
        // Second validation should reuse schema
        ZfinReportValidator.ValidationResult result2 = ZfinReportValidator.validate(validReport);
        assertTrue(result2.isValid());
        
        // Both should use the same schema instance
        assertNotNull(ZfinReportValidator.getSchema());
    }
}