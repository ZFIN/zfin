package org.zfin.zirc.service;

import org.zfin.zirc.api.ZircAssayFormSchema;
import org.zfin.zirc.api.jsonschema.JsonSchema;
import org.zfin.zirc.api.jsonschema.ObjectSchema;
import org.zfin.zirc.entity.GenotypingAssay;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatus;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatusResult;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Per-field status for one {@link GenotypingAssay} row under a {@link org.zfin.zirc.entity.Mutation}.
 * Only {@code assayType} is required — it's the gateway field the form's
 * type-picker forces before any other assay field appears. Everything else
 * is optional and stays COMPLETE whether empty or filled.
 */
public final class GenotypingAssayStatusComputer {

    public enum Field {
        ASSAY_TYPE                    ("assayType"),
        FORWARD_PRIMER                ("forwardPrimer"),
        REVERSE_PRIMER                ("reversePrimer"),
        EXPECTED_WT_PCR               ("expectedWtPcr"),
        EXPECTED_MUT_PCR              ("expectedMutPcr"),
        RESTRICTION_ENZYME_NAME       ("restrictionEnzymeName"),
        RESTRICTION_ENZYME_CATALOG    ("restrictionEnzymeCatalog"),
        ENZYME_CLEAVES                ("enzymeCleaves"),
        EXPECTED_WT_DIGEST            ("expectedWtDigest"),
        EXPECTED_MUT_DIGEST           ("expectedMutDigest"),
        ADDITIONAL_INFO               ("additionalInfo"),
        SEQUENCING_PRIMER             ("sequencingPrimer"),
        DCAPS_MISMATCH_PRIMER         ("dcapsMismatchPrimer"),
        WT_SPECIFIC_PRIMER            ("wtSpecificPrimer"),
        MUT_SPECIFIC_PRIMER           ("mutSpecificPrimer"),
        COMMON_PRIMER                 ("commonPrimer"),
        KASP_GENOMIC_SEQUENCE         ("kaspGenomicSequence"),
        SSLP_MARKER_NAME              ("sslpMarkerName"),
        SSLP_DISTANCE                 ("sslpDistance"),
        SSLP_GENOMIC_LOCATION         ("sslpGenomicLocation"),
        SSLP_INDUCED_BACKGROUND       ("sslpInducedBackground"),
        SSLP_OUTCROSSED_BACKGROUND    ("sslpOutcrossedBackground"),
        SSLP_INDUCED_PCR              ("sslpInducedPcr"),
        SSLP_OUTCROSSED_PCR           ("sslpOutcrossedPcr");

        private final String path;

        Field(String path) { this.path = path; }

        public String getPath() { return path; }
    }

    private static final Set<String> REQUIRED_PATHS = collectRequiredPaths(ZircAssayFormSchema.schema());

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

    private static FieldStatus statusFor(GenotypingAssay ga, String path) {
        Object value = readProperty(ga, path);
        if (isEmpty(value) && REQUIRED_PATHS.contains(path)) return FieldStatus.MISSING;
        return FieldStatus.COMPLETE;
    }

    private GenotypingAssayStatusComputer() {}

    public static FieldStatusResult compute(GenotypingAssay ga) {
        Map<String, FieldStatus> byField = new LinkedHashMap<>();
        for (Field f : Field.values()) {
            byField.put(f.getPath(), statusFor(ga, f.getPath()));
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
