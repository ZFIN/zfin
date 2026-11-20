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
        @JsonIgnore Boolean isNullable,
        @JsonIgnore boolean allowsUnknown
) implements JsonSchema {

    public BooleanSchema(String title, Boolean isNullable) {
        this(title, isNullable, false);
    }

    /** Nullable boolean that may also hold the string "unknown" (yesNoRadio allowUnknown). */
    public static BooleanSchema nullableOrUnknown(String title) {
        return new BooleanSchema(title, Boolean.TRUE, true);
    }

    @JsonProperty("enum")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Object> enumValues() {
        if (!allowsUnknown) {return null;}
        List<Object> v = new java.util.ArrayList<>();
        v.add(Boolean.TRUE); v.add(Boolean.FALSE); v.add("unknown"); v.add(null);
        return v;
    }

    /** Factory for the everyday nullable-boolean pattern. */
    public static BooleanSchema nullable(String title) {
        return new BooleanSchema(title, Boolean.TRUE);
    }

    @JsonProperty("type")
    public Object type() {
        if (allowsUnknown) {return List.of("boolean", "string", "null");}
        return Boolean.TRUE.equals(isNullable) ? List.of("boolean", "null") : "boolean";
    }
}
