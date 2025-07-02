package org.zfin.datatransfer.report.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Utility class for serializing and deserializing ZFIN reports to/from JSON.
 * Provides a configured ObjectMapper specifically for ZFIN report serialization.
 */
public class ZfinReportSerializationUtil {

    private static final ObjectMapper objectMapper = createObjectMapper();

    /**
     * Creates and configures an ObjectMapper for ZFIN report serialization.
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Configure serialization settings
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // Omit null values
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true); // Write dates as timestamps
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        return mapper;
    }

    /**
     * Returns the configured ObjectMapper instance.
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Serializes an object to JSON string.
     * 
     * @param object the object to serialize
     * @return JSON string representation
     * @throws JsonProcessingException if serialization fails
     */
    public static String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * Serializes an object to pretty-printed JSON string.
     * 
     * @param object the object to serialize
     * @return pretty-printed JSON string representation
     * @throws JsonProcessingException if serialization fails
     */
    public static String toPrettyJson(Object object) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    /**
     * Deserializes a JSON string to an object of the specified type.
     * 
     * @param json the JSON string to deserialize
     * @param clazz the target class type
     * @param <T> the type parameter
     * @return the deserialized object
     * @throws JsonProcessingException if deserialization fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }
}