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

    private static FieldStatus statusFor(Lesion lz, String path) {
        Object value = readProperty(lz, path);
        if (isEmpty(value) && REQUIRED_PATHS.contains(path)) return FieldStatus.MISSING;
        return FieldStatus.COMPLETE;
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

        FieldStatus overall = FieldStatus.COMPLETE;
        for (FieldStatus st : bySection.values()) overall = overall.worse(st);

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
