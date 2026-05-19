package org.zfin.zirc.api.jsonschema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@code {const: <value>, title?}}. The non-obvious one: no {@code type}
 * field. JSON Schema's {@code const} keyword pins the schema to a
 * single value; emitting {@code type} would be redundant and our
 * current snapshot doesn't include it on the canonical-reasons items.
 *
 * <p>{@code value} is {@code Object} because {@code const} can hold any
 * JSON-serializable scalar — string in the reasons case, but could be
 * boolean or number elsewhere. {@code @JsonProperty("const")} renames
 * it on the wire since {@code const} is a Java keyword.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConstSchema(
        @JsonProperty("const") Object value,
        String title
) implements JsonSchema {
}
