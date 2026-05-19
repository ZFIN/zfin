package org.zfin.zirc.dto;

import org.zfin.zirc.api.jsonschema.JsonSchema;
import org.zfin.zirc.api.uischema.UiSchemaElement;

/**
 * Wire DTO for the three {@code /form-schema} endpoints. Both sides are
 * now typed:
 *
 * <ul>
 *   <li>{@code schema}   — {@link JsonSchema} tree under
 *       {@code org.zfin.zirc.api.jsonschema}</li>
 *   <li>{@code uiSchema} — {@link UiSchemaElement} tree under
 *       {@code org.zfin.zirc.api.uischema}</li>
 * </ul>
 *
 * <p>Jackson serializes both as JSON Forms-shaped JSON via each record's
 * {@code @JsonProperty("type")} accessor; the client reads the regular
 * keys by name.
 */
public record FormSchemaDTO(
        JsonSchema schema,
        UiSchemaElement uiSchema) {
}
