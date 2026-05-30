package org.zfin.zirc.api.jsonschema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code {type: "object", title?, properties: {...}, required?: [...]}}.
 * The {@code properties} map should be a {@link LinkedHashMap} so insertion
 * order is preserved on the wire (production order, not the alphabetized
 * snapshot-test order).
 *
 * <p>{@code required} carries the JSON Schema 2020-12 required-property
 * list and is the single source of truth for "this leaf is required"
 * across the stack: server-side status computers read it; client renderers
 * can also derive "MISSING" from it without a separate side-channel.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ObjectSchema(
        String title,
        Map<String, JsonSchema> properties,
        List<String> required
) implements JsonSchema {

    /** Factory: no title, fresh ordered properties map ready to add to. */
    public static ObjectSchema of() {
        return new ObjectSchema(null, new LinkedHashMap<>(), null);
    }

    /** Factory: just a properties map. */
    public static ObjectSchema of(Map<String, JsonSchema> properties) {
        return new ObjectSchema(null, properties, null);
    }

    /** Factory: title + properties. */
    public static ObjectSchema of(String title, Map<String, JsonSchema> properties) {
        return new ObjectSchema(title, properties, null);
    }

    /** Factory: title + properties + required property names. */
    public static ObjectSchema of(String title,
                                  Map<String, JsonSchema> properties,
                                  List<String> required) {
        return new ObjectSchema(title, properties, required);
    }

    @JsonProperty("type")
    public String type() {
        return "object";
    }
}
