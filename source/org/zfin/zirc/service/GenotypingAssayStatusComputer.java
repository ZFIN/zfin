package org.zfin.zirc.service;

import org.zfin.zirc.entity.GenotypingAssay;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatus;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatusResult;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-field status for one {@link GenotypingAssay} row under a {@link org.zfin.zirc.entity.Mutation}.
 * Only {@code assayType} is required — it's the gateway field the form's
 * type-picker forces before any other assay field appears. Everything else
 * is optional and stays COMPLETE whether empty or filled.
 */
public final class GenotypingAssayStatusComputer {

    public enum Field {
        ASSAY_TYPE                    ("assayType",                  true),
        FORWARD_PRIMER                ("forwardPrimer",              false),
        REVERSE_PRIMER                ("reversePrimer",              false),
        EXPECTED_WT_PCR               ("expectedWtPcr",              false),
        EXPECTED_MUT_PCR              ("expectedMutPcr",             false),
        RESTRICTION_ENZYME_NAME       ("restrictionEnzymeName",      false),
        RESTRICTION_ENZYME_CATALOG    ("restrictionEnzymeCatalog",   false),
        ENZYME_CLEAVES                ("enzymeCleaves",              false),
        EXPECTED_WT_DIGEST            ("expectedWtDigest",           false),
        EXPECTED_MUT_DIGEST           ("expectedMutDigest",          false),
        ADDITIONAL_INFO               ("additionalInfo",             false),
        SEQUENCING_PRIMER             ("sequencingPrimer",           false),
        DCAPS_MISMATCH_PRIMER         ("dcapsMismatchPrimer",        false),
        WT_SPECIFIC_PRIMER            ("wtSpecificPrimer",           false),
        MUT_SPECIFIC_PRIMER           ("mutSpecificPrimer",          false),
        COMMON_PRIMER                 ("commonPrimer",               false),
        KASP_GENOMIC_SEQUENCE         ("kaspGenomicSequence",        false),
        SSLP_MARKER_NAME              ("sslpMarkerName",             false),
        SSLP_DISTANCE                 ("sslpDistance",               false),
        SSLP_GENOMIC_LOCATION         ("sslpGenomicLocation",        false),
        SSLP_INDUCED_BACKGROUND       ("sslpInducedBackground",      false),
        SSLP_OUTCROSSED_BACKGROUND    ("sslpOutcrossedBackground",   false),
        SSLP_INDUCED_PCR              ("sslpInducedPcr",             false),
        SSLP_OUTCROSSED_PCR           ("sslpOutcrossedPcr",          false);

        private final String path;
        private final boolean required;

        Field(String path, boolean required) {
            this.path = path;
            this.required = required;
        }

        public String  getPath()    { return path; }
        public boolean isRequired() { return required; }

        public FieldStatus statusFor(GenotypingAssay ga) {
            Object value = readProperty(ga, path);
            boolean empty = isEmpty(value);
            if (empty && required) return FieldStatus.MISSING;
            return FieldStatus.COMPLETE;
        }
    }

    private GenotypingAssayStatusComputer() {}

    public static FieldStatusResult compute(GenotypingAssay ga) {
        Map<String, FieldStatus> byField = new LinkedHashMap<>();
        for (Field f : Field.values()) {
            byField.put(f.getPath(), f.statusFor(ga));
        }

        FieldStatus overall = FieldStatus.COMPLETE;
        for (FieldStatus st : byField.values()) overall = overall.worse(st);

        Map<String, FieldStatus> bySection = new LinkedHashMap<>();
        bySection.put("Genotyping Assay", overall);

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
