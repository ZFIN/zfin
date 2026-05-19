package org.zfin.zirc.api.jsonschema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * {@code {type: "string", title?, maxLength?, oneOf?}}.
 *
 * <p>When {@code nullable} is true, the {@code type} property serializes
 * as {@code ["string", "null"]} (JSON Schema 2020-12 nullable form) —
 * matches what {@code ZircFormSchema}'s mutation-summary fields emit
 * today.
 *
 * <p>The {@code oneOf} slot holds {@link ConstSchema} entries when the
 * string is constrained to a labelled enum, as the canonical-reasons
 * list does on the submission form.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StringSchema(
        String title,
        Integer maxLength,
        List<ConstSchema> oneOf,
        @JsonIgnore Boolean isNullable
) implements JsonSchema {

    /** Factory for the common case: just a title + maxLength. */
    public static StringSchema of(String title, int maxLength) {
        return new StringSchema(title, maxLength, null, null);
    }

    /** Factory for nullable strings (e.g. mutation-summary item fields). */
    public static StringSchema nullable() {
        return new StringSchema(null, null, null, Boolean.TRUE);
    }

    /** Factory for the canonical-reasons-list pattern (string with oneOf). */
    public static StringSchema withOneOf(List<ConstSchema> oneOf) {
        return new StringSchema(null, null, oneOf, null);
    }

    @JsonProperty("type")
    public Object type() {
        return Boolean.TRUE.equals(isNullable) ? List.of("string", "null") : "string";
    }
}
