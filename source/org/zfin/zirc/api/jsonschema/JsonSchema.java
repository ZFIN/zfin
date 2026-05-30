package org.zfin.zirc.api.jsonschema;

/**
 * Sealed root of the JSON Schema record tree. Each implementation
 * carries its own {@code type} accessor — JSON Schema's {@code type}
 * is a regular schema keyword, not a polymorphic discriminator, so
 * there's no {@code @JsonTypeInfo}.
 *
 * <p>Concrete shapes:
 * <ul>
 *   <li>{@link StringSchema}  — {@code {type: "string", maxLength?, title?, oneOf?}}, optionally nullable</li>
 *   <li>{@link BooleanSchema} — {@code {type: "boolean", title?}}, optionally nullable</li>
 *   <li>{@link NumberSchema}  — {@code {type: "number", title?}}, optionally nullable</li>
 *   <li>{@link ArraySchema}   — {@code {type: "array", items, title?, maxItems?, uniqueItems?}}</li>
 *   <li>{@link ObjectSchema}  — {@code {type: "object", title?, properties}}</li>
 *   <li>{@link ConstSchema}   — {@code {const: <value>, title?}}, no {@code type} field (used inside {@code oneOf})</li>
 * </ul>
 *
 * <p>Nullable strings and booleans emit {@code "type": ["string", "null"]}
 * etc. via a computed accessor that returns either a String or a List
 * depending on the nullable flag. The flag itself is {@code @JsonIgnore}d
 * so it doesn't leak into the wire shape.
 *
 * <p>No polymorphic deserialization — the React client is the only
 * consumer; we never read schema back from JSON in Java.
 */
public sealed interface JsonSchema
        permits StringSchema, BooleanSchema, NumberSchema,
                ArraySchema, ObjectSchema, ConstSchema {
}
