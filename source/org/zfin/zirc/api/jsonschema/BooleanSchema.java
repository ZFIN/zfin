package org.zfin.zirc.api.jsonschema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * {@code {type: "boolean", title?}}, optionally nullable. Every existing
 * boolean field in the ZIRC forms is nullable (the curator may not have
 * answered yet), so this carries {@code nullable = true} in the common
 * case.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BooleanSchema(
        String title,
        @JsonIgnore Boolean isNullable
) implements JsonSchema {

    /** Factory for the everyday nullable-boolean pattern. */
    public static BooleanSchema nullable(String title) {
        return new BooleanSchema(title, Boolean.TRUE);
    }

    @JsonProperty("type")
    public Object type() {
        return Boolean.TRUE.equals(isNullable) ? List.of("boolean", "null") : "boolean";
    }
}
