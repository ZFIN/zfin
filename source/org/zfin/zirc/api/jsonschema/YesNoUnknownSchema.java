package org.zfin.zirc.api.jsonschema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.zfin.zirc.entity.YesNoUnknown;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@code {type: ["boolean", "string", "null"], enum: [true, false, "unknown", null], title?}}
 * — a yes/no question that also accepts "unknown" (ZFIN-10617). See
 * {@link YesNoUnknown} for the wire values.
 *
 * <p>The {@code enum} is what the yesNoRadio renderer reads to decide whether
 * to offer the "Unknown" radio, so the schema is the one place a field opts in.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record YesNoUnknownSchema(String title) implements JsonSchema {

    public static YesNoUnknownSchema of(String title) {
        return new YesNoUnknownSchema(title);
    }

    @JsonProperty("type")
    public List<String> type() {
        return List.of("boolean", "string", "null");
    }

    /** Every answer's wire value, then {@code null} for unanswered. */
    @JsonProperty("enum")
    public List<Object> enumValues() {
        List<Object> values = new ArrayList<>(
                Arrays.stream(YesNoUnknown.values()).map(YesNoUnknown::wireValue).toList());
        values.add(null);  // List.of / toList() copies reject or hide null
        return values;
    }
}
