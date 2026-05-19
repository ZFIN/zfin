package org.zfin.zirc.api.jsonschema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code {type: "object", title?, properties: {...}}}. The
 * {@code properties} map should be a {@link LinkedHashMap} so insertion
 * order is preserved on the wire (production order, not the alphabetized
 * snapshot-test order).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ObjectSchema(
        String title,
        Map<String, JsonSchema> properties
) implements JsonSchema {

    /** Factory: no title, fresh ordered properties map ready to add to. */
    public static ObjectSchema of() {
        return new ObjectSchema(null, new LinkedHashMap<>());
    }

    /** Factory: just a properties map. */
    public static ObjectSchema of(Map<String, JsonSchema> properties) {
        return new ObjectSchema(null, properties);
    }

    /** Factory: title + properties. */
    public static ObjectSchema of(String title, Map<String, JsonSchema> properties) {
        return new ObjectSchema(title, properties);
    }

    @JsonProperty("type")
    public String type() {
        return "object";
    }
}
