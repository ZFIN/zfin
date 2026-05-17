package org.zfin.zirc.service;

import org.zfin.zirc.entity.Lesion;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatus;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatusResult;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-field status for one {@link Lesion} row under a {@link org.zfin.zirc.entity.Mutation}.
 * Only {@code lesionType} is required — it's the gateway field the form's
 * type-picker forces before any other lesion field appears. All other fields
 * are optional and stay COMPLETE whether empty or filled.
 */
public final class LesionStatusComputer {

    public enum Field {
        LESION_TYPE              ("lesionType",              true),
        LESION_SIZE_BP           ("lesionSizeBp",            false),
        INSERTION_SIZE_BP        ("insertionSizeBp",         false),
        NUCLEOTIDE_CHANGE        ("nucleotideChange",        false),
        DELETED_SEQUENCE         ("deletedSequence",         false),
        INSERTED_SEQUENCE        ("insertedSequence",        false),
        TRANSGENE_SEQUENCE       ("transgeneSequence",       false),
        LOCATION_INLINE          ("locationInline",          false),
        FIVE_PRIME_FLANK         ("fivePrimeFlank",          false),
        THREE_PRIME_FLANK        ("threePrimeFlank",         false),
        HAS_LARGE_VARIANT        ("hasLargeVariant",         false),
        MUTATED_AMINO_ACIDS      ("mutatedAminoAcids",       false),
        MUTATED_AMINO_ACIDS_HGVS ("mutatedAminoAcidsHgvs",   false),
        ADDITIONAL_INFO          ("additionalInfo",          false);

        private final String path;
        private final boolean required;

        Field(String path, boolean required) {
            this.path = path;
            this.required = required;
        }

        public String  getPath()    { return path; }
        public boolean isRequired() { return required; }

        public FieldStatus statusFor(Lesion lz) {
            Object value = readProperty(lz, path);
            boolean empty = isEmpty(value);
            if (empty && required) return FieldStatus.MISSING;
            return FieldStatus.COMPLETE;
        }
    }

    private LesionStatusComputer() {}

    public static FieldStatusResult compute(Lesion lz) {
        Map<String, FieldStatus> byField = new LinkedHashMap<>();
        for (Field f : Field.values()) {
            byField.put(f.getPath(), f.statusFor(lz));
        }

        FieldStatus overall = FieldStatus.COMPLETE;
        for (FieldStatus st : byField.values()) overall = overall.worse(st);

        Map<String, FieldStatus> bySection = new LinkedHashMap<>();
        bySection.put("Lesion", overall);

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
