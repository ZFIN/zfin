package org.zfin.zirc.service;

import org.zfin.zirc.api.ZircLesionFormSchema;
import org.zfin.zirc.api.jsonschema.JsonSchema;
import org.zfin.zirc.api.jsonschema.ObjectSchema;
import org.zfin.zirc.entity.Lesion;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatus;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatusResult;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Per-field status for one {@link Lesion} row under a {@link org.zfin.zirc.entity.Mutation}.
 * Only {@code lesionType} is required — it's the gateway field the form's
 * type-picker forces before any other lesion field appears. All other fields
 * are optional and stay COMPLETE whether empty or filled.
 */
public final class LesionStatusComputer {

    public enum Field {
        LESION_TYPE              ("lesionType"),
        LESION_SIZE_BP           ("lesionSizeBp"),
        INSERTION_SIZE_BP        ("insertionSizeBp"),
        NUCLEOTIDE_CHANGE        ("nucleotideChange"),
        DELETED_SEQUENCE         ("deletedSequence"),
        INSERTED_SEQUENCE        ("insertedSequence"),
        TRANSGENE_SEQUENCE       ("transgeneSequence"),
        LOCATION_INLINE          ("locationInline"),
        FIVE_PRIME_FLANK         ("fivePrimeFlank"),
        THREE_PRIME_FLANK        ("threePrimeFlank"),
        HAS_LARGE_VARIANT        ("hasLargeVariant"),
        MUTATED_AMINO_ACIDS      ("mutatedAminoAcids"),
        MUTATED_AMINO_ACIDS_HGVS ("mutatedAminoAcidsHgvs"),
        AA_CHANGE_FROM           ("aaChangeFrom"),
        AA_CHANGE_TO             ("aaChangeTo"),
        AA_POSITION_START        ("aaPositionStart"),
        AA_POSITION_END          ("aaPositionEnd"),
        TRANSCRIPT_CONSEQUENCES  ("transcriptConsequences"),
        PROTEIN_CONSEQUENCES     ("proteinConsequences"),
        ADDITIONAL_INFO          ("additionalInfo");

        private final String path;

        Field(String path) { this.path = path; }

        public String getPath() { return path; }
    }

    private static final Set<String> REQUIRED_PATHS = collectRequiredPaths(ZircLesionFormSchema.schema());

    private static Set<String> collectRequiredPaths(JsonSchema node) {
        Set<String> out = new LinkedHashSet<>();
        if (node instanceof ObjectSchema obj) {
            if (obj.required() != null) out.addAll(obj.required());
            if (obj.properties() != null) {
                for (JsonSchema child : obj.properties().values()) {
                    out.addAll(collectRequiredPaths(child));
                }
            }
        }
        return out;
    }

    /**
     * Fields that are required as a <em>group</em> rather than individually:
     * at least one must be answered, and until one is, every member shows
     * MISSING.
     *
     * <p>ZFIN-10400 asks for this on the insertion-origin checklist, and
     * ZFIN-10379 on "either nucleotide information or amino acid information".
     * Neither can come from {@link ZircLesionFormSchema#schema()}'s
     * {@code required} list, which only expresses per-field requirements.
     *
     * <p>Deliberately a visual indicator only — nothing blocks a save. The
     * ticket defers real submission validation, and asks for a marker that
     * the eventual validation work has something concrete to replace.
     */
    private record RequiredGroup(List<String> paths, List<String> lesionTypes) {}

    private static final List<RequiredGroup> REQUIRED_GROUPS = List.of(
            // ZFIN-10379: nucleotide information OR amino acid information.
            // Modelled as one flat "any of these" group rather than a
            // disjunction of two sub-groups. The two differ only once a
            // curator has partly filled one side, and the ticket is explicit
            // that this is a marker for validation that does not exist yet —
            // so the simpler shape is the honest one until the real rule is
            // specified.
            new RequiredGroup(
                    List.of("nucleotideChange", "aaChangeFrom", "aaChangeTo",
                            "aaPositionStart", "mutatedAminoAcidsHgvs"),
                    List.of("point_mutation")));

    private static FieldStatus statusFor(Lesion lz, String path) {
        Object value = readProperty(lz, path);
        if (isEmpty(value) && REQUIRED_PATHS.contains(path)) return FieldStatus.MISSING;
        if (isEmpty(value) && groupUnanswered(lz, path)) return FieldStatus.MISSING;
        return FieldStatus.COMPLETE;
    }

    /**
     * True when {@code path} belongs to a required group that applies to this
     * lesion's type and no member of that group has been answered yet.
     *
     * <p>Gated on lesion type so the badge doesn't appear on a deletion, where
     * the questions are not even rendered.
     */
    private static boolean groupUnanswered(Lesion lz, String path) {
        // A lesion is created with no type — that is the state every one of
        // them starts in — and List.of(...).contains(null) throws rather than
        // returning false, so this must short-circuit before the lookup.
        String type = lz.getLesionType();
        if (type == null) return false;
        for (RequiredGroup g : REQUIRED_GROUPS) {
            if (!g.paths().contains(path)) continue;
            if (!g.lesionTypes().contains(type)) continue;
            boolean anyAnswered = g.paths().stream()
                    .anyMatch(p -> !isEmpty(readProperty(lz, p)));
            if (!anyAnswered) return true;
        }
        return false;
    }

    private LesionStatusComputer() {}

    /**
     * Section labels match the {@link ZircLesionFormSchema#uiSchema()}
     * Group labels, computed once via {@link SchemaSections}. The legacy
     * single "Lesion" key is gone — section status now mirrors the actual
     * UI groups (General, Sizing, Nucleotide Change, etc.).
     */
    private static final Map<String, List<String>> SECTIONS =
            SchemaSections.groupsToFields(ZircLesionFormSchema.uiSchema());

    public static FieldStatusResult compute(Lesion lz) {
        Map<String, FieldStatus> byField = new LinkedHashMap<>();
        for (Field f : Field.values()) {
            byField.put(f.getPath(), statusFor(lz, f.getPath()));
        }

        Map<String, FieldStatus> bySection = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> e : SECTIONS.entrySet()) {
            FieldStatus worst = FieldStatus.COMPLETE;
            for (String f : e.getValue()) {
                FieldStatus st = byField.get(f);
                if (st != null) worst = worst.worse(st);
            }
            bySection.put(e.getKey(), worst);
        }

        // Roll up overall from byField rather than bySection so that headless
        // (label-less) Groups — which SchemaSections skips — still contribute
        // their fields to the aggregate's overall status.
        FieldStatus overall = FieldStatus.COMPLETE;
        for (FieldStatus st : byField.values()) overall = overall.worse(st);

        return new FieldStatusResult(byField, bySection, overall);
    }

    private static Object readProperty(Object bean, String propertyName) {
        String method = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        try {
            Method m = bean.getClass().getMethod(method);
            return m.invoke(bean);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Cannot read property '" + propertyName + "' (no public " + method
                            + "() on " + bean.getClass().getName() + ")", e);
        }
    }

    private static boolean isEmpty(Object value) {
        if (value == null) return true;
        if (value instanceof String s) return s.isBlank();
        if (value instanceof Object[] arr) return arr.length == 0;
        if (value instanceof Collection<?> c) return c.isEmpty();
        return false;
    }
}
