package org.zfin.zirc.api.jsonschema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@code {type: "array", items, title?, maxItems?, uniqueItems?}}.
 * Used for the server-managed child lists (mutations/assays/attachments),
 * for the publications stringList per mutation, and for the reasons
 * chip-list with its {@code uniqueItems: true} guard.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ArraySchema(
        String title,
        JsonSchema items,
        Integer maxItems,
        Boolean uniqueItems
) implements JsonSchema {

    /** Factory: just items, no title or caps. */
    public static ArraySchema of(JsonSchema items) {
        return new ArraySchema(null, items, null, null);
    }

    @JsonProperty("type")
    public String type() {
        return "array";
    }
}
