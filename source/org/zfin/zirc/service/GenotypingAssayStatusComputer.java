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
import java.util.List;
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
        RESTRICTION_ENZYME_VENDOR     ("restrictionEnzymeVendor"),
        ENZYME_CLEAVES_WT             ("enzymeCleavesWt"),
        ENZYME_CLEAVES_MUT            ("enzymeCleavesMut"),
        EXPECTED_WT_DIGEST            ("expectedWtDigest"),
        EXPECTED_MUT_DIGEST           ("expectedMutDigest"),
        ADDITIONAL_INFO               ("additionalInfo"),
        SEQUENCING_PRIMER             ("sequencingPrimer"),
        DCAPS_MISMATCH_PRIMER_CHOICE  ("dcapsMismatchPrimerChoice"),
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

    /**
     * Primer fields carrying the ZFIN-10407 rule -- a DNA sequence must be
     * entered, and it must be at least {@link ZircAssayFormSchema#PRIMER_MIN_LENGTH}
     * bases -- mapped to the assay types on which the form actually shows that
     * field.
     *
     * <p>The value matters as much as the key: a rule that fired regardless of
     * type would report a missing forward primer on an ASA assay, which shows
     * the WT/mutant/common trio instead and has no forward primer to fill in.
     *
     * <p>ZFIN-10407 named the forward / reverse pair; ZFIN-10439 asked for the
     * same minimum on the ASA primer boxes, so the trio is here under its own
     * type set. Still absent: {@code sequencingPrimer}, which no ticket has
     * asked for, and {@code dcapsMismatchPrimerChoice}, which holds "Forward"
     * or "Reverse" rather than a sequence.
     *
     * <p>Kept in step with the {@code minBases} options
     * {@link ZircAssayFormSchema#uiSchema()} emits, which drive the matching
     * inline hint on the form.
     */
    private static final Map<String, List<String>> LENGTH_CHECKED_PRIMERS = Map.of(
            Field.FORWARD_PRIMER.getPath(),     ZircAssayFormSchema.FWD_REV_PRIMER_TYPES,
            Field.REVERSE_PRIMER.getPath(),     ZircAssayFormSchema.FWD_REV_PRIMER_TYPES,
            Field.WT_SPECIFIC_PRIMER.getPath(), ZircAssayFormSchema.ALLELE_SPECIFIC_TYPES,
            Field.MUT_SPECIFIC_PRIMER.getPath(), ZircAssayFormSchema.ALLELE_SPECIFIC_TYPES,
            Field.COMMON_PRIMER.getPath(),      ZircAssayFormSchema.ALLELE_SPECIFIC_TYPES);

    private static FieldStatus statusFor(GenotypingAssay ga, String path) {
        Object value = readProperty(ga, path);

        if (primerRuleApplies(ga, path)) {
            if (isEmpty(value)) return FieldStatus.MISSING;
            if (value instanceof String s
                    && s.trim().length() < ZircAssayFormSchema.PRIMER_MIN_LENGTH) {
                return FieldStatus.IN_PROGRESS;
            }
            return FieldStatus.COMPLETE;
        }

        if (isEmpty(value) && REQUIRED_PATHS.contains(path)) return FieldStatus.MISSING;
        return FieldStatus.COMPLETE;
    }

    /**
     * True when {@code path} is length-checked and this assay's type is one
     * that shows it. A null type means the submitter has not picked one yet, so
     * no primer box is on screen and nothing should be flagged.
     */
    private static boolean primerRuleApplies(GenotypingAssay ga, String path) {
        List<String> types = LENGTH_CHECKED_PRIMERS.get(path);
        return types != null && ga.getAssayType() != null && types.contains(ga.getAssayType());
    }

    private GenotypingAssayStatusComputer() {}

    private static final Map<String, List<String>> SECTIONS =
            SchemaSections.groupsToFields(ZircAssayFormSchema.uiSchema());

    public static FieldStatusResult compute(GenotypingAssay ga) {
        Map<String, FieldStatus> byField = new LinkedHashMap<>();
        for (Field f : Field.values()) {
            byField.put(f.getPath(), statusFor(ga, f.getPath()));
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
