package org.zfin.zirc.service;

import org.zfin.zirc.entity.Gene;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatus;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatusResult;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-field status for one {@link Gene} row under a {@link org.zfin.zirc.entity.Mutation}.
 * Only {@code mutatedGene} is required; everything else is optional and will be
 * COMPLETE whether empty or filled.
 */
public final class GeneStatusComputer {

    public enum Field {
        MUTATED_GENE          ("mutatedGene",         true),
        LINKAGE_GROUP         ("linkageGroup",        false),
        GENBANK_GENOMIC_DNA   ("genbankGenomicDna",   false),
        GENBANK_CDNA          ("genbankCdna",         false);

        private final String path;
        private final boolean required;

        Field(String path, boolean required) {
            this.path = path;
            this.required = required;
        }

        public String  getPath()    { return path; }
        public boolean isRequired() { return required; }

        public FieldStatus statusFor(Gene g) {
            Object value = readProperty(g, path);
            boolean empty = isEmpty(value);
            if (empty && required) return FieldStatus.MISSING;
            return FieldStatus.COMPLETE;
        }
    }

    private GeneStatusComputer() {}

    public static FieldStatusResult compute(Gene g) {
        Map<String, FieldStatus> byField = new LinkedHashMap<>();
        for (Field f : Field.values()) {
            byField.put(f.getPath(), f.statusFor(g));
        }

        FieldStatus overall = FieldStatus.COMPLETE;
        for (FieldStatus st : byField.values()) overall = overall.worse(st);

        Map<String, FieldStatus> bySection = new LinkedHashMap<>();
        bySection.put("Gene", overall);

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
