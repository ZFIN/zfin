package org.zfin.zirc.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;

/**
 * Single field change against the form schema. The {@code path} is a
 * JSON-Pointer-shaped key from {@code ZircFormSchema.FIELD_HANDLERS};
 * the server rejects unknown paths and dispatches to the registered
 * setter for known ones. {@code value} is a raw JsonNode so the same
 * endpoint handles strings, booleans, arrays, and null uniformly.
 */
public record FieldUpdate(
        @NotBlank String path,
        JsonNode value) {
}
