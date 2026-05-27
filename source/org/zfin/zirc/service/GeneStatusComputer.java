package org.zfin.zirc.service;

import org.zfin.zirc.api.ZircGeneFormSchema;
import org.zfin.zirc.api.jsonschema.JsonSchema;
import org.zfin.zirc.api.jsonschema.ObjectSchema;
import org.zfin.zirc.entity.Gene;
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
 * Per-field status for one {@link Gene} row under a {@link org.zfin.zirc.entity.Mutation}.
 * Only {@code mutatedGene} is required; everything else is optional and will be
 * COMPLETE whether empty or filled.
 */
public final class GeneStatusComputer {

    /**
     * Field paths match the schema property names (and DTO components), so
     * the schema's {@code required} list is the single source of truth for
     * required-ness across the stack. The entity exposes a
     * {@link Gene#getMutatedGeneZdbID()} {@code @Transient} accessor so
     * reflection can read it under the schema-side name.
     */
    public enum Field {
        MUTATED_GENE_ZDB_ID   ("mutatedGeneZdbID"),
        LINKAGE_GROUP         ("linkageGroup"),
        GENBANK_GENOMIC_DNA   ("genbankGenomicDna"),
        GENBANK_CDNA          ("genbankCdna");

        private final String path;

        Field(String path) { this.path = path; }

        public String getPath() { return path; }
    }

    private static final Set<String> REQUIRED_PATHS = collectRequiredPaths(ZircGeneFormSchema.schema());

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

    private static FieldStatus statusFor(Gene g, String path) {
        Object value = readProperty(g, path);
        if (isEmpty(value) && REQUIRED_PATHS.contains(path)) return FieldStatus.MISSING;
        return FieldStatus.COMPLETE;
    }

    private GeneStatusComputer() {}

    private static final Map<String, List<String>> SECTIONS =
            SchemaSections.groupsToFields(ZircGeneFormSchema.uiSchema());

    public static FieldStatusResult compute(Gene g) {
        Map<String, FieldStatus> byField = new LinkedHashMap<>();
        for (Field f : Field.values()) {
            byField.put(f.getPath(), statusFor(g, f.getPath()));
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
