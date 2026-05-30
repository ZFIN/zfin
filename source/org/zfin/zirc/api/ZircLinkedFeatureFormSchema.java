package org.zfin.zirc.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zfin.zirc.entity.LinkedFeature;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Field-path PATCH dispatch for a single {@link LinkedFeature} row.
 *
 * <p>Unlike the other aggregates, LinkedFeature has a composite PK
 * {@code (submission, mutationA, mutationB)}, so it doesn't get its
 * own {@code /form-schema} endpoint or its own React page — the edit
 * UX is a row of inline inputs inside the parent submission form's
 * Linked Features section. This class only exists to host the
 * {@code FIELDS} map for the per-field PATCH endpoint
 * {@code PATCH /api/zirc/line-submissions/{zdbID}/linked-features/{aId}/{bId}}.
 *
 * <p>Distance is exposed as the two underlying columns
 * ({@code /distanceCentimorgans} and {@code /distanceMegabases}); the
 * React renderer combines them into a single (value, unit) widget but
 * the wire shape stays flat.
 */
public final class ZircLinkedFeatureFormSchema {

    private ZircLinkedFeatureFormSchema() {}

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Read + write for one form-schema path on a LinkedFeature. */
    public record FieldDescriptor(
            Function<LinkedFeature, JsonNode> read,
            BiConsumer<LinkedFeature, JsonNode> write) {
    }

    public static final Map<String, FieldDescriptor> FIELDS = Map.ofEntries(
            field("/distanceKnown",
                    LinkedFeature::getDistanceKnown,
                    (lf, v) -> lf.setDistanceKnown(boolNullable(v))),
            field("/distanceCentimorgans",
                    LinkedFeature::getDistanceCentimorgans,
                    (lf, v) -> lf.setDistanceCentimorgans(doubleNullable(v))),
            field("/distanceMegabases",
                    LinkedFeature::getDistanceMegabases,
                    (lf, v) -> lf.setDistanceMegabases(doubleNullable(v))),
            field("/additionalInfo",
                    LinkedFeature::getAdditionalInfo,
                    (lf, v) -> lf.setAdditionalInfo(text(v)))
    );

    // ─── descriptor builders ────────────────────────────────────────────────

    private static Map.Entry<String, FieldDescriptor> field(
            String path,
            Function<LinkedFeature, ?> getter,
            BiConsumer<LinkedFeature, JsonNode> setter) {
        return Map.entry(path, new FieldDescriptor(
                lf -> MAPPER.valueToTree(getter.apply(lf)),
                setter));
    }

    // ─── value coercers ────────────────────────────────────────────────────

    private static String text(JsonNode v) {
        if (v == null || v.isNull()) {return null;}
        String s = v.asText();
        return s.isBlank() ? null : s.trim();
    }

    private static Boolean boolNullable(JsonNode v) {
        if (v == null || v.isNull()) {return null;}
        return v.asBoolean();
    }

    private static Double doubleNullable(JsonNode v) {
        if (v == null || v.isNull()) {return null;}
        return v.isNumber() ? v.asDouble() : null;
    }
}
