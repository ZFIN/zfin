package org.zfin.zirc.api.jsonschema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * {@code {type: "number", title?}}, optionally nullable. JSON Schema's
 * {@code number} covers both integers and floats; the existing schema
 * uses it for ids and sort orders.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NumberSchema(
        String title,
        @JsonIgnore Boolean isNullable
) implements JsonSchema {

    public static NumberSchema of() {
        return new NumberSchema(null, null);
    }

    @JsonProperty("type")
    public Object type() {
        return Boolean.TRUE.equals(isNullable) ? List.of("number", "null") : "number";
    }
}
