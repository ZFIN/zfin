package org.zfin.datatransfer.report.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.zfin.datatransfer.report.model.ZfinReport;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for validating ZFIN reports against the JSON schema.
 * 
 * This class provides methods to validate both ZfinReport objects and raw JSON strings
 * against the zfin-report-schema.json schema file.
 * 
 * Example usage:
 * <pre>
 * ZfinReport report = new ZfinReport();
 * // ... populate report
 * 
 * ValidationResult result = ZfinReportValidator.validate(report);
 * if (result.isValid()) {
 *     System.out.println("Report is valid!");
 * } else {
 *     System.out.println("Validation errors: " + result.getErrors());
 * }
 * </pre>
 * 
 * @author ZFIN Team
 */
public class ZfinReportValidator {
    
    private static final String SCHEMA_RESOURCE_PATH = "/WEB-INF/classes/zfin/report/zfin-report-schema.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static Schema schema;
    
    /**
     * Represents the result of a validation operation.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        }
        
        /**
         * @return true if the validation passed, false otherwise
         */
        public boolean isValid() {
            return valid;
        }
        
        /**
         * @return list of validation error messages, empty if validation passed
         */
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        /**
         * @return formatted error message with all validation errors
         */
        public String getErrorMessage() {
            if (valid) {
                return "Validation passed";
            }
            return String.join("; ", errors);
        }
        
        @Override
        public String toString() {
            return "ValidationResult{valid=" + valid + ", errors=" + errors + "}";
        }
    }
    
    /**
     * Initialize the schema loader. This is called lazily on first validation.
     */
    private static synchronized void initializeSchema() {
        if (schema != null) {
            return;
        }
        
        try (InputStream schemaStream = ZfinReportValidator.class.getResourceAsStream(SCHEMA_RESOURCE_PATH)) {
            if (schemaStream == null) {
                throw new RuntimeException("Could not find schema file at: " + SCHEMA_RESOURCE_PATH);
            }
            
            JSONObject rawSchema = new JSONObject(new JSONTokener(schemaStream));
            schema = SchemaLoader.load(rawSchema);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load schema file: " + SCHEMA_RESOURCE_PATH, e);
        }
    }
    
    /**
     * Validates a ZfinReport object against the JSON schema.
     * 
     * @param report the ZfinReport to validate
     * @return ValidationResult containing the validation result and any errors
     */
    public static ValidationResult validate(ZfinReport report) {
        if (report == null) {
            return new ValidationResult(false, List.of("Report cannot be null"));
        }
        
        try {
            String jsonString = ZfinReportSerializationUtil.toJson(report);
            return validateJson(jsonString);
        } catch (Exception e) {
            return new ValidationResult(false, List.of("Failed to serialize report to JSON: " + e.getMessage()));
        }
    }
    
    /**
     * Validates a JSON string against the JSON schema.
     * 
     * @param jsonString the JSON string to validate
     * @return ValidationResult containing the validation result and any errors
     */
    public static ValidationResult validateJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return new ValidationResult(false, List.of("JSON string cannot be null or empty"));
        }
        
        try {
            initializeSchema();
            
            // Parse JSON string to JSONObject for validation
            JSONObject jsonObject = new JSONObject(jsonString);
            
            // Perform validation
            schema.validate(jsonObject);
            
            return new ValidationResult(true, null);
            
        } catch (ValidationException e) {
            List<String> errors = new ArrayList<>();
            collectValidationErrors(e, errors);
            return new ValidationResult(false, errors);
        } catch (Exception e) {
            return new ValidationResult(false, List.of("JSON parsing error: " + e.getMessage()));
        }
    }
    
    /**
     * Validates a JSON string and returns true if valid, false otherwise.
     * This is a convenience method for simple boolean validation checks.
     * 
     * @param jsonString the JSON string to validate
     * @return true if the JSON is valid against the schema, false otherwise
     */
    public static boolean isValid(String jsonString) {
        return validateJson(jsonString).isValid();
    }
    
    /**
     * Validates a ZfinReport object and returns true if valid, false otherwise.
     * This is a convenience method for simple boolean validation checks.
     * 
     * @param report the ZfinReport to validate
     * @return true if the report is valid against the schema, false otherwise
     */
    public static boolean isValid(ZfinReport report) {
        return validate(report).isValid();
    }
    
    /**
     * Recursively collects all validation errors from a ValidationException.
     * This handles nested validation errors that can occur with complex schemas.
     * 
     * @param e the ValidationException to extract errors from
     * @param errors the list to collect errors into
     */
    private static void collectValidationErrors(ValidationException e, List<String> errors) {
        // Add the main error message
        String message = e.getMessage();
        if (message != null && !message.trim().isEmpty()) {
            errors.add(message);
        }
        
        // Recursively collect errors from nested ValidationExceptions
        for (ValidationException cause : e.getCausingExceptions()) {
            collectValidationErrors(cause, errors);
        }
    }
    
    /**
     * Validates that a JSON string is parseable and represents a valid JSON structure.
     * This does NOT validate against the schema, just JSON syntax.
     * 
     * @param jsonString the JSON string to check
     * @return true if the string is valid JSON, false otherwise
     */
    public static boolean isValidJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }
        
        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Gets the loaded schema object. Primarily for testing purposes.
     * 
     * @return the loaded JSON Schema object
     */
    protected static Schema getSchema() {
        initializeSchema();
        return schema;
    }
    
    /**
     * Resets the schema cache. This is primarily for testing purposes
     * to allow testing different schema loading scenarios.
     */
    protected static void resetSchema() {
        schema = null;
    }
}