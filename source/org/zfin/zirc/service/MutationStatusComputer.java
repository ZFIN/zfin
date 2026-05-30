package org.zfin.zirc.service;

import org.zfin.zirc.api.ZircMutationFormSchema;
import org.zfin.zirc.api.jsonschema.JsonSchema;
import org.zfin.zirc.api.jsonschema.ObjectSchema;
import org.zfin.zirc.entity.Gene;
import org.zfin.zirc.entity.GenotypingAssay;
import org.zfin.zirc.entity.Lesion;
import org.zfin.zirc.entity.Mutation;
import org.zfin.zirc.entity.Phenotype;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatus;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatusResult;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * System-derived per-field / per-section / overall status for a {@link Mutation},
 * mirroring {@link LineSubmissionStatusComputer}. Currently only the General
 * section is wired; additional sections (Genes, Lesions, Genotyping Assays,
 * Phenotypes, Lethality, Publications) can be added the same way.
 */
public final class MutationStatusComputer {

    /**
     * Catalogue of Mutation fields that participate in status computation.
     * Required-ness lives on the schema's {@code required} array
     * (see {@link ZircMutationFormSchema#schema()}); {@link #REQUIRED_PATHS}
     * unions it at class init.
     */
    public enum Field {
        ALLELE_IN_ZFIN              ("alleleInZfin"),
        ALLELE_DESIGNATION          ("alleleDesignation"),
        MUTAGENESIS_STAGE           ("mutagenesisStage"),
        MUTAGENESIS_PROTOCOL        ("mutagenesisProtocol"),
        MOLECULARLY_CHARACTERIZED   ("molecularlyCharacterized"),
        MUTATION_TYPE               ("mutationType"),
        ZFIN_RECORD_ESTABLISHED     ("zfinRecordEstablished"),
        // Conditional applicability — the compute loop skips it unless
        // zfinRecordEstablished is explicitly Yes.
        CELL_GENOMIC_FEATURE        ("cellGenomicFeature"),
        MUTATION_DISCOVERER         ("mutationDiscoverer"),
        MUTATION_INSTITUTION        ("mutationInstitution");

        private final String path;

        Field(String path) { this.path = path; }

        public String getPath() { return path; }
    }

    private static final Set<String> REQUIRED_PATHS = collectRequiredPaths(ZircMutationFormSchema.schema());

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

    private static FieldStatus statusFor(Mutation m, String path) {
        Object value = readProperty(m, path);
        if (isEmpty(value) && REQUIRED_PATHS.contains(path)) return FieldStatus.MISSING;
        return FieldStatus.COMPLETE;
    }

    /** Worst-of the per-field statuses for the listed fields. Mirrors the
     * private helper in {@link LineSubmissionStatusComputer}. */
    private static FieldStatus rollup(Map<String, FieldStatus> byField, Field... fields) {
        FieldStatus worst = FieldStatus.COMPLETE;
        for (Field f : fields) {
            FieldStatus st = byField.get(f.getPath());
            if (st != null) worst = worst.worse(st);
        }
        return worst;
    }

    private MutationStatusComputer() {}

    public static FieldStatusResult compute(Mutation m) {
        Map<String, FieldStatus> byField = new LinkedHashMap<>();
        for (Field f : Field.values()) {
            if (f == Field.CELL_GENOMIC_FEATURE
                    && !Boolean.TRUE.equals(m.getZfinRecordEstablished())) {
                continue;
            }
            byField.put(f.getPath(), statusFor(m, f.getPath()));
        }

        // Section keys must match the Group labels in ZircMutationFormSchema.uiSchema()
        // so the React SectionRenderer's lookup (config.sectionStatus[label]) hits.
        // General + Mutagenesis split the per-field rollup along the schema's
        // group boundaries; the legacy "Overview" key is gone.
        FieldStatus general = rollup(byField,
                Field.ALLELE_IN_ZFIN, Field.ALLELE_DESIGNATION, Field.MUTATION_TYPE,
                Field.ZFIN_RECORD_ESTABLISHED, Field.CELL_GENOMIC_FEATURE,
                Field.MUTATION_DISCOVERER, Field.MUTATION_INSTITUTION);
        FieldStatus mutagenesis = rollup(byField,
                Field.MUTAGENESIS_STAGE, Field.MUTAGENESIS_PROTOCOL,
                Field.MOLECULARLY_CHARACTERIZED);

        // Genes section is required at the Mutation level: a mutation must
        // declare at least one Gene; if present, each Gene rolls up its own
        // overall (mutatedGene is required per-row).
        FieldStatus genesSection;
        if (m.getGenes() == null || m.getGenes().isEmpty()) {
            genesSection = FieldStatus.MISSING;
        } else {
            genesSection = FieldStatus.COMPLETE;
            for (Gene g : m.getGenes()) {
                genesSection = genesSection.worse(GeneStatusComputer.compute(g).overall());
            }
        }

        // Lesions section: worst-of each Lesion's overall (lesionType is required
        // per-row). Empty collection is allowed → COMPLETE.
        FieldStatus lesionsSection = FieldStatus.COMPLETE;
        if (m.getLesions() != null) {
            for (Lesion lz : m.getLesions()) {
                lesionsSection = lesionsSection.worse(LesionStatusComputer.compute(lz).overall());
            }
        }

        // Genotyping Assays section: worst-of each assay's overall (assayType
        // is required per-row). Empty collection is allowed → COMPLETE.
        FieldStatus assaysSection = FieldStatus.COMPLETE;
        if (m.getGenotypingAssays() != null) {
            for (GenotypingAssay ga : m.getGenotypingAssays()) {
                assaysSection = assaysSection.worse(GenotypingAssayStatusComputer.compute(ga).overall());
            }
        }

        // Phenotypes section: worst-of each phenotype's overall (description
        // is required per-row). Empty collection is allowed → COMPLETE.
        FieldStatus phenotypesSection = FieldStatus.COMPLETE;
        if (m.getPhenotypes() != null) {
            for (Phenotype p : m.getPhenotypes()) {
                phenotypesSection = phenotypesSection.worse(PhenotypeStatusComputer.compute(p).overall());
            }
        }

        Map<String, FieldStatus> bySection = new LinkedHashMap<>();
        bySection.put("General",           general);
        bySection.put("Mutagenesis",       mutagenesis);
        bySection.put("Genes",             genesSection);
        bySection.put("Lesions",           lesionsSection);
        bySection.put("Genotyping Assays", assaysSection);
        bySection.put("Phenotypes",        phenotypesSection);
        bySection.put("Lethality",         FieldStatus.COMPLETE);
        bySection.put("Publications",      FieldStatus.COMPLETE);

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
